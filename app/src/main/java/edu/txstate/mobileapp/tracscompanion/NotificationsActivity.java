package edu.txstate.mobileapp.tracscompanion;

import android.app.ProgressDialog;
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
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import edu.txstate.mobileapp.tracscompanion.notifications.NotificationTypes;
import edu.txstate.mobileapp.tracscompanion.notifications.NotificationsBundle;
import edu.txstate.mobileapp.tracscompanion.notifications.NotificationsAdapter;
import edu.txstate.mobileapp.tracscompanion.notifications.TracsAppNotification;
import edu.txstate.mobileapp.tracscompanion.notifications.tracs.TracsNotification;
import edu.txstate.mobileapp.tracscompanion.util.IntegrationServer;
import edu.txstate.mobileapp.tracscompanion.util.TracsClient;
import edu.txstate.mobileapp.tracscompanion.util.http.HttpQueue;
import edu.txstate.mobileapp.tracscompanion.util.http.requests.TracsSiteRequest;

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
        this.tracsNotifications = new NotificationsBundle();

        loadingDialog = new ProgressDialog(this);
        loadingDialog.setMessage("Loading Notifications...");

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
        if (showDialog) { loadingDialog.show(); }
        this.tracsNotifications = new NotificationsBundle();
        this.tracsNotifications.addObserver(this);
        IntegrationServer.getInstance()
                .getDispatchNotifications(NotificationsActivity.this::onResponse, AnalyticsApplication.getContext());
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
        this.notificationsRetrieved += 1;
        boolean done = this.allRequestsBack();
        if (response.getType().equals(NotificationTypes.ERROR)) {
            if (done) {
                loadingDialog.dismiss();
                Log.wtf(TAG, "Error retrieving notifications.");
                displayListView();
            }
        } else {
            this.tracsNotifications.addOne(response);
            if (done) {
                this.notificationsRetrieved = 0;
                this.displayListView();
            }
        }
    }

    private boolean allRequestsBack() {
        return this.notificationsRetrieved >= this.dispatchNotifications.size();
    }

    private void displayListView() {
        loadingDialog.dismiss();
        refreshLayout.setRefreshing(false);
        final ListView notificationsList = (ListView) findViewById(R.id.notifications_list);
        adapter = new NotificationsAdapter(tracsNotifications, this.getApplicationContext());
        notificationsList.setAdapter(adapter);
    }

    private void updateListView() {
        adapter.notifyDataSetChanged();
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
            ));
        }
    }

    public void onSiteNameReturned(Map<String, String> siteNameAndId) {
        for (TracsAppNotification notification : tracsNotifications) {
            try {
                String siteId = TracsNotification.class.cast(notification).getSiteId();
                String siteName = siteNameAndId.get(siteId);
                if (siteName != null) {
                    TracsNotification.class.cast(notification).setSiteName(siteName);
                }
            } catch (NullPointerException | ClassCastException e) {
                Log.wtf(TAG, "Could not set site name.");
            }
        }

        updateListView();
    }
}
