package edu.txstate.mobileapp.tracscompanion.listeners;

import edu.txstate.mobileapp.tracscompanion.notifications.NotificationsBundle;

public interface DispatchListener extends RequestListener {
    void onRequestReturned(NotificationsBundle notifications);
}
