package edu.txstate.mobile.tracs.util.http.requests;

import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

public class DispatchUnregisterRequest extends Request<Void> {

    private static final String TAG = "UnregisterRequest";

    public DispatchUnregisterRequest(String url, String token) {
        super(Method.DELETE, url + "?token=" + token, error -> Log.i(TAG, new String(error.networkResponse.data)));
    }

    @Override
    protected Response<Void> parseNetworkResponse(NetworkResponse response) {
        if (response.statusCode == 200) {
            return Response.success(null, HttpHeaderParser.parseCacheHeaders(response));
        } else {
            Log.e(TAG, new String(response.data));
            return Response.error(new VolleyError("Failed to unregister from dispatch with response code " + response.statusCode));
        }
    }

    @Override
    protected void deliverResponse(Void response) {
        Log.i(TAG, "Device registration has been deleted.");
    }
}
