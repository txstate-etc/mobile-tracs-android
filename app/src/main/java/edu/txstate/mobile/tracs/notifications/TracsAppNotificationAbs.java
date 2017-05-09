package edu.txstate.mobile.tracs.notifications;

public abstract class TracsAppNotificationAbs implements TracsAppNotification {
    private boolean seen;
    private boolean read;
    private boolean cleared;


    public boolean hasBeenRead() {
        return this.read;
    }

    public boolean hasBeenSeen() {
        return this.seen;
    }

    public boolean hasBeenCleared() {
        return this.cleared;
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
