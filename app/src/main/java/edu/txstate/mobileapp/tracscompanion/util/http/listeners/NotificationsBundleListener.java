package edu.txstate.mobileapp.tracscompanion.util.http.listeners;

import com.android.volley.Response;

import edu.txstate.mobileapp.tracscompanion.notifications.NotificationsBundle;


public interface NotificationsBundleListener extends Response.Listener<NotificationsBundle> {
    void onResponse(NotificationsBundle response);
}
