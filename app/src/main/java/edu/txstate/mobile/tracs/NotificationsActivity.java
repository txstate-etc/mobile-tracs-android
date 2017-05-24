package edu.txstate.mobile.tracs;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.google.gson.JsonObject;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

import edu.txstate.mobile.tracs.adapters.NotificationsAdapter;
import edu.txstate.mobile.tracs.notifications.NotificationTypes;
import edu.txstate.mobile.tracs.notifications.NotificationsBundle;
import edu.txstate.mobile.tracs.notifications.TracsAppNotification;
import edu.txstate.mobile.tracs.notifications.tracs.TracsNotification;
import edu.txstate.mobile.tracs.util.IntegrationServer;
import edu.txstate.mobile.tracs.util.TracsClient;
import edu.txstate.mobile.tracs.util.async.StatusUpdate;
import edu.txstate.mobile.tracs.util.http.HttpQueue;
import edu.txstate.mobile.tracs.util.http.requests.TracsPageIdRequest;
import edu.txstate.mobile.tracs.util.http.requests.TracsSiteRequest;

public class NotificationsActivity extends BaseTracsActivity {
    public static final String TAG = "NotificationsActivity";
    private static final String SCREEN_NAME = "Notifications";
    private NotificationsBundle tracsNotifications;
    private NotificationsBundle dispatchNotifications;
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
        refreshNotifications();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.setupOptionsMenu(menu);
        MenuItem notifications = super.optionsMenu.findItem(R.id.menu_notifications);
        notifications.setVisible(false);
        MenuItem clearAll = super.optionsMenu.findItem(R.id.clear_all);
        clearAll.setIcon(
                new IconDrawable(this, FontAwesomeIcons.fa_times_circle_o)
                        .colorRes(R.color.colorHeaderIcons)
                        .actionBarSize()
        );
        clearAll.setOnMenuItemClickListener(
                item -> {
                    NotificationsActivity.this.promptClearAll();
                    return false;
                }
        );
        clearAll.setVisible(false);
        return true;
    }

    private void refreshNotifications() {
        if (this.tracsNotifications != null ){
            this.tracsNotifications.deleteObservers();
        }
        this.tracsNotifications = new NotificationsBundle();
        this.tracsNotifications.addObserver(this);
        IntegrationServer.getInstance()
                .getDispatchNotifications(NotificationsActivity.this::onDispatchResponse);
    }

    private void promptClearAll() {
        AlertDialog clearPrompt = new ClearAllDialog(this, this::clearAll);
        clearPrompt.show();
    }

    private void clearAll(View view) {
        this.notificationsList.setAdapter(null);
        this.tracsNotifications.removeAll();
        this.dispatchNotifications.removeAll();
        Log.i(TAG, "Notifications have been cleared");
    }

    private void onDispatchResponse(NotificationsBundle response) {
        if (response.size() == 0) {
            displayListView();
            return;
        }
        this.dispatchNotifications = response;
        super.setBadgeCount(this.dispatchNotifications.totalUnread());
        TracsClient tracs = TracsClient.getInstance();
        tracs.getNotifications(response, NotificationsActivity.this::onTracsResponse, AnalyticsApplication.getContext());
    }

    private void onTracsResponse(TracsNotification response) {
        if (response.getType().equals(NotificationTypes.ERROR)) {
            Log.wtf(TAG, "Error retrieving notifications from TRACS");
        } else {
            this.tracsNotifications.add(response);
        }
    }

    /**
     * Overkill to check if notifications are back, activate only if there is a problem
     * with not getting PageIds or SiteNames back.
     * @return true if PageIds and Sitenames are set and both dispatch and tracs arrays
     * are the same size.
     */
    private boolean allRequestsAreBack() {
        boolean sizeMatch = this.tracsNotifications.size() >= this.dispatchNotifications.size();
        if (!sizeMatch) { return false; }
        int totalSiteNames = 0;
        int totalPageIds = 0;
        for (TracsAppNotification notification : this.tracsNotifications) {
            TracsNotification tracsNote = TracsNotification.class.cast(notification);
            if (tracsNote.hasSiteName()) { totalSiteNames++; }
            if (tracsNote.hasPageId()) { totalPageIds++; }
        }
        boolean siteNamesDone = totalSiteNames == tracsNotifications.size();
        boolean pageIdsDone = totalPageIds == tracsNotifications.size();
        return siteNamesDone && pageIdsDone;
    }

    private void displayListView() {
        findViewById(R.id.loading_spinner).setVisibility(View.GONE);
        this.notificationsList = (ListView) findViewById(R.id.notifications_list);
        adapter = new NotificationsAdapter(tracsNotifications, this);
        this.notificationsList.setAdapter(adapter);
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
        if (notification == null) { return; }
        HttpQueue requestQueue = HttpQueue.getInstance(AnalyticsApplication.getContext());
        if (!notification.hasSiteName()) {
            Map<String, String> headers = new HashMap<>();
            requestQueue.addToRequestQueue(new TracsSiteRequest(
                    notification.getSiteId(), headers, NotificationsActivity.this::onSiteNameReturned
            ), this);
        }
        String pageIdUrl = getString(R.string.tracs_base) +
                getString(R.string.tracs_site) +
                notification.getSiteId() +
                "/pages.json";
        Log.wtf(TAG, pageIdUrl);
        requestQueue.addToRequestQueue(new TracsPageIdRequest(
                pageIdUrl, notification.getDispatchId(), NotificationsActivity.this::onPageIdReturned
        ), this);
    }

    private void onSiteNameReturned(JsonObject siteInfo) {
        for (TracsAppNotification notification : tracsNotifications) {
            try {
                TracsNotification tracsNotification = TracsNotification.class.cast(notification);
                boolean titleIsSet = !TracsNotification.NOT_SET.equals(tracsNotification.getSiteName());
                if (titleIsSet) {
                    continue;
                }

                String siteId = tracsNotification.getSiteId();
                String fetchedSiteId = siteInfo.get("entityId").getAsString();

                if (fetchedSiteId != null && fetchedSiteId.equals(siteId)) {
                    String siteName = siteInfo.get("entityTitle").getAsString();
                    tracsNotification.setSiteName(siteName);
                }
            } catch (NullPointerException | ClassCastException e) {
                Log.wtf(TAG, "Could not set site name.");
            }
        }
    }

    private void onPageIdReturned(Map<String, String> pageIdPair) {
        for (TracsAppNotification notification : tracsNotifications) {
            try {
                TracsNotification tracsNotification = TracsNotification.class.cast(notification);
                String pageId = pageIdPair.get(tracsNotification.getDispatchId());
                if (pageId != null) {
                    tracsNotification.setPageId(pageId);
                    break;
                }
            } catch (NullPointerException | ClassCastException e) {
                Log.wtf(TAG, "Could not set pageId of notification.");
            }
        }
        if (allRequestsAreBack()) {
            HttpQueue.getInstance(AnalyticsApplication.getContext()).getRequestQueue().cancelAll(this);
            displayListView();
        }
    }

    public void setBadgeCount(int count) {
        super.setBadgeCount(count);
    }

}
