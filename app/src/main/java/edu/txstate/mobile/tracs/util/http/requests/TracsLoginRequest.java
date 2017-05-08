package edu.txstate.mobile.tracs.util.http.requests;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

import edu.txstate.mobile.tracs.AnalyticsApplication;
import edu.txstate.mobile.tracs.util.AppStorage;

public class TracsLoginRequest extends StringRequest {

    private static final String TAG = "TracsLoginRequest";
    private Map<String, String> params = new HashMap<>();

    public TracsLoginRequest (String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(Method.POST, url, listener, errorListener);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        params.put("_username", AppStorage.get(AppStorage.USERNAME, AnalyticsApplication.getContext()));
        params.put("_password", AppStorage.get(AppStorage.PASSWORD, AnalyticsApplication.getContext()));
        return params;
    }

    @Override
    public String getBodyContentType() {
        return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
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
