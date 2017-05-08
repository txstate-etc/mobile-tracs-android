package edu.txstate.mobile.tracs.util.http.listeners;

import com.android.volley.Response;

import edu.txstate.mobile.tracs.notifications.tracs.TracsNotification;

public interface TracsNotificationListener extends Response.Listener<TracsNotification> {
    void onResponse(TracsNotification response);
}
