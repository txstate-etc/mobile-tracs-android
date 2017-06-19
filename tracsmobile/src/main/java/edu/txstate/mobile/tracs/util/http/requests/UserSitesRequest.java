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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import edu.txstate.mobile.tracs.AnalyticsApplication;
import edu.txstate.mobile.tracs.R;
import edu.txstate.mobile.tracs.util.AppStorage;

public class UserSitesRequest extends Request<LinkedHashMap<String, String>> {

    private static final String TAG = "UserSitesRequest";
    private final String DEFAULT_SITE_NAME = "Site Name Not Found";
    private Response.Listener<LinkedHashMap<String, String>> listener;
    private Response.ErrorListener errorListener;
    private static final String URL = AnalyticsApplication.getContext().getString(R.string.tracs_base) +
            AnalyticsApplication.getContext().getString(R.string.tracs_user_sites_url);

    public UserSitesRequest(Response.Listener<LinkedHashMap<String, String>> listener, Response.ErrorListener errorListener) {
        super(Method.GET, URL, errorListener);
        this.listener = listener;
        this.errorListener = errorListener;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<>();
        headers.putAll(super.getHeaders());
        headers.put("Cookie", "JSESSIONID=" + AppStorage.get(AppStorage.SESSION_ID, AnalyticsApplication.getContext()));
        return headers;
    }

    @Override
    protected Response<LinkedHashMap<String, String>> parseNetworkResponse(NetworkResponse response) {
        String responseData = new String(response.data);
        JsonStreamParser parser = new JsonStreamParser(responseData);
        LinkedHashMap<String, String> siteNames = new LinkedHashMap<>();
        JsonArray sites;
        try {
            sites = parser.hasNext() ? parser.next().getAsJsonObject().get("membership_collection").getAsJsonArray() : new JsonArray();
        } catch (NullPointerException e) {
            String errorMsg = "User is not a member of any sites";
            Log.wtf(TAG, errorMsg);
            return Response.error(new VolleyError(errorMsg));
        }

        for (JsonElement site : sites) {
            String id = site.getAsJsonObject().get("id").getAsString().split(":")[3];
            siteNames.put(id, DEFAULT_SITE_NAME);
        }

        return Response.success(siteNames, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    public void deliverError(VolleyError error) {
        this.errorListener.onErrorResponse(error);
    }

    @Override
    protected void deliverResponse(LinkedHashMap<String, String> response) {
        this.listener.onResponse(response);
    }
}
