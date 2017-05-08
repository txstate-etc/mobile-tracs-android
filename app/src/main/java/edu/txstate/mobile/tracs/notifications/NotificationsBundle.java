package edu.txstate.mobile.tracs.notifications;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;

/**
 * A container for multiple Notifications
 */
public class NotificationsBundle extends Observable implements Iterable<TracsAppNotification> {
    private static final String TAG = "NotificationsBundle";
    private ArrayList<TracsAppNotification> allNotifications;
    private Map<String, Integer> notificationsCount;

    /**
     * Creates a new empty notifications container
     */
    public NotificationsBundle() {
        this.allNotifications = new ArrayList<>();
        this.notificationsCount = new HashMap<>();
    }

    public void addOne(TracsAppNotification notification) {
        this.incrementCount(notification.getType());
        this.allNotifications.add(notification);
        this.setChanged();
        this.notifyObservers(notification);
        this.clearChanged();
    }

    public void remove(String id) {
        if (id == null) { return; }
        for(int i = 0; i < allNotifications.size(); i++) {
            if (id.equals(allNotifications.get(i).getId())){
                String type = allNotifications.get(i).getType();
                this.notificationsCount.put(type, this.notificationsCount.get(type)-1);
                allNotifications.remove(i);
                return;
            }
        }
    }

    public int size() {
        return allNotifications.size();
    }

    public int countAnnouncements() {
        return this.notificationsCount.get(NotificationTypes.ANNOUNCEMENT);
    }

    public ArrayList<TracsAppNotification> getAllNotifications() {
        return this.allNotifications;
    }

    private void announceUpdate() {
        this.setChanged();
        this.notifyObservers();
        this.clearChanged();
    }

    @NonNull
    public NotificationsBundleIterator iterator() {
        return new NotificationsBundleIterator(allNotifications.size());
    }

    public int getNotificationCount(String type) {
        Integer total = this.notificationsCount.get(type);
        return total == null ? 0 : total;
    }

    public TracsAppNotification get(int position) {
        if (position < this.allNotifications.size()) {
            return this.allNotifications.get(position);
        }
        return null;
    }

    private void incrementCount(String type) {
        boolean typeExists = this.notificationsCount.containsKey(type);
        int updatedTypeTotal = typeExists ? this.notificationsCount.get(type) + 1 : 1;
        this.notificationsCount.put(type, updatedTypeTotal);
    }

    private class NotificationsBundleIterator implements Iterator<TracsAppNotification> {
        private int size;
        private int current;

        NotificationsBundleIterator(int size) {
            this.size = size;
        }

        @Override
        public boolean hasNext() {
            return current < size;
        }

        @Override
        public TracsAppNotification next() {
            return allNotifications.get(current++);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
