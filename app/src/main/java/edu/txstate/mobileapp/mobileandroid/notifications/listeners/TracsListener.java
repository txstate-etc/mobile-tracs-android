package edu.txstate.mobileapp.mobileandroid.notifications.listeners;

import edu.txstate.mobileapp.mobileandroid.notifications.tracs.TracsNotification;

public interface TracsListener extends RequestListener {
    void onRequestReturned(TracsNotification notification);
}
