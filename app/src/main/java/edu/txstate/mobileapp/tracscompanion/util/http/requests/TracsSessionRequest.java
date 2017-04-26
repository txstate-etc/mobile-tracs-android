package edu.txstate.mobileapp.tracscompanion.util.http.requests;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonStreamParser;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public class TracsSessionRequest<T> extends Request<T> {
    private static final String TAG = "TracsSessionRequest";
    private Gson gson = new Gson();
    private final Class<T> gClass;
    private final Map<String, String> headers;
    private final Response.Listener<T> listener;

    public TracsSessionRequest(String url, Class<T> clazz, Map<String, String> headers,
                               Response.Listener<T> listener, Response.ErrorListener errorHandler) {
          super(Method.GET, url, errorHandler);
        this.gClass = clazz;
        this.headers = headers;
        this.listener = listener;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers != null ? headers : super.getHeaders();
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));
            String sessionId = response.headers.get("Set-Cookie");
            JsonStreamParser parser = new JsonStreamParser(json);
            JsonArray sessions;
            if (parser.hasNext()) {
                sessions = parser.next().getAsJsonObject().get("session_collection").getAsJsonArray();
            } else {
                sessions = new JsonArray();
            }

            JsonObject session = sessions.get(0).getAsJsonObject();
            session.addProperty("sessionId", sessionId.split(";")[0].split("=")[1]);
            T tracsSession = gson.fromJson(session, gClass);
            return Response.success(tracsSession, HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void deliverResponse(T response) {
        if (response == null) { return; }
        listener.onResponse(response);
    }

    private class ErrorHandler implements Response.ErrorListener {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.wtf(TAG, error);
        }
    }
}
