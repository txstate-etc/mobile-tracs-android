package edu.txstate.mobileapp.mobileandroid;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.net.TrafficStatsCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.txstate.mobileapp.mobileandroid.notifications.NotificationsBundle;
import edu.txstate.mobileapp.mobileandroid.notifications.NotificationsListLoader;
import edu.txstate.mobileapp.mobileandroid.notifications.TracsAppNotification;
import edu.txstate.mobileapp.mobileandroid.notifications.tracs.TracsNotification;
import edu.txstate.mobileapp.mobileandroid.notifications.listeners.NotificationListener;
import edu.txstate.mobileapp.mobileandroid.util.IntegrationServer;
import edu.txstate.mobileapp.mobileandroid.util.TracsClient;

public class NotificationsActivity
        extends AppCompatActivity
        implements NotificationListener {
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

            final ListView notificationsList = (ListView) findViewById(R.id.notifications_list);
            final NotificationsListLoader adapter = new NotificationsListLoader(this,
                    android.R.layout.simple_list_item_1,
                    tracsNotificationsBundle);
            notificationsList.setAdapter(adapter);

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
