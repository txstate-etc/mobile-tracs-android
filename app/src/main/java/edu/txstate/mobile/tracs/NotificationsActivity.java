package edu.txstate.mobile.tracs;

import android.app.ProgressDialog;
import android.content.Intent;
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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import edu.txstate.mobile.tracs.notifications.NotificationTypes;
import edu.txstate.mobile.tracs.notifications.NotificationsAdapter;
import edu.txstate.mobile.tracs.notifications.NotificationsBundle;
import edu.txstate.mobile.tracs.notifications.TracsAppNotification;
import edu.txstate.mobile.tracs.notifications.tracs.TracsNotification;
import edu.txstate.mobile.tracs.util.IntegrationServer;
import edu.txstate.mobile.tracs.util.TracsClient;
import edu.txstate.mobile.tracs.util.http.HttpQueue;
import edu.txstate.mobile.tracs.util.http.requests.TracsSiteRequest;

public class NotificationsActivity
        extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, Observer {
    private static final String TAG = "NotificationsActivity";
    private static final String SCREEN_NAME = "Notifications";
    private NotificationsBundle tracsNotifications;
    private NotificationsBundle dispatchNotifications;
    private int notificationsRetrieved;
    private ProgressDialog loadingDialog;
    private Tracker analyticsTracker;
    private SwipeRefreshLayout refreshLayout;
    private NotificationsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        analyticsTracker = AnalyticsApplication.class.cast(getApplication()).getDefaultTracker();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Activity has resumed");
        analyticsTracker.setScreenName(SCREEN_NAME);
        analyticsTracker.send(new HitBuilders.ScreenViewBuilder().build());
        init();
    }

    private void init() {
        loadingDialog = new ProgressDialog(this);

        setContentView(R.layout.activity_notifications);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.notification_swipe_refresh);
        refreshLayout.setOnRefreshListener(NotificationsActivity.this);
        refreshLayout.setColorSchemeResources(R.color.colorPrimary);
        refreshNotifications(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.findItem(R.id.notifications_menu).setVisible(false);
        MenuItem refreshButton = menu.findItem(R.id.menu_refresh);
        refreshButton.setIcon(
                new IconDrawable(this, FontAwesomeIcons.fa_refresh)
                        .colorRes(R.color.colorAccent)
                        .actionBarSize()
        );
        refreshButton.setOnMenuItemClickListener(
                item -> {
                    NotificationsActivity.this.refreshNotifications(true);
                    return false;
                }
        );
        refreshButton.setVisible(true);
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
        this.tracsNotifications = new NotificationsBundle();
        this.tracsNotifications.addObserver(this);
        IntegrationServer.getInstance()
                .getDispatchNotifications(NotificationsActivity.this::onResponse);
    }

    protected void onResponse(NotificationsBundle response) {
        if (response.size() == 0) {
            return;
        }
        this.dispatchNotifications = response;
        TracsClient tracs = TracsClient.getInstance();
        tracs.getNotifications(response, NotificationsActivity.this::onResponse, AnalyticsApplication.getContext());
    }

    public void onResponse(TracsNotification response) {
        if (response.getType().equals(NotificationTypes.ERROR)) {
            Log.wtf(TAG, "Error retrieving notifications from TRACS");
        } else {
            this.tracsNotifications.addOne(response);
        }
    }

    private boolean allRequestsAreBack() {
        return this.notificationsRetrieved >= this.dispatchNotifications.size();
    }

    private void displayListView() {
        loadingDialog.dismiss();
        refreshLayout.setRefreshing(false);
        final ListView notificationsList = (ListView) findViewById(R.id.notifications_list);
        adapter = new NotificationsAdapter(tracsNotifications, this.getApplicationContext());
        notificationsList.setAdapter(adapter);
        notificationsList.setOnItemClickListener((parent, view, position, id) -> {
            TracsNotification notification = (TracsNotification) notificationsList.getAdapter().getItem(position);
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("url", notification.getUrl());
            startActivity(intent);
            Log.i(TAG, notification.getTitle());
        });
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
                    notification, headers, NotificationsActivity.this::onSiteNameReturned
            ), TAG);
        }
    }

    public void onSiteNameReturned(JsonObject siteInfo) {
        for (TracsAppNotification notification : tracsNotifications) {
            try {
                TracsNotification tracsNotification = TracsNotification.class.cast(notification);
                boolean titleIsSet = !TracsNotification.NOT_SET.equals(tracsNotification.getSiteName());
                if (titleIsSet) {
                    this.notificationsRetrieved += 1;
                }

                String siteId = tracsNotification.getSiteId();
                String fetchedSiteId = siteInfo.get("entityId").getAsString();
                String siteName = siteInfo.get("entityTitle").getAsString();

                if (fetchedSiteId != null && fetchedSiteId.equals(siteId)) {
                    tracsNotification.setSiteName(siteName);
                    JsonArray sitePages = siteInfo.get("sitePages").getAsJsonArray();
                    for (JsonElement page : sitePages) {
                        JsonObject jsonPage = page.getAsJsonObject();
                        String pageName = jsonPage.get("title").getAsString();
                        if (pageName.toLowerCase().contains(NotificationTypes.ANNOUNCEMENT)) {
                            tracsNotification.setPageId(jsonPage.get("id").getAsString());
                        }
                    }
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
}
