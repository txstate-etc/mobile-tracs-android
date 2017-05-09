package edu.txstate.mobile.tracs.util.http.requests;

import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONException;
import org.json.JSONObject;

import edu.txstate.mobile.tracs.notifications.DispatchNotification;

public class DispatchSeenRequest extends Request<Void> {

    private static final String responseError = "Error communicating with dispatch";
    private static final String TAG = "DispatchSeenRequest";
    private String notificationId;

    public DispatchSeenRequest(String url, DispatchNotification dispatchNotification) {
        super(Method.PATCH, url + "/" + dispatchNotification.getId(), error -> Log.wtf(TAG, error.getMessage()));
    }

    @Override
    public byte[] getBody() {
        JSONObject body = new JSONObject();
        try {
            body.put("seen", true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return body.toString().getBytes();
    }

    @Override
    protected Response<Void> parseNetworkResponse(NetworkResponse response) {
        if (response.statusCode == 200) {
            return Response.success(null, HttpHeaderParser.parseCacheHeaders(response));
        }
        return Response.error(new VolleyError(responseError));
    }

    @Override
    protected void deliverResponse(Void response) {
        //TODO: Log firebase event for notification marked as seen
    }
}
