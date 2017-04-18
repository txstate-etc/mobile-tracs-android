package edu.txstate.mobileapp.mobileandroid.notifications.tracs;

import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import edu.txstate.mobileapp.mobileandroid.notifications.NotificationTypes;
import edu.txstate.mobileapp.mobileandroid.util.TracsClient;

public class TracsAnnouncement extends TracsNotificationAbs {

    private static final String TAG = "TracsAnnouncement";

    public TracsAnnouncement() {}

    public TracsAnnouncement(JsonObject rawNotification) {
        super.setId(this.extractKey(rawNotification, "id", String.class));
        super.setTitle(this.extractKey(rawNotification, "title", String.class));
        super.setSubtitle(this.extractKey(rawNotification, "body", String.class));
    }

    public String getUrl() {
        String announcementUrl = TracsClient.makeUrl(this.getType());
        announcementUrl += super.getId();
        return announcementUrl;
    }

    @Override
    public String getType() {
        return NotificationTypes.ANNOUNCEMENT;
    }

    @Override
    public <T> T extractKey(JsonObject notification, String key, Class<T> returnType) {
        T value;
        JsonElement jsonValue = notification.get(key);

        if (jsonValue == null) {
            Log.e(TAG, "Tried to find key: " + key + " and failed.");
            return null;
        }

        value = returnType.cast(jsonValue.getAsString());
        return value;
    }
}
