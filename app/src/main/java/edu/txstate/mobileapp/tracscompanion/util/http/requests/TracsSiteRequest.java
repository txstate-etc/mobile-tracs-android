package edu.txstate.mobileapp.tracscompanion.util.http.requests;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.JsonObject;
import com.google.gson.JsonStreamParser;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import edu.txstate.mobileapp.tracscompanion.AnalyticsApplication;
import edu.txstate.mobileapp.tracscompanion.notifications.tracs.TracsNotification;
import edu.txstate.mobileapp.tracscompanion.util.AppStorage;

public class TracsSiteRequest extends Request<Map<String, String>> {

    private static final String url = "https://tracs.txstate.edu/direct/site/";
    private static final String TAG = "TracsSiteRequest";

    private final Response.Listener<Map<String, String>> listener;
    private Map<String, String> headers;

    public TracsSiteRequest(TracsNotification notification,
                            Map<String, String> headers,
                            Response.Listener<Map<String, String>> listener) {
        super(Method.GET,
                url + notification.getSiteId() + ".json",
                error -> Log.wtf(TAG, error.getMessage()));
        this.listener = listener;
        this.headers = headers;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        headers.putAll(super.getHeaders());
        headers.put("Cookie", "JSESSIONID=" + AppStorage.get(AppStorage.SESSION_ID, AnalyticsApplication.getContext()));
        return headers;
    }

    @Override
    protected Response<Map<String, String>> parseNetworkResponse(NetworkResponse response) {
        String siteData = null;
        try {
            siteData = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        JsonStreamParser parser = new JsonStreamParser(siteData);
        JsonObject siteInfo = parser.hasNext() ? (JsonObject) parser.next() : new JsonObject();

        Map<String, String> siteName = new HashMap<>();

        try {
            siteName.put(siteInfo.get("entityId").getAsString(), siteInfo.get("entityTitle").getAsString());
        } catch (NullPointerException e) {
            Log.wtf(TAG, "Could not retrieve site name");
        }

        return Response.success(siteName, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(Map<String, String> response) {
        this.listener.onResponse(response);
    }
}
