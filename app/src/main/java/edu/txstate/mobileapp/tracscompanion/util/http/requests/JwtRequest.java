package edu.txstate.mobileapp.tracscompanion.util.http.requests;

import android.content.Context;
import android.content.SharedPreferences;
import android.webkit.CookieManager;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import edu.txstate.mobileapp.tracscompanion.AnalyticsApplication;

public class JwtRequest extends StringRequest {
    private Map<String, String> headers;

    public JwtRequest(String url, Map<String, String> headers, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(Method.GET, url, listener, errorListener);
        this.headers = headers;
    }


    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        this.headers.putAll(super.getHeaders());
        CookieManager cookies = CookieManager.getInstance();
        String url = "https://login.its.txstate.edu";
        SharedPreferences prefs = AnalyticsApplication.getContext().getSharedPreferences("cas", Context.MODE_PRIVATE);
        String userAgent = prefs.getString("user-agent", "");
        prefs.edit().remove("user-agent").apply();
        this.headers.put("Cookie", cookies.getCookie(url).split("; ")[1]);
        this.headers.put("User-Agent", userAgent);
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
