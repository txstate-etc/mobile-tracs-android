package edu.txstate.mobileapp.mobileandroid;

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
    private static final String TAG = "NotificationsActvitiy";
    private NotificationsBundle tracsNotificationsBundle;
    private NotificationsBundle dispatchNotifications;

    //FIXME: remove after testing
    private String entityId = "831342dd-fdb6-4878-8b3c-1d29ecb06a14:main:aa4f8f85-a645-4766-bc91-1a1c7bef93df";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            return;
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        IntegrationServer.getInstance()
                .getDispatchNotifications(this, this, "ajt79");
        Log.d(TAG, "Request is out");
        TracsClient tracs = TracsClient.getInstance();
        tracs.getAnnouncement(entityId, this);
        //TODO: set up for display of notifications fetched from Tracs (after all async calls)
    }

    @Override
    public void onNotificationAvailable(TracsNotification announcement) {
        if (announcement.isNull()) { return; }
        this.tracsNotificationsBundle.addOne(announcement);
    }

    @Override
    public void onNotificationAvailable(NotificationsBundle notifications) {
        if (notifications.size() == 0) { return; }
        this.dispatchNotifications = notifications;
    }
}
