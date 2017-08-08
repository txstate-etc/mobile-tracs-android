package edu.txstate.mobile.tracs.util.http.requests;

import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import edu.txstate.mobile.tracs.AnalyticsApplication;
import edu.txstate.mobile.tracs.util.AppStorage;

public class JwtRequest extends StringRequest {
    private Map<String, String> headers;

    public JwtRequest(String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(Method.GET, url, listener, errorListener);
        this.headers = new HashMap<>();
    }


    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        this.headers.putAll(super.getHeaders());
        String username = AppStorage.get(AppStorage.USERNAME, AnalyticsApplication.getContext());
        String password = AppStorage.get(AppStorage.PASSWORD, AnalyticsApplication.getContext());
        String creds = String.format("%s:%s", username, password);
        String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.DEFAULT);
        headers.put("Authorization", auth);
        return headers;
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        String jwToken = "";
        try {
            jwToken = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return Response.success(jwToken, HttpHeaderParser.parseCacheHeaders(response));
    }
}
