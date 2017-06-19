package edu.txstate.mobile.tracs.util.http.requests;

import android.annotation.SuppressLint;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.JsonObject;
import com.google.gson.JsonStreamParser;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Map;

import edu.txstate.mobile.tracs.AnalyticsApplication;
import edu.txstate.mobile.tracs.notifications.DispatchNotification;
import edu.txstate.mobile.tracs.notifications.tracs.TracsAnnouncement;
import edu.txstate.mobile.tracs.notifications.tracs.TracsNotification;
import edu.txstate.mobile.tracs.notifications.tracs.TracsNotificationError;
import edu.txstate.mobile.tracs.util.AppStorage;
import edu.txstate.mobile.tracs.util.NotificationStatus;

public class TracsNotificationRequest extends Request<TracsNotification> {

    private static final String TAG = "TracsNotificationRequest";
    private final Map<String, String> headers;
    private final Response.Listener<TracsNotification> listener;
    private String dispatchId;
    private NotificationStatus status;
    private Date notifyAfter;

    public TracsNotificationRequest(String url, Map<String, String> headers, DispatchNotification dispatchNotification,
                                       Response.Listener<TracsNotification> listener, Response.ErrorListener errorHandler) {
        super(Request.Method.GET, url, errorHandler);
        this.headers = headers;
        this.listener = listener;
        this.dispatchId = dispatchNotification.getDispatchId();
        this.notifyAfter = dispatchNotification.getNotifyAfter();
        this.status = new NotificationStatus(dispatchNotification.hasBeenSeen(),
                dispatchNotification.hasBeenRead(),
                dispatchNotification.hasBeenCleared());
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> customHeaders = headers != null ? headers : super.getHeaders();
        String jSessionId = "JSESSIONID=" + AppStorage.get(AppStorage.SESSION_ID, AnalyticsApplication.getContext());
        customHeaders.put("Cookie", jSessionId);
        return customHeaders;
    }

    @SuppressLint("LongLogTag")
    @Override
    protected Response<TracsNotification> parseNetworkResponse(NetworkResponse response) {
        TracsNotification tracsNotification;
        String notificationData = null;
        try {
            notificationData = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if ("".equals(notificationData) || notificationData == null) {
            tracsNotification = new TracsAnnouncement();
        } else {
            JsonStreamParser parser = new JsonStreamParser(notificationData);
            JsonObject notification = null;

            if (parser.hasNext()) {
                try {
                    notification = (JsonObject) parser.next();
                } catch (ClassCastException e) {
                    Log.e(TAG, "Could not parse JSON response.");
                    TracsNotificationError errorNotification = new TracsNotificationError(response.statusCode);
                    errorNotification.setDispatchId(this.dispatchId);
                    Response.success(errorNotification, HttpHeaderParser.parseCacheHeaders(response));
                }
            }
            tracsNotification = new TracsAnnouncement(notification);
            tracsNotification.setDispatchId(this.dispatchId);
            tracsNotification.setNotifyAfter(this.notifyAfter);
            tracsNotification.markSeen(status.hasBeenSeen());
            tracsNotification.markRead(status.hasBeenRead());
            tracsNotification.markCleared(status.hasBeenCleared());
        }
        return Response.success(tracsNotification, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(TracsNotification response) {
        listener.onResponse(response);
    }
}
