package edu.txstate.mobile.tracs.util.http.requests;

import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

public class DispatchSeenRequest extends Request<Void> {

    private static final String TAG = "DispatchSeenRequest";

    public DispatchSeenRequest(int method, String url) {
        super(method, url, error -> Log.wtf(TAG, error.getMessage()));
    }

    @Override
    protected Response<Void> parseNetworkResponse(NetworkResponse response) {
        return null;
    }

    @Override
    protected void deliverResponse(Void response) {

    }
}
