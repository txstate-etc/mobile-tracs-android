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
import com.google.gson.JsonObject;
import com.google.gson.JsonStreamParser;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import edu.txstate.mobile.tracs.AnalyticsApplication;
import edu.txstate.mobile.tracs.R;
import edu.txstate.mobile.tracs.notifications.NotificationTypes;
import edu.txstate.mobile.tracs.util.AppStorage;
import edu.txstate.mobile.tracs.util.http.listeners.DataErrorListener;

public class TracsPageIdRequest extends Request<JsonObject> {

    private static final String TAG = "TracsPageIdRequest";
    private static final String url = AnalyticsApplication.getContext().getResources().getString(R.string.tracs_base)
            + AnalyticsApplication.getContext().getResources().getString(R.string.tracs_site);
    private Response.Listener<JsonObject> listener;
    private String siteId;

    public TracsPageIdRequest(String siteId, Response.Listener<JsonObject> listener, DataErrorListener errorListener) {
        super(Method.GET,
                url + siteId + "/pages.json",
                error -> errorListener.onError(siteId));
        this.listener = listener;
        this.siteId = siteId;
    }


    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<>();
        headers.putAll(super.getHeaders());
        headers.put("Cookie", "JSESSIONID=" + AppStorage.get(AppStorage.SESSION_ID, AnalyticsApplication.getContext()));
        return headers;
    }

    @Override
    protected Response<JsonObject> parseNetworkResponse(NetworkResponse response) {
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
        JsonObject pageIds = new JsonObject();
        pageIds.addProperty("siteId", this.siteId);
        for (JsonElement page : pages) {
            JsonArray tools = page.getAsJsonObject().get("tools").getAsJsonArray();
            String pageId;
            for (JsonElement tool : tools) {
                String toolId = tool.getAsJsonObject().get("toolId").getAsString();
                switch(toolId) {
                    case Tool.ANNOUNCEMENTS:
                        pageId = tool.getAsJsonObject().get("pageId").getAsString();
                        pageIds.addProperty(NotificationTypes.ANNOUNCEMENT, pageId);
                        break;
                    case Tool.DISCUSSIONS:
                        pageId = tool.getAsJsonObject().get("pageId").getAsString();
                        pageIds.addProperty(NotificationTypes.DISCUSSION, pageId);
                        break;
                    default:
                        break;
                }
            }
        }
        return Response.success(pageIds, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(JsonObject response) {
        this.listener.onResponse(response);
    }

    private interface Tool {
        String ANNOUNCEMENTS = "sakai.announcements";
        String DISCUSSIONS = "sakai.forums";
    }
}
