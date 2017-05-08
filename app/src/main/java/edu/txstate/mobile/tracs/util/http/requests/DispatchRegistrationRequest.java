package edu.txstate.mobile.tracs.util.http.requests;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONObject;

import edu.txstate.mobile.tracs.util.LoginStatus;

public class DispatchRegistrationRequest extends Request<Void> {

    private static final String TAG = "DispatchRegistration";
    private static final String dispatchError = "Could not register with dispatch";
    private JSONObject body;

    public DispatchRegistrationRequest(String url, JSONObject body) {
        super(Method.POST, url, error -> Log.wtf(TAG, error.getMessage()));
        this.body = body;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        return body.toString().getBytes();
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
        LoginStatus.getInstance().login();
    }
}