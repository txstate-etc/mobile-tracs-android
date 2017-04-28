package edu.txstate.mobileapp.tracscompanion;

import android.app.ProgressDialog;
import android.os.Bundle;
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

import java.util.ArrayList;

import edu.txstate.mobileapp.tracscompanion.notifications.NotificationTypes;
import edu.txstate.mobileapp.tracscompanion.notifications.NotificationsBundle;
import edu.txstate.mobileapp.tracscompanion.notifications.NotificationsListLoader;
import edu.txstate.mobileapp.tracscompanion.notifications.tracs.TracsNotification;
import edu.txstate.mobileapp.tracscompanion.util.IntegrationServer;
import edu.txstate.mobileapp.tracscompanion.util.TracsClient;

public class NotificationsActivity
        extends AppCompatActivity {
    private static final String TAG = "NotificationsActivity";
    private static final String SCREEN_NAME = "Notifications";
    private NotificationsBundle tracsNotifications;
    private NotificationsBundle dispatchNotifications;
    private int notificationsRetrieved;
    private ProgressDialog loadingDialog;
    private Tracker analyticsTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        analyticsTracker = AnalyticsApplication.class.cast(getApplication()).getDefaultTracker();

        loadingDialog = new ProgressDialog(this);
        this.tracsNotifications = new NotificationsBundle();
        loadingDialog.setMessage("Loading NotificationsBundle...");
        loadingDialog.show();
        setContentView(R.layout.activity_notifications);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            return;
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        IntegrationServer.getInstance()
                .getDispatchNotifications(NotificationsActivity.this::onResponse, getApplicationContext());
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
        refreshButton.setVisible(true);
        return true;
    }

    protected void onResponse(NotificationsBundle response) {
        if (response.size() == 0) { return; }
        this.dispatchNotifications = response;
        TracsClient tracs = TracsClient.getInstance();
        tracs.getNotifications(response, NotificationsActivity.this::onResponse, getApplicationContext());
    }

    public void onResponse(TracsNotification response) {
        this.notificationsRetrieved += 1;
        boolean done = this.allRequestsBack();
        if (response.getType().equals(NotificationTypes.ERROR)) {
            if (done) {
                loadingDialog.dismiss();
                //Could have valid notifications available to view
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
        final ListView notificationsList = (ListView) findViewById(R.id.notifications_list);
        final NotificationsListLoader adapter = new NotificationsListLoader(this,
                android.R.layout.simple_list_item_1,
                tracsNotifications);
        notificationsList.setAdapter(adapter);
    }
}
