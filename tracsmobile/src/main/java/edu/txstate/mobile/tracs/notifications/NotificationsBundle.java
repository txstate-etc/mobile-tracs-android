package edu.txstate.mobile.tracs.notifications;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

    private Comparator<TracsAppNotification> comparator;
    /**
     * Creates a new empty notifications container
     */
    public NotificationsBundle() {
        this.allNotifications = new ArrayList<>();
        this.notificationsCount = new HashMap<>();
        comparator = (o1, o2) -> {
            if ( o1.getNotifyAfter().after(o2.getNotifyAfter()) ) {
                return -1;
            }
            if ( o1.getNotifyAfter().before(o2.getNotifyAfter()) ) {
                return 1;
            }
            return 0;
        };
    }

    public void add(TracsAppNotification notification) {
        this.incrementCount(notification.getType());
        this.allNotifications.add(notification);
        this.setChanged();
        this.notifyObservers(notification);
        this.clearChanged();
        Collections.sort(allNotifications, comparator);
    }

    public void remove(Object notification) {
        String type = ((TracsAppNotification) notification).getType();
        int newCount = notificationsCount.get(type) - 1;
        notificationsCount.put(type, newCount);
        this.allNotifications.remove(notification);
    }

    public void remove(String dispatchId) {
        if (dispatchId == null) { return; }
        for (int i = 0; i < this.allNotifications.size(); i++) {
            if (dispatchId.equals(this.allNotifications.get(i).getDispatchId())) {
                this.allNotifications.remove(i);
                break;
            }
        }
    }

    public void removeAll() {
        this.allNotifications.clear();
        notificationsCount.clear();
    }

    public int size() {
        return allNotifications.size();
    }

    public int totalUnread() {
        int unread = 0;
        for (TracsAppNotification notification : allNotifications) {
            if (!notification.hasBeenRead()) {
                unread += 1;
            }
        }
        return unread;
    }

    public int totalUnseen() {
        int unseen = 0;
        for (TracsAppNotification notification : allNotifications) {
            if (!notification.hasBeenSeen()) {
                unseen += 1;
            }
        }
        return unseen;
    }

    public ArrayList<TracsAppNotification> getAllNotifications() {
        return this.allNotifications;
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

    public TracsAppNotification get(String dispatchId) {
        if (dispatchId == null) { return null; }
        for (TracsAppNotification notification : allNotifications) {
            if (dispatchId.equals(notification.getDispatchId())) {
                return notification;
            }
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
