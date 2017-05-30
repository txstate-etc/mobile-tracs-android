package edu.txstate.mobile.tracs.util.http.requests;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Iterator;
import java.util.Map;

import edu.txstate.mobile.tracs.notifications.NotificationTypes;
import edu.txstate.mobile.tracs.util.SettingsStore;

public class SettingsRequest extends Request<Void> {

    private final static String TAG = "SettingsRequest";
    private final static String TOKEN_PARAMETER = "/" + FirebaseInstanceId.getInstance().getToken();
    private Response.Listener<Void> listener;

    public SettingsRequest(String url, Response.Listener<Void> listener) {
        super(Method.POST, url + TOKEN_PARAMETER, error -> Log.wtf(TAG, new String(error.networkResponse.data)));
        this.listener = listener;
    }

    @Override
    public String getBodyContentType() {
        return "application/json; charset=" + getParamsEncoding();
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        JsonObject settings = SettingsStore.getInstance().getSettings();
        JsonArray blacklist = new JsonArray();
        JsonObject blacklistEntry = new JsonObject();
        boolean firstEntry = true;

        for (Map.Entry pair : settings.entrySet() ) {
            String key = pair.getKey().toString();
            boolean disabled = !((JsonPrimitive) pair.getValue()).getAsBoolean();
            if (disabled) {
                if (firstEntry) {
                    firstEntry = false;
                    blacklistEntry.add("keys", new JsonObject());
                    blacklistEntry.add("other_keys", new JsonObject());
                }
                if (checkNotificationType(key)) {
                    blacklistEntry.get("keys").getAsJsonObject().addProperty("object_type", pair.getKey().toString());
                } else {
                    blacklistEntry.get("other_keys").getAsJsonObject().addProperty("site_id", pair.getKey().toString());
                }
            }
        }
        if (blacklistEntry.size() > 0) {
            blacklist.add(blacklistEntry);
        }
        JsonObject body = new JsonObject();
        body.add("blacklist", blacklist);
        body.addProperty("global_disable", false);

        return body.toString().getBytes();
    }

    @Override
    protected Response<Void> parseNetworkResponse(NetworkResponse response) {
        if (response.statusCode >= 400) {
            return Response.error(new VolleyError("Error saving settings!"));
        }
        return Response.success(null, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(Void response) {
        this.listener.onResponse(response);
    }

    //Not a fan of this.
    private boolean checkNotificationType(String settingValue) {
        return settingValue != null &&
                (settingValue.equals(NotificationTypes.ANNOUNCEMENT) ||
                 settingValue.equals(NotificationTypes.ASSESSMENT) ||
                 settingValue.equals(NotificationTypes.ASSIGNMENT) ||
                 settingValue.equals(NotificationTypes.GRADE) ||
                 settingValue.equals(NotificationTypes.DISCUSSION));
    }
}
