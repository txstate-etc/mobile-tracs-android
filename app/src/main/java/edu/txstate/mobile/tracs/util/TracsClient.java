package edu.txstate.mobile.tracs.util;

import android.content.Context;
import android.util.Log;

import com.android.volley.Response;

import java.util.HashMap;
import java.util.Map;

import edu.txstate.mobile.tracs.notifications.DispatchNotification;
import edu.txstate.mobile.tracs.notifications.NotificationTypes;
import edu.txstate.mobile.tracs.notifications.NotificationsBundle;
import edu.txstate.mobile.tracs.notifications.TracsAppNotification;
import edu.txstate.mobile.tracs.notifications.tracs.TracsNotificationError;
import edu.txstate.mobile.tracs.util.http.HttpQueue;
import edu.txstate.mobile.tracs.util.http.listeners.TracsNotificationListener;
import edu.txstate.mobile.tracs.util.http.requests.TracsNotificationRequest;

public class TracsClient {
    private static final String TAG = "TracsClient";
    private static final String TRACS_URL = "https://tracs.txstate.edu";
    private static final String TRACS_BASE = TRACS_URL + "/direct";
    private static final String ANNOUNCEMENT_URL = TRACS_BASE + "/announcement/";
    private static final String PORTAL_URL = TRACS_URL + "/portal";
    private static final String SITE_URL = PORTAL_URL + "/site/";
    public static final String LOGIN_URL = TRACS_BASE + "/session";

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
            case "SITE":
                desiredUrl = SITE_URL;
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
            DispatchNotification tracsNotification;
            try {
                tracsNotification = (DispatchNotification) notification;
            } catch (ClassCastException e) {
                Log.wtf(TAG, e.getMessage());
                continue;
            }

            HttpQueue requestQueue = HttpQueue.getInstance(context);
            String url = makeUrl(notification.getType()) + tracsNotification.getObjectId() + ".json";
            Map<String, String> headers = new HashMap<>();
            Response.ErrorListener errorHandler = error -> listener.onResponse(new TracsNotificationError(
                    error.networkResponse.statusCode
            ));

            requestQueue.addToRequestQueue(new TracsNotificationRequest(
                    url, headers, listener, errorHandler), TAG);
        }

    }
}