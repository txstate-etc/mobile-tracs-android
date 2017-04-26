package edu.txstate.mobileapp.tracscompanion.util.http.requests;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.JsonObject;
import com.google.gson.JsonStreamParser;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import edu.txstate.mobileapp.tracscompanion.AnalyticsApplication;
import edu.txstate.mobileapp.tracscompanion.NotificationsActivity;
import edu.txstate.mobileapp.tracscompanion.notifications.tracs.TracsAnnouncement;
import edu.txstate.mobileapp.tracscompanion.notifications.tracs.TracsNotification;
import edu.txstate.mobileapp.tracscompanion.util.AppStorage;

public class TracsNotificationRequest extends Request<TracsNotification> {

    private static final String TAG = "TracsNotificationRequest";
    private final Map<String, String> headers;
    private final Response.Listener<TracsNotification> listener;

    public TracsNotificationRequest(String url, Map<String, String> headers,
                                       Response.Listener<TracsNotification> listener, Response.ErrorListener errorHandler) {
        super(Request.Method.GET, url, errorHandler);
        this.headers = headers;
        this.listener = listener;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> customHeaders = headers != null ? headers : super.getHeaders();
        String jSessionId = "JSESSIONID=" + AppStorage.get(AppStorage.SESSION_ID, AnalyticsApplication.getAppContext());
        customHeaders.put("Cookie", jSessionId);
        return customHeaders;
    }

    @Override
    protected Response<TracsNotification> parseNetworkResponse(NetworkResponse response) {
        TracsNotification announcement;
        String notificationData = null;
        try {
            notificationData = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if ("".equals(notificationData) || notificationData == null) {
            announcement = new TracsAnnouncement();
        } else {
            JsonStreamParser parser = new JsonStreamParser(notificationData);
            JsonObject notification = null;

            while (parser.hasNext()) {
                notification = (JsonObject) parser.next();
            }
            announcement = new TracsAnnouncement(notification);
        }
        return Response.success(announcement, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(TracsNotification response) {
        listener.onResponse(response);
    }
}
