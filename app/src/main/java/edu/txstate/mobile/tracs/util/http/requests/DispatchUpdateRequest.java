package edu.txstate.mobile.tracs.util.http.requests;

import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import edu.txstate.mobile.tracs.notifications.DispatchNotification;
import edu.txstate.mobile.tracs.notifications.TracsAppNotification;
import edu.txstate.mobile.tracs.util.NotificationStatus;

public class DispatchSeenRequest extends Request<Void> {

    private static final String responseError = "Error communicating with dispatch";
    private static final String TAG = "DispatchSeenRequest";
    private NotificationStatus status;

    private DispatchSeenRequest(String url, String notificationId, NotificationStatus status) {
        super(Method.PATCH,
                url + "/" + notificationId + "?token=" + FirebaseInstanceId.getInstance().getToken(),
                error -> Log.wtf(TAG, error.getMessage()));
        this.status = status;
    }

    @Override
    public byte[] getBody() {
        JSONObject body = new JSONObject();
        try {
            body.put("seen", status.hasBeenSeen());
            body.put("read", status.hasBeenRead());
            body.put("cleared", status.hasBeenCleared());
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
