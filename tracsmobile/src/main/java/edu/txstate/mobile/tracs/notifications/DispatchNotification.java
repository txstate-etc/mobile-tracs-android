package edu.txstate.mobile.tracs.notifications;

import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DispatchNotification extends TracsAppNotificationAbs {
    private String id;
    private String providerId;
    private String objectId;
    private String userId;
    private String type;
    private String siteId;
    private String toolId;

    private static final String TAG = "DispatchNotification";

    public DispatchNotification(JsonObject rawNotification) {
        JsonObject keys = extractKey(rawNotification, "keys", JsonObject.class);
        JsonObject other_keys = extractKey(rawNotification, "other_keys", JsonObject.class);

        this.id = extractKey(rawNotification, "id", String.class);
        super.setDispatchId(this.id);
        super.markSeen(extractKey(rawNotification, "seen", Boolean.class));
        super.markRead(extractKey(rawNotification, "read", Boolean.class));
        super.markCleared(extractKey(rawNotification, "cleared", Boolean.class));
        super.setNotifyAfter(extractKey(rawNotification, "notify_after", Date.class));
        this.type = extractKey(keys, "object_type", String.class);
        this.providerId = extractKey(keys, "provider_id", String.class);
        this.objectId = extractKey(keys, "object_id", String.class);
        this.userId = extractKey(keys, "user_id", String.class);
        this.siteId = extractKey(other_keys, "site_id", String.class);
        this.toolId = extractKey(other_keys, "tool_id", String.class);
    }

    public String getId() {
        return this.id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return this.type;
    }

    public String getObjectId() { return this.objectId; }

    public String getProviderId() {
        return this.providerId;
    }

    public String getUserId() {
        return this.userId;
    }

    public String getToolId() {
        return this.toolId;
    }

    public String getSiteId() { return this.siteId; }

    public <T> T extractKey(JsonObject rawNotification, String key, Class<T> Type) {
        if (rawNotification == null) { return null; }
        JsonElement value = rawNotification.get(key);
        if (value == null) { return null; }

        if (Type == Boolean.class) {
            return Type.cast(value.getAsBoolean());
        }
        if (Type == JsonObject.class) {
            return Type.cast(value.getAsJsonObject());
        }
        if (Type == Date.class) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
            try {
                return Type.cast(dateFormat.parse(value.getAsString()));
            } catch (ParseException e) {
                Log.e(TAG, "Could not parse date");
                return Type.cast(new Date());
            }
        }
        return Type.cast(value.getAsString());
    }
}
