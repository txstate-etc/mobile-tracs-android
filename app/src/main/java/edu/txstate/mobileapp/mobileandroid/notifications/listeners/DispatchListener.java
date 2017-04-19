package edu.txstate.mobileapp.mobileandroid.notifications.listeners;

import edu.txstate.mobileapp.mobileandroid.notifications.NotificationsBundle;

public interface DispatchListener extends RequestListener{
    void onRequestReturned(NotificationsBundle notifications);
}
