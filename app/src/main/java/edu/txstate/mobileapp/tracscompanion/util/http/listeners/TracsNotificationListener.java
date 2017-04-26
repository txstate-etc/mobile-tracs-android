package edu.txstate.mobileapp.tracscompanion.util.http.listeners;

import com.android.volley.Response;

import edu.txstate.mobileapp.tracscompanion.notifications.tracs.TracsNotification;

public interface TracsNotificationListener extends Response.Listener<TracsNotification> {
    void onResponse(TracsNotification response);
}
