package edu.txstate.mobile.tracs.util;

public class NotificationStatus {
    private boolean seen;
    private boolean read;
    private boolean cleared;

    public NotificationStatus() {
        this(false, false, false);
    }

    public NotificationStatus(boolean seen) {
        this(seen, false, false);
    }

    public NotificationStatus(boolean seen, boolean read) {
        this(seen, read, false);
    }

    public NotificationStatus(boolean seen, boolean read, boolean cleared) {
        this.seen = seen;
        this.read = read;
        this.cleared = cleared;
    }

    public boolean hasBeenSeen() {
        return this.seen;
    }

    public boolean hasBeenRead() {
        return this.read;
    }

    public boolean hasBeenCleared() {
        return this.cleared;
    }
}
