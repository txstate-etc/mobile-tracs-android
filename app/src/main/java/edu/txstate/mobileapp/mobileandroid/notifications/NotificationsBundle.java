package edu.txstate.mobileapp.mobileandroid.notifications;

import java.util.ArrayList;

/**
 * A container for multiple Notifications
 */
public class NotificationsBundle {
    private static final String TAG = "NotificationsBundle";
    private ArrayList<TracsAppNotification> allNotifications;

    /**
     * Creates a new empty notifications container
     */
    public NotificationsBundle() {
        this.allNotifications = new ArrayList<>();
    }

    public void addOne(TracsAppNotification notification) {
        this.allNotifications.add(notification);
    }

    public void addMany(ArrayList<TracsAppNotification> notifications) {
        this.allNotifications.addAll(notifications);
    }

    public int size() {
        return allNotifications.size();
    }
}
