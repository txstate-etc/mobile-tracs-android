package edu.txstate.mobile.tracs.util;

import android.content.Context;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.JsonObject;

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
import edu.txstate.mobile.tracs.notifications.util.NotificationData;
import edu.txstate.mobile.tracs.notifications.util.SiteSet;
import edu.txstate.mobile.tracs.util.http.HttpQueue;
import edu.txstate.mobile.tracs.util.http.listeners.LoginListener;
import edu.txstate.mobile.tracs.util.http.listeners.NotificationDataListener;
import edu.txstate.mobile.tracs.util.http.requests.TracsLoginRequest;
import edu.txstate.mobile.tracs.util.http.requests.TracsNotificationRequest;
import edu.txstate.mobile.tracs.util.http.requests.TracsPageIdRequest;
import edu.txstate.mobile.tracs.util.http.requests.TracsSessionRequest;
import edu.txstate.mobile.tracs.util.http.requests.TracsSiteRequest;
import edu.txstate.mobile.tracs.util.http.responses.TracsSession;

public class TracsClient {
    private static final String TAG = "TracsClient";
    private static final String TRACS_URL = AnalyticsApplication.getContext().getString(R.string.tracs_base);
    private static final String TRACS_BASE = TRACS_URL + "direct";
    private static final String ANNOUNCEMENT_URL = TRACS_URL + AnalyticsApplication.getContext().getString(R.string.tracs_announcement);
    private static final String PORTAL_URL = TRACS_URL + "portal";
    private static final String SITE_URL = PORTAL_URL + "/pda/";

    private static TracsClient tracsClient;

    private HttpQueue queue = HttpQueue.getInstance(AnalyticsApplication.getContext());

    private LoginListener loginListener;
    private NotificationDataListener dataListener;
    private SiteSet siteData;

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
                Log.e(TAG, e.getMessage());
                continue;
            }

            HttpQueue requestQueue = HttpQueue.getInstance(context);
            String entityId = "";
            switch (tracsNotification.getType()) {
                case NotificationTypes.ANNOUNCEMENT:
                    entityId = tracsNotification.getSiteId() + ":main:" + tracsNotification.getObjectId();
                    break;
                case NotificationTypes.DISCUSSION:
                    entityId = tracsNotification.getObjectId();
                    break;
                default:
            }
            String url = makeUrl(notification.getType()) + entityId + ".json";
            Map<String, String> headers = new HashMap<>();

            Response.ErrorListener errorHandler = (error) -> {
                TracsNotificationError errorNotification;
                if (error != null && error.networkResponse != null) {
                    errorNotification = new TracsNotificationError(
                            error.networkResponse.statusCode
                    );
                } else {
                    errorNotification = new TracsNotificationError(404);
                }
                errorNotification.setDispatchId(tracsNotification.getDispatchId());
                listener.onResponse(errorNotification);
            };

            requestQueue.addToRequestQueue(new TracsNotificationRequest(
                    url, headers, tracsNotification, listener, errorHandler), this);
        }

    }

    public void getSiteData(SiteSet siteData, NotificationDataListener listener) {
        this.dataListener = listener;
        this.siteData = siteData;

        for (NotificationData data : siteData) {
            TracsSiteRequest siteRequest = new TracsSiteRequest(data.getSiteId(),
                    this::onSiteNameReturned, this::onDataError);
            queue.addToRequestQueue(siteRequest, data.getSiteId());

            TracsPageIdRequest pageIdRequest = new TracsPageIdRequest(data.getSiteId(),
                    this::onPageIdsReturned, this::onDataError);
            queue.addToRequestQueue(pageIdRequest, data.getSiteId());
        }
    }

    private void onDataError(String siteId) {
        NotificationData data = this.siteData.get(siteId);
        queue.getRequestQueue().cancelAll(siteId);
        if (data != null) {
            this.siteData.remove(data);
            if (siteDataHasBeenFetched()) {
                this.dataListener.onResponse(this.siteData);
            }
        }
    }

    private void onSiteNameReturned(JsonObject site) {
        String siteId = site.get("entityId").getAsString();
        String siteName = site.get("entityTitle").getAsString();
        NotificationData data = this.siteData.get(siteId);
        if (data != null) {
            data.setSiteName(siteName);
            if (siteDataHasBeenFetched()) {
                this.dataListener.onResponse(this.siteData);
            }
        }
    }

    private void onPageIdsReturned(JsonObject pageIds) {
        NotificationData data = this.siteData.get(pageIds.get("siteId").getAsString());
        if (data != null) {
            if (pageIds.has(NotificationTypes.ANNOUNCEMENT)) {
                data.setAnnouncementPageId(pageIds.get(NotificationTypes.ANNOUNCEMENT).getAsString());
            } else {
                data.setAnnouncementPageId(null);
            }
            if (pageIds.has(NotificationTypes.DISCUSSION)) {
                data.setDiscussionPageId(pageIds.get(NotificationTypes.DISCUSSION).getAsString());
            } else {
                data.setDiscussionPageId(null);
            }
            if (siteDataHasBeenFetched()) {
                this.dataListener.onResponse(this.siteData);
            }
        }
    }

    private boolean siteDataHasBeenFetched() {
        return this.siteData.isComplete();
    }

    public void verifySession(LoginListener listener) {
        this.loginListener = listener;
        Map<String, String> headers = new HashMap<>();
        queue.addToRequestQueue(new TracsSessionRequest(headers,
                TracsClient.this::onSessionReturned,
                TracsClient.this::onStatusError), this);
    }

    private void login() {
        TracsLoginRequest.execute(this.loginListener);
    }

    private void onSessionReturned(TracsSession session) {
        Context context = AnalyticsApplication.getContext();
        String currentUser = AppStorage.get(AppStorage.USERNAME, context);
        if (session.hasNetId() && session.getUserEid().equals(currentUser)) {
            this.loginListener.onResponse(AppStorage.get(AppStorage.SESSION_ID, context));
        } else if (AppStorage.credentialsAreStored(context)) {
            login();
        } else {
            resetLoginState();
        }
    }

    private void onStatusError(VolleyError error) {
        Log.e(TAG, "Error verifying session: no token retrieved");
        resetLoginState();
    }

    private void resetLoginState() {
        LoginStatus.getInstance().logout();
        this.loginListener.onResponse(null);
    }
}
