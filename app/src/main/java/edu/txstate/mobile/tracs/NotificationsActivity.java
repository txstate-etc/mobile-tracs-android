package edu.txstate.mobile.tracs;

import android.app.ProgressDialog;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.JsonObject;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import edu.txstate.mobile.tracs.adapters.NotificationsAdapter;
import edu.txstate.mobile.tracs.notifications.DispatchNotification;
import edu.txstate.mobile.tracs.notifications.NotificationTypes;
import edu.txstate.mobile.tracs.notifications.NotificationsBundle;
import edu.txstate.mobile.tracs.notifications.TracsAppNotification;
import edu.txstate.mobile.tracs.notifications.tracs.TracsNotification;
import edu.txstate.mobile.tracs.util.IntegrationServer;
import edu.txstate.mobile.tracs.util.MenuController;
import edu.txstate.mobile.tracs.util.TracsClient;
import edu.txstate.mobile.tracs.util.async.StatusUpdate;
import edu.txstate.mobile.tracs.util.http.HttpQueue;
import edu.txstate.mobile.tracs.util.http.requests.TracsPageIdRequest;
import edu.txstate.mobile.tracs.util.http.requests.TracsSiteRequest;

public class NotificationsActivity extends BaseTracsActivity implements SwipeRefreshLayout.OnRefreshListener {
    public static final String TAG = "NotificationsActivity";
    private static final String SCREEN_NAME = "Notifications";
    private NotificationsBundle tracsNotifications;
    private NotificationsBundle dispatchNotifications;
    private int notificationsRetrieved;
    private ProgressDialog loadingDialog;
    private NotificationsAdapter adapter;
    private ListView notificationsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_notifications);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        super.hitScreenView(SCREEN_NAME);
        init();
    }

    private void init() {
        loadingDialog = new ProgressDialog(this);
        refreshNotifications(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.setupOptionsMenu(menu);
        MenuItem notifications = super.optionsMenu.findItem(R.id.menu_notifications);
        notifications.setEnabled(false);
        notifications.getActionView().setAlpha(0.5f);
        notifications.getActionView().setClickable(false);
        MenuItem refreshButton = super.optionsMenu.findItem(R.id.menu_refresh);
        refreshButton.setIcon(
                new IconDrawable(this, FontAwesomeIcons.fa_refresh)
                        .colorRes(R.color.colorHeaderIcons)
                        .actionBarSize()
        );
        refreshButton.setOnMenuItemClickListener(
                item -> {
                    NotificationsActivity.this.refreshNotifications(true);
                    return false;
                }
        );
        refreshButton.setVisible(false);
        return true;
    }

    @Override
    public void onRefresh() {
        refreshNotifications(false);
    }

    private void refreshNotifications(boolean showDialog) {
        if (showDialog) {
            loadingDialog.setMessage("Loading Notifications...");
            loadingDialog.show();
        }
        if (this.tracsNotifications != null ){
            this.tracsNotifications.deleteObservers();
        }
        this.tracsNotifications = new NotificationsBundle();
        this.tracsNotifications.addObserver(this);
        IntegrationServer.getInstance()
                .getDispatchNotifications(NotificationsActivity.this::onResponse);
    }

    private void onResponse(NotificationsBundle response) {
        if (response.size() == 0) {
            displayListView();
            return;
        }
        this.dispatchNotifications = response;
        super.setBadgeCount(this.dispatchNotifications.totalUnread());
        TracsClient tracs = TracsClient.getInstance();
        tracs.getNotifications(response, NotificationsActivity.this::onResponse, AnalyticsApplication.getContext());
    }

    private void onResponse(TracsNotification response) {
        if (response.getType().equals(NotificationTypes.ERROR)) {
            Log.wtf(TAG, "Error retrieving notifications from TRACS");
        } else {
            this.tracsNotifications.add(response);
        }
    }

    private boolean allRequestsAreBack() {
        return this.notificationsRetrieved >= this.dispatchNotifications.size();
    }

    private void displayListView() {
        loadingDialog.dismiss();
        notificationsList = (ListView) findViewById(R.id.notifications_list);
        adapter = new NotificationsAdapter(tracsNotifications, this);
        notificationsList.setAdapter(adapter);
        new StatusUpdate().updateSeen(this.tracsNotifications);
    }

    @Override
    public void update(Observable tracsNotifications, Object newNotification) {
        TracsNotification notification;
        try {
            notification = (TracsNotification) newNotification;
        } catch (ClassCastException e) {
            Log.wtf(TAG, e.getMessage());
            return;
        }

        if (notification != null && !notification.hasSiteName()) {
            HttpQueue requestQueue = HttpQueue.getInstance(AnalyticsApplication.getContext());
            Map<String, String> headers = new HashMap<>();
            requestQueue.addToRequestQueue(new TracsSiteRequest(
                    notification.getSiteId(), headers, NotificationsActivity.this::onSiteNameReturned
            ), this);

            String pageIdUrl = getString(R.string.tracs_base) +
                               getString(R.string.tracs_site) +
                               notification.getSiteId() +
                               "/pages.json";
            requestQueue.addToRequestQueue(new TracsPageIdRequest(
                pageIdUrl, notification.getDispatchId(), NotificationsActivity.this::onPageIdReturned
            ), this);
        }
    }

    private void onSiteNameReturned(JsonObject siteInfo) {
        for (TracsAppNotification notification : tracsNotifications) {
            try {
                TracsNotification tracsNotification = TracsNotification.class.cast(notification);
                boolean titleIsSet = !TracsNotification.NOT_SET.equals(tracsNotification.getSiteName());
                if (titleIsSet) {
                    this.notificationsRetrieved += 1;
                    return;
                }

                String siteId = tracsNotification.getSiteId();
                String fetchedSiteId = siteInfo.get("entityId").getAsString();

                if (fetchedSiteId != null && fetchedSiteId.equals(siteId)) {
                    String siteName = siteInfo.get("entityTitle").getAsString();
                    tracsNotification.setSiteName(siteName);
                    this.notificationsRetrieved += 1;
                }
            } catch (NullPointerException | ClassCastException e) {
                Log.wtf(TAG, "Could not set site name.");
            }
        }

        if (allRequestsAreBack()) {
            this.notificationsRetrieved = 0;
            displayListView();
        }
    }

    private void onPageIdReturned(Map<String, String> pageIdPair) {
        for (TracsAppNotification notification : tracsNotifications) {
            try {
                TracsNotification tracsNotification = TracsNotification.class.cast(notification);
                String pageId = pageIdPair.get(tracsNotification.getDispatchId());
                if (pageId != null) {
                    tracsNotification.setPageId(pageId);
                }
            } catch (NullPointerException | ClassCastException e) {
                Log.wtf(TAG, "Could not set pageId of notification.");
            }
        }
    }

    public void setBadgeCount(int count) {
        super.setBadgeCount(count);
    }

}
