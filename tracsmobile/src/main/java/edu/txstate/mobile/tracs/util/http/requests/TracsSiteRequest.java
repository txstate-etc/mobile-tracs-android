package edu.txstate.mobile.tracs.util.http.requests;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.JsonObject;
import com.google.gson.JsonStreamParser;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import edu.txstate.mobile.tracs.AnalyticsApplication;
import edu.txstate.mobile.tracs.R;
import edu.txstate.mobile.tracs.util.AppStorage;

public class TracsSiteRequest extends Request<JsonObject> {

    private static final String url = AnalyticsApplication.getContext().getString(R.string.tracs_base) +
                                      AnalyticsApplication.getContext().getString(R.string.tracs_site);
    private static final String TAG = "TracsSiteRequest";

    private final Response.Listener<JsonObject> listener;
    private Map<String, String> headers;

    public TracsSiteRequest(String siteId,
                            Map<String, String> headers,
                            Response.Listener<JsonObject> listener) {
        super(Method.GET,
                url + siteId + ".json",
                error -> Log.e(TAG, "Site name is not available"));
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
    protected Response<JsonObject> parseNetworkResponse(NetworkResponse response) {
        String siteData = null;
        try {
            siteData = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        JsonStreamParser parser = new JsonStreamParser(siteData);
        JsonObject siteInfo;
        try {
            siteInfo = parser.hasNext() ? (JsonObject) parser.next() : new JsonObject();
        } catch (ClassCastException e) {
            return Response.error(new VolleyError("Could not parse site id."));
        }

        return Response.success(siteInfo, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(JsonObject response) {
        this.listener.onResponse(response);
    }
}
