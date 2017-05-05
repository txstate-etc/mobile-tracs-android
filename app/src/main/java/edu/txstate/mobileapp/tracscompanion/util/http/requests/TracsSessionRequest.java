package edu.txstate.mobileapp.tracscompanion.util.http.requests;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonStreamParser;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import edu.txstate.mobileapp.tracscompanion.AnalyticsApplication;
import edu.txstate.mobileapp.tracscompanion.util.AppStorage;

public class TracsSessionRequest<T> extends Request<T> {
    private static final String TAG = "TracsSessionRequest";
    private Gson gson = new Gson();
    private final Class<T> gClass;
    private final Map<String, String> headers;
    private final Response.Listener<T> listener;

    public TracsSessionRequest(Class<T> clazz, Map<String, String> headers,
                               Response.Listener<T> listener, Response.ErrorListener errorHandler) {
        super(Method.GET, "https://tracs.txstate.edu/direct/session.json", errorHandler);
        this.gClass = clazz;
        this.headers = headers;
        this.listener = listener;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        headers.putAll(super.getHeaders());
        headers.put("Cookie", "JSESSIONID=" + AppStorage.get(AppStorage.SESSION_ID, AnalyticsApplication.getContext()));
        return headers;
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
            if (sessionId != null) {
                session.addProperty("sessionId", sessionId.split(";")[0].split("=")[1]);
            }
            T tracsSession = gson.fromJson(session, gClass);
            return Response.success(tracsSession, HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void deliverResponse(T response) {
        listener.onResponse(response);
    }
}
