package edu.txstate.mobileapp.mobileandroid;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import edu.txstate.mobileapp.mobileandroid.notifications.NotificationsBundle;
import edu.txstate.mobileapp.mobileandroid.notifications.tracs.TracsNotification;
import edu.txstate.mobileapp.mobileandroid.notifications.listeners.NotificationListener;
import edu.txstate.mobileapp.mobileandroid.util.IntegrationServer;
import edu.txstate.mobileapp.mobileandroid.util.TracsClient;

public class NotificationsActivity extends AppCompatActivity implements NotificationListener {
    private static final String TAG = "NotificationsActivity";
    private NotificationsBundle tracsNotificationsBundle;
    private NotificationsBundle dispatchNotifications;
    private int notificationsRetrieved;
    private ProgressDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                .getDispatchNotifications(this, "ajt79");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Activity has resumed");

    }

    @Override
    public void onNotificationAvailable(TracsNotification announcement) {
        if (announcement.isNull()) { return; }
        boolean allNotificationsRetrieved;
        this.tracsNotificationsBundle.addOne(announcement);
        this.notificationsRetrieved += 1;
        allNotificationsRetrieved = this.notificationsRetrieved >= this.dispatchNotifications.size();
        if (allNotificationsRetrieved) {
            this.notificationsRetrieved = 0;
            loadingDialog.dismiss();
        }
    }

    @Override
    public void onNotificationAvailable(NotificationsBundle notifications) {
        if (notifications.size() == 0) { return; }
        this.dispatchNotifications = notifications;
        TracsClient tracs = TracsClient.getInstance();
        tracs.getNotifications(dispatchNotifications, this);
    }
}
