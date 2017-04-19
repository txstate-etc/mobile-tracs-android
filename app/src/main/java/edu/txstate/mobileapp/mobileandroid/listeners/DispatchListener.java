package edu.txstate.mobileapp.mobileandroid.listeners;

import edu.txstate.mobileapp.mobileandroid.notifications.NotificationsBundle;

public interface DispatchListener extends RequestListener {
    void onRequestReturned(NotificationsBundle notifications);
}
