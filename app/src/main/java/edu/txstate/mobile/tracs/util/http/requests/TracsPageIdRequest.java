package edu.txstate.mobile.tracs.util.http.requests;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonStreamParser;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import edu.txstate.mobile.tracs.AnalyticsApplication;
import edu.txstate.mobile.tracs.util.AppStorage;

public class TracsPageIdRequest extends Request<Map<String, String>> {

    private static final String TAG = "TracsPageIdRequest";
    private String dispatchId;
    private Response.Listener<Map<String, String>> listener;

    public TracsPageIdRequest(String url, String dispatchId, Response.Listener<Map<String, String>> listener) {
        super(Method.GET, url, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.wtf(TAG, new String(error.networkResponse.data));
            }
        });
        this.listener = listener;
        this.dispatchId = dispatchId;
    }


    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<>();
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
        JsonArray pages;
        try {
            pages = parser.hasNext() ? (JsonArray) parser.next() : new JsonArray();
        } catch (ClassCastException e) {
            return Response.error(new VolleyError("Could not parse page id."));
        }

        for (JsonElement page : pages) {
            JsonArray tools = page.getAsJsonObject().get("tools").getAsJsonArray();
            for (JsonElement tool : tools) {
                String toolId = tool.getAsJsonObject().get("toolId").getAsString();
                if (Tool.ANNOUNCEMENTS.equals(toolId)) {
                    String pageId = tool.getAsJsonObject().get("pageId").getAsString();
                    Map<String, String> pageIdDispatchId = new HashMap<>();
                    pageIdDispatchId.put(this.dispatchId, pageId);
                    return Response.success(pageIdDispatchId, HttpHeaderParser.parseCacheHeaders(response));
                }
            }
        }

        return Response.error(new VolleyError("Could not find a pageId for this notification"));
    }

    @Override
    protected void deliverResponse(Map<String, String> response) {
        this.listener.onResponse(response);
    }

    private interface Tool {
        String ANNOUNCEMENTS = "sakai.announcements";
    }
}
