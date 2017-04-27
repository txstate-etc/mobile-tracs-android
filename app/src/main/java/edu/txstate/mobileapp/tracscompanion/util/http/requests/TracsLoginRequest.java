package edu.txstate.mobileapp.tracscompanion.util.http.requests;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.JsonObject;

import java.io.UnsupportedEncodingException;

import edu.txstate.mobileapp.tracscompanion.AnalyticsApplication;
import edu.txstate.mobileapp.tracscompanion.util.AppStorage;

public class TracsLoginRequest extends StringRequest {

    private static final String TAG = "TracsLoginRequest";
    private JsonObject jsonBody = new JsonObject();
    private final String requestBody;

    public TracsLoginRequest (String url, Response.Listener<String> listener) {
        super(Request.Method.POST, url, listener, error -> Log.wtf(TAG, error.getMessage()));
        jsonBody.addProperty("_username", AppStorage.get(AppStorage.USERNAME, AnalyticsApplication.getContext()));
        jsonBody.addProperty("_password", AppStorage.get(AppStorage.PASSWORD, AnalyticsApplication.getContext()));
        requestBody = jsonBody.toString();
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        byte[] bodyBytes = null;
        try {
            bodyBytes = requestBody == null ? null : requestBody.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            Log.wtf(TAG, e.getMessage());
        }
        return bodyBytes;
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        String jSessionId = response.headers.get("Set-Cookie");
        if (jSessionId == null) {
            return Response.error(new VolleyError("Login Attempt Failed."));
        }
        jSessionId = jSessionId.split(";")[0].split("=")[1];
        AppStorage.put(AppStorage.SESSION_ID, jSessionId, AnalyticsApplication.getContext());
        return Response.success(jSessionId, HttpHeaderParser.parseCacheHeaders(response));
    }
}
