package edu.txstate.mobileapp.tracscompanion.listeners;

import edu.txstate.mobileapp.tracscompanion.notifications.tracs.TracsNotification;

public interface TracsListener extends RequestListener {
    void onRequestReturned(TracsNotification notification);
}
