package edu.txstate.mobileapp.mobileandroid.notifications.listeners;

import edu.txstate.mobileapp.mobileandroid.notifications.NotificationsBundle;
import edu.txstate.mobileapp.mobileandroid.notifications.tracs.TracsNotification;

public interface NotificationListener {
    void onNotificationAvailable(TracsNotification notification);
    void onNotificationAvailable(NotificationsBundle notifications);
}
