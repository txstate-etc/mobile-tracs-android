package edu.txstate.mobile.tracs.notifications;

import java.util.Date;

public abstract class TracsAppNotificationAbs implements TracsAppNotification {
    private boolean seen;
    private boolean read;
    private boolean cleared;
    private String dispatchId;
    private Date notifyAfter;

    private static final String TAG = "TracsAppNotificationAbs";

    public String getDispatchId() {
        return this.dispatchId;
    }

    @Override
    public boolean hasBeenRead() {
        return this.read;
    }

    @Override
    public boolean hasBeenSeen() {
        return this.seen;
    }

    public boolean hasBeenCleared() {
        return this.cleared;
    }

    public void setDispatchId(String dispatchId) {
        this.dispatchId = dispatchId;
    }

    public void setNotifyAfter(Date notifyAfter) {
        this.notifyAfter = notifyAfter;
    }

    public Date getNotifyAfter() {
        return this.notifyAfter;
    }

    @Override
    public void markSeen(boolean seen) {
        this.seen = seen;
    }

    @Override
    public void markRead(boolean read) {
        this.read = read;
    }

    @Override
    public void markCleared(boolean cleared) {
        this.cleared = cleared;
    }
}
