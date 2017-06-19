package edu.txstate.mobile.tracs.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import edu.txstate.mobile.tracs.AnalyticsApplication;
import edu.txstate.mobile.tracs.NotificationsActivity;
import edu.txstate.mobile.tracs.R;

public class NotificationService extends FirebaseMessagingService {

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Intent userClicksNotification = new Intent(this, NotificationsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                userClicksNotification,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(AnalyticsApplication.getContext().getResources().getColor(R.color.notificationColor))
                .setContentTitle(remoteMessage.getNotification().getTitle())
                .setContentText(remoteMessage.getNotification().getBody())
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setDefaults(Notification.DEFAULT_SOUND)
                .build();

        Intent newNotificationIntent = new Intent("badge_count");
        this.sendBroadcast(newNotificationIntent);
        notificationManager.notify(remoteMessage.getNotification().hashCode(), notification);
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }
}
