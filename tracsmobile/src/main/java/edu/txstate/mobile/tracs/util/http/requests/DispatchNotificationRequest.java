package edu.txstate.mobile.tracs.util.http.requests;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonStreamParser;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import edu.txstate.mobile.tracs.notifications.DispatchNotification;
import edu.txstate.mobile.tracs.notifications.NotificationTypes;
import edu.txstate.mobile.tracs.notifications.NotificationsBundle;

public class DispatchNotificationRequest extends Request<NotificationsBundle> {
    private static final String TAG = "TracsSessionRequest";
    private final Map<String, String> headers;
    private final Response.Listener<NotificationsBundle> listener;

    public DispatchNotificationRequest(String url, Map<String, String> headers,
                               Response.Listener<NotificationsBundle> listener, Response.ErrorListener errorHandler) {
        super(Request.Method.GET, url, errorHandler);
        this.headers = headers;
        this.listener = listener;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers != null ? headers : super.getHeaders();
    }

    @Override
    protected Response<NotificationsBundle> parseNetworkResponse(NetworkResponse response) {
        try {
            String notificationData = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));
            JsonElement data = new JsonArray();
            JsonArray notifications = new JsonArray();
            JsonStreamParser parser = new JsonStreamParser(notificationData);
            NotificationsBundle dispatchNotifications = new NotificationsBundle();
            if (parser.hasNext()) {
                data = parser.next();
            }
            if (data.isJsonArray()) {
                notifications = (JsonArray) data;
            }

            if (data.isJsonObject()) {
                notifications.add(data);
            }

            for (JsonElement notification : notifications) {
                if (notification.isJsonObject() && notification.getAsJsonObject().get("keys").getAsJsonObject().get("object_type").getAsString().equals(NotificationTypes.ANNOUNCEMENT)) {
                    dispatchNotifications.add(new DispatchNotification((JsonObject) notification));
                }
            }
            return Response.success(dispatchNotifications, HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void deliverResponse(NotificationsBundle response) {
        listener.onResponse(response);
    }
}