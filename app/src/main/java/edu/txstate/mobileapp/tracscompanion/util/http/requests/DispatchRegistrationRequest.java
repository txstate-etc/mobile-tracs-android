package edu.txstate.mobileapp.tracscompanion.util.http.requests;

import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

public class DispatchRegistrationRequest extends Request<Void> {

    private static final String TAG = "DispatchRegistration";
    private static final String dispatchError = "Could not register with dispatch";

    public DispatchRegistrationRequest(String url) {
        super(Method.POST, url, error -> Log.wtf(TAG, error.getMessage()));
    }

    @Override
    protected Response<Void> parseNetworkResponse(NetworkResponse response) {
        if (response.statusCode == 200) {
            return Response.success(null, HttpHeaderParser.parseCacheHeaders(response));
        }
        return Response.error(new VolleyError(dispatchError));
    }

    @Override
    protected void deliverResponse(Void response) {
        Log.i(TAG, "Device registered.");
    }
}
