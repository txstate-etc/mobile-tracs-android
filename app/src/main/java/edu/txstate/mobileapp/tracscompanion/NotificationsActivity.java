package edu.txstate.mobileapp.tracscompanion;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ListView;

import com.android.volley.Response;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.securepreferences.SecurePreferences;

import edu.txstate.mobileapp.tracscompanion.listeners.RequestListener;
import edu.txstate.mobileapp.tracscompanion.notifications.NotificationsBundle;
import edu.txstate.mobileapp.tracscompanion.notifications.NotificationsListLoader;
import edu.txstate.mobileapp.tracscompanion.listeners.DispatchListener;
import edu.txstate.mobileapp.tracscompanion.listeners.TracsListener;
import edu.txstate.mobileapp.tracscompanion.notifications.tracs.TracsAnnouncement;
import edu.txstate.mobileapp.tracscompanion.notifications.tracs.TracsNotification;
import edu.txstate.mobileapp.tracscompanion.util.AppStorage;
import edu.txstate.mobileapp.tracscompanion.util.IntegrationServer;
import edu.txstate.mobileapp.tracscompanion.util.TracsClient;
import edu.txstate.mobileapp.tracscompanion.util.http.listeners.NotificationsBundleListener;
import edu.txstate.mobileapp.tracscompanion.util.http.listeners.TracsNotificationListener;

public class NotificationsActivity
        extends AppCompatActivity {
    private static final String TAG = "NotificationsActivity";
    private static final String SCREEN_NAME = "Notifications";
    private NotificationsBundle tracsNotificationsBundle;
    private NotificationsBundle dispatchNotifications;
    private int notificationsRetrieved;
    private ProgressDialog loadingDialog;
    private Tracker analyticsTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        analyticsTracker = AnalyticsApplication.class.cast(getApplication()).getDefaultTracker();

        loadingDialog = new ProgressDialog(this);
        this.tracsNotificationsBundle = new NotificationsBundle();
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

    protected void onResponse(NotificationsBundle response) {
        if (response.size() == 0) { return; }
        this.dispatchNotifications = response;
        TracsClient tracs = TracsClient.getInstance();
        tracs.getNotifications(response, NotificationsActivity.this::onResponse, getApplicationContext());
    }

    public void onResponse(TracsNotification response) {
        if (response.isNull()) {
            return;
        }

        boolean allNotificationsRetrieved;
        this.tracsNotificationsBundle.addOne(response);
        this.notificationsRetrieved += 1;
        allNotificationsRetrieved = this.notificationsRetrieved >= this.dispatchNotifications.size();

        if (allNotificationsRetrieved) {
            this.notificationsRetrieved = 0;
            loadingDialog.dismiss();

            final ListView notificationsList = (ListView) findViewById(R.id.notifications_list);
            final NotificationsListLoader adapter = new NotificationsListLoader(this,
                    android.R.layout.simple_list_item_1,
                    tracsNotificationsBundle);
            notificationsList.setAdapter(adapter);
        }
    }
}
