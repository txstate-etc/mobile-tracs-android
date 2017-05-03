package edu.txstate.mobileapp.tracscompanion.util;

import android.content.Intent;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import edu.txstate.mobileapp.tracscompanion.NotificationsActivity;

public class NotificationService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Intent intent = new Intent(this, NotificationsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }
}
