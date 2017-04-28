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
import android.view.View;
import android.widget.ListView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import java.util.ArrayList;

import edu.txstate.mobileapp.tracscompanion.notifications.NotificationTypes;
import edu.txstate.mobileapp.tracscompanion.notifications.NotificationsBundle;
import edu.txstate.mobileapp.tracscompanion.notifications.NotificationsListLoader;
import edu.txstate.mobileapp.tracscompanion.notifications.tracs.TracsNotification;
import edu.txstate.mobileapp.tracscompanion.util.IntegrationServer;
import edu.txstate.mobileapp.tracscompanion.util.TracsClient;

public class NotificationsActivity
        extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "NotificationsActivity";
    private static final String SCREEN_NAME = "Notifications";
    private NotificationsBundle tracsNotifications;
    private NotificationsBundle dispatchNotifications;
    private int notificationsRetrieved;
    private ProgressDialog loadingDialog;
    private Tracker analyticsTracker;
    private SwipeRefreshLayout refreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        analyticsTracker = AnalyticsApplication.class.cast(getApplication()).getDefaultTracker();


        this.tracsNotifications = new NotificationsBundle();
        loadingDialog = new ProgressDialog(this);
        loadingDialog.setMessage("Loading Notifications...");
        setContentView(R.layout.activity_notifications);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            return;
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.notification_swipe_refresh);
        refreshLayout.setOnRefreshListener(NotificationsActivity.this);
        refreshLayout.setColorSchemeResources(R.color.colorPrimary);
        refreshNotifications(true);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "Activity has resumed");
        analyticsTracker.setScreenName(SCREEN_NAME);
        analyticsTracker.send(new HitBuilders.ScreenViewBuilder().build());
        super.onResume();
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
        final NotificationsListLoader adapter = new NotificationsListLoader(this,
                android.R.layout.simple_list_item_1,
                tracsNotifications);
        notificationsList.setAdapter(adapter);
    }
}
