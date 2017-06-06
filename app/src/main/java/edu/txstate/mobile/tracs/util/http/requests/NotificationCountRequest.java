package edu.txstate.mobile.tracs.util.http.requests;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonStreamParser;

import java.io.UnsupportedEncodingException;

import edu.txstate.mobile.tracs.notifications.DispatchNotification;


public class NotificationCountRequest extends Request<Integer> {
    private static final String TAG = "NotificationCountRequest";
    private final Response.Listener<Integer> listener;

    public NotificationCountRequest(String url, Response.Listener<Integer> listener, Response.ErrorListener errorHandler) {
        super(Request.Method.GET, url, errorHandler);
        this.listener = listener;
    }

    @Override
    protected Response<Integer> parseNetworkResponse(NetworkResponse response) {
        try {
            String notificationData = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));
            JsonElement data = new JsonArray();
            JsonArray notifications = new JsonArray();
            JsonStreamParser parser = new JsonStreamParser(notificationData);
            if (parser.hasNext()) {
                data = parser.next();
            }
            if (data.isJsonArray()) {
                notifications = (JsonArray) data;
            } else if (data.isJsonObject()) {
                notifications.add(data);
            }

            int unseenNotifications = 0;
            for (JsonElement notification : notifications) {
                if (notification.isJsonObject()) {
                    DispatchNotification temp = new DispatchNotification((JsonObject) notification);
                    if (!temp.hasBeenSeen()) {
                        unseenNotifications++;
                    }
                }
            }
            return Response.success(unseenNotifications, HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void deliverResponse(Integer badgeCount) {
        listener.onResponse(badgeCount);
    }
}
