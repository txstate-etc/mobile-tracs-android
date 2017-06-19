package edu.txstate.mobile.tracs.util.http.requests;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.JsonObject;
import com.google.gson.JsonStreamParser;

import java.io.UnsupportedEncodingException;

import edu.txstate.mobile.tracs.AnalyticsApplication;
import edu.txstate.mobile.tracs.R;


public class NotificationCountRequest extends Request<Integer> {
    private static final String TAG = "NotificationCountRequest";
    private final Response.Listener<Integer> listener;

    public NotificationCountRequest(Response.Listener<Integer> listener, Response.ErrorListener errorHandler) {
        super(Request.Method.GET,
                AnalyticsApplication.getContext().getResources().getString(R.string.dispatch_base)
                        + AnalyticsApplication.getContext().getResources().getString(R.string.dispatch_notifications)
                        + "/count?token=" + FirebaseInstanceId.getInstance().getToken(),
                errorHandler);
        this.listener = listener;
    }

    @Override
    protected Response<Integer> parseNetworkResponse(NetworkResponse response) {
        try {
            String count = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));
            JsonObject countResponse = new JsonStreamParser(count).next().getAsJsonObject();
            int unseenCount = countResponse.get("count").getAsInt();
            return Response.success(unseenCount, HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void deliverResponse(Integer badgeCount) {
        listener.onResponse(badgeCount);
    }
}
