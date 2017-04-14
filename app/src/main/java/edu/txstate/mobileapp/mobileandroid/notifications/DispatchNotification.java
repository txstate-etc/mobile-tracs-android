package edu.txstate.mobileapp.mobileandroid.notifications;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class DispatchNotification implements TracsAppNotification {
    private String id;
    private Boolean seen;
    private Boolean read;

    private String providerId;
    private String objectId;
    private String userId;
    private String type;
    private String siteId;
    private String toolId;
    private String entityId;

    DispatchNotification() {}

    public DispatchNotification(JsonObject rawNotification) {
        this.id = extractKey(rawNotification, "id", String.class);
        this.seen = extractKey(rawNotification, "seen", Boolean.class);
        this.read = extractKey(rawNotification, "read", Boolean.class);
        this.type = extractKey(rawNotification, "object_type", String.class);
        this.providerId = extractKey(rawNotification, "provider_id", String.class);
        this.objectId = extractKey(rawNotification, "object_id", String.class);
        this.userId = extractKey(rawNotification, "user_id", String.class);
        this.siteId = extractKey(rawNotification, "site_id", String.class);
        this.toolId = extractKey(rawNotification, "tool_id", String.class);
        this.entityId = makeEntityId(this.siteId, this.objectId);
    }

    public String getId() {
        return this.id;
    }

    public Boolean hasBeenRead() {
        return this.read;
    }

    public Boolean hasBeenSeen() {
        return this.seen;
    }

    public void markAsRead() {
        this.read = true;
    }

    public void markAsViewed() {
        this.seen = true;
    }

    public String getType() {
        return this.type;
    }

    public String getEntityId() {
        return this.entityId;
    }

    public String getProviderId() {
        return providerId;
    }

    public String getUserId() {
        return userId;
    }

    public String getToolId() {
        return toolId;
    }

    public <T> T extractKey(JsonObject rawNotification, String key, Class<T> Type) {
        JsonElement value = rawNotification.get(key);
        if (Type == Boolean.class) {
            return Type.cast(value.getAsBoolean());
        }
        return Type.cast(value.getAsString());
    }

    private String makeEntityId(String siteId, String objectId) {
        return siteId + ":main:" + objectId;
    }

}
