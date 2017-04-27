package edu.txstate.mobileapp.tracscompanion.util;

import android.content.Context;

import com.android.volley.Response;

import java.util.HashMap;
import java.util.Map;

import edu.txstate.mobileapp.tracscompanion.notifications.NotificationTypes;
import edu.txstate.mobileapp.tracscompanion.notifications.NotificationsBundle;
import edu.txstate.mobileapp.tracscompanion.notifications.TracsAppNotification;
import edu.txstate.mobileapp.tracscompanion.notifications.tracs.TracsNotificationError;
import edu.txstate.mobileapp.tracscompanion.util.http.HttpQueue;
import edu.txstate.mobileapp.tracscompanion.util.http.listeners.TracsNotificationListener;
import edu.txstate.mobileapp.tracscompanion.util.http.requests.TracsLoginRequest;
import edu.txstate.mobileapp.tracscompanion.util.http.requests.TracsNotificationRequest;

public class TracsClient {
    private static final String TAG = "TracsClient";
    private static final String TRACS_URL = "https://tracs.txstate.edu";
    private static final String TRACS_BASE = TRACS_URL + "/direct";
    private static final String ANNOUNCEMENT_URL = TRACS_BASE + "/announcement/";
    static final String SITE_URL = TRACS_BASE + "/site";
    static final String PORTAL_URL = TRACS_URL + "/portal";
    public static final String LOGIN_URL = TRACS_URL + "/session";
    static final String LOGOUT_URL = TRACS_URL + "/portal/pda/?force.logout=yes";

    private static TracsClient tracsClient;

    private TracsClient() {
    }

    public static TracsClient getInstance() {
        if (tracsClient == null) {
            tracsClient = new TracsClient();
        }
        return tracsClient;
    }

    public static String makeUrl() {
        return TRACS_BASE;
    }

    public static String makeUrl(String type) {
        String desiredUrl;
        switch (type) {
            case NotificationTypes.ANNOUNCEMENT:
                desiredUrl = ANNOUNCEMENT_URL;
                break;
            case NotificationTypes.ASSESSMENT:
                desiredUrl = TRACS_BASE;
                break;
            case NotificationTypes.ASSIGNMENT:
                desiredUrl = TRACS_BASE;
                break;
            case NotificationTypes.DISCUSSION:
                desiredUrl = TRACS_BASE;
                break;
            case NotificationTypes.GRADE:
                desiredUrl = TRACS_BASE;
                break;
            default:
                desiredUrl = TRACS_BASE;
        }
        return desiredUrl;
    }

    public void getNotifications(NotificationsBundle notifications,
                                 TracsNotificationListener listener,
                                 Context context) {

        for (TracsAppNotification notification : notifications) {
            HttpQueue requestQueue = HttpQueue.getInstance(context);
            String url = makeUrl(notification.getType());
            Map<String, String> headers = new HashMap<>();
            Response.ErrorListener errorHandler = error -> listener.onResponse(new TracsNotificationError(
                    error.networkResponse.statusCode
            ));

            requestQueue.addToRequestQueue(new TracsNotificationRequest(
                    url, headers,
                    listener, errorHandler));
        }
    }

    private void login(Context context, Response.Listener<String> listener) {
        HttpQueue.getInstance(context).addToRequestQueue(new TracsLoginRequest(
                LOGIN_URL, listener));
    }
}
