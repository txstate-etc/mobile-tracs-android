package edu.txstate.mobileapp.tracscompanion.util;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.HashMap;
import java.util.Map;

import edu.txstate.mobileapp.tracscompanion.notifications.DispatchNotification;
import edu.txstate.mobileapp.tracscompanion.notifications.NotificationTypes;
import edu.txstate.mobileapp.tracscompanion.notifications.NotificationsBundle;
import edu.txstate.mobileapp.tracscompanion.notifications.TracsAppNotification;
import edu.txstate.mobileapp.tracscompanion.listeners.TracsListener;
import edu.txstate.mobileapp.tracscompanion.requests.AsyncTaskFactory;
import edu.txstate.mobileapp.tracscompanion.requests.Task;
import edu.txstate.mobileapp.tracscompanion.util.http.HttpQueue;
import edu.txstate.mobileapp.tracscompanion.util.http.listeners.TracsNotificationListener;
import edu.txstate.mobileapp.tracscompanion.util.http.requests.DispatchNotificationRequest;
import edu.txstate.mobileapp.tracscompanion.util.http.requests.TracsNotificationRequest;

public class TracsClient {
    private static final String TAG = "TracsClient";
    private static final String tracsUrl = "https://tracs.txstate.edu";
    private static final String tracsBase = tracsUrl + "/direct";
    private static final String announcementUrl = tracsBase + "/announcement/";
    static final String siteUrl = tracsBase + "/site";
    static final String portalUrl = tracsUrl + "/portal";
    static final String loginUrl = tracsUrl + "/portal/login";
    static final String logoutUrl = tracsUrl + "/portal/pda/?force.logout=yes";

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
        return tracsBase;
    }

    public static String makeUrl(String type) {
        String desiredUrl;
        switch (type) {
            case NotificationTypes.ANNOUNCEMENT:
                desiredUrl = announcementUrl;
                break;
            case NotificationTypes.ASSESSMENT:
                desiredUrl = tracsBase;
                break;
            case NotificationTypes.ASSIGNMENT:
                desiredUrl = tracsBase;
                break;
            case NotificationTypes.DISCUSSION:
                desiredUrl = tracsBase;
                break;
            case NotificationTypes.GRADE:
                desiredUrl = tracsBase;
                break;
            default:
                desiredUrl = tracsBase;
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
            Response.ErrorListener errorHandler = error -> Log.wtf(TAG, error.getMessage());

            requestQueue.addToRequestQueue(new TracsNotificationRequest(
                    url, headers,
                    listener, errorHandler));
        }
    }
}
