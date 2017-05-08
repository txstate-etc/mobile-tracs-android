package edu.txstate.mobile.tracs.util.http.listeners;

import com.android.volley.Response;

import edu.txstate.mobile.tracs.notifications.NotificationsBundle;


public interface NotificationsBundleListener extends Response.Listener<NotificationsBundle> {
    void onResponse(NotificationsBundle response);
}
