package edu.txstate.mobile.tracs.util;

import android.content.Context;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.HashMap;
import java.util.Map;

import edu.txstate.mobile.tracs.AnalyticsApplication;
import edu.txstate.mobile.tracs.R;
import edu.txstate.mobile.tracs.notifications.DispatchNotification;
import edu.txstate.mobile.tracs.notifications.NotificationTypes;
import edu.txstate.mobile.tracs.notifications.NotificationsBundle;
import edu.txstate.mobile.tracs.notifications.TracsAppNotification;
import edu.txstate.mobile.tracs.notifications.tracs.TracsNotification;
import edu.txstate.mobile.tracs.notifications.tracs.TracsNotificationError;
import edu.txstate.mobile.tracs.util.http.HttpQueue;
import edu.txstate.mobile.tracs.util.http.requests.TracsLoginRequest;
import edu.txstate.mobile.tracs.util.http.requests.TracsNotificationRequest;
import edu.txstate.mobile.tracs.util.http.requests.TracsSessionRequest;
import edu.txstate.mobile.tracs.util.http.responses.TracsSession;

public class TracsClient {
    private static final String TAG = "TracsClient";
    private static final String TRACS_URL = AnalyticsApplication.getContext().getString(R.string.tracs_base);
    private static final String TRACS_BASE = TRACS_URL + "/direct";
    private static final String ANNOUNCEMENT_URL = TRACS_URL + AnalyticsApplication.getContext().getString(R.string.tracs_announcement);
    private static final String PORTAL_URL = TRACS_URL + "/portal";
    private static final String SITE_URL = PORTAL_URL + "/site/";
    public static final String SESSION_URL = TRACS_URL + AnalyticsApplication.getContext().getString(R.string.tracs_session_login);

    private static TracsClient tracsClient;

    private Response.Listener<String> loginListener;

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
                                 Response.Listener<TracsNotification> listener,
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
            String entityId = tracsNotification.getSiteId() + ":main:" + tracsNotification.getObjectId();
            String url = makeUrl(notification.getType()) + entityId + ".json";
            Map<String, String> headers = new HashMap<>();
            Response.ErrorListener errorHandler = error -> listener.onResponse(new TracsNotificationError(
                    error.networkResponse.statusCode
            ));

            requestQueue.addToRequestQueue(new TracsNotificationRequest(
                    url, headers, tracsNotification, listener, errorHandler), TAG);
        }

    }

    public void verifySession (Response.Listener<String> listener) {
        this.loginListener = listener;
        HttpQueue requestQueue = HttpQueue.getInstance(AnalyticsApplication.getContext());
        Map<String, String> headers = new HashMap<>();
        requestQueue.addToRequestQueue(new TracsSessionRequest(headers,
                TracsClient.this::onSessionReturned,
                TracsClient.this::onStatusError), TAG);
    }

    private void login () {
        HttpQueue requestQueue = HttpQueue.getInstance(AnalyticsApplication.getContext());
        requestQueue.addToRequestQueue(new TracsLoginRequest(
                SESSION_URL, this.loginListener, TracsClient.this::onStatusError), TAG);
    }

    private void onSessionReturned(TracsSession session) {
        Context context = AnalyticsApplication.getContext();
        String currentUser = AppStorage.get(AppStorage.USERNAME, context);
        if (session.hasNetId() && session.getUserEid().equals(currentUser)) {
            AppStorage.put(AppStorage.SESSION_ID, session.getSessionId(), context);
            this.loginListener.onResponse(session.getSessionId());
        } else if (AppStorage.credentialsAreStored(context)) {
            login();
        } else {
            resetLoginState();
        }
    }

    private void onStatusError(VolleyError error) {
        Log.wtf(TAG, new String(error.networkResponse.data));
        resetLoginState();
    }

    private void resetLoginState() {
        LoginStatus.getInstance().logout();
        this.loginListener.onResponse(null);
    }
}
