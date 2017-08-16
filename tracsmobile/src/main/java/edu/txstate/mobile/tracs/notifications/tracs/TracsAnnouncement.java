package edu.txstate.mobile.tracs.notifications.tracs;

import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import edu.txstate.mobile.tracs.notifications.NotificationTypes;
import edu.txstate.mobile.tracs.util.TracsClient;

public class TracsAnnouncement extends TracsNotificationAbs {

    private static final String TAG = "TracsAnnouncement";
    public TracsAnnouncement() {}

    public TracsAnnouncement(JsonObject rawNotification) {
        super.setId(this.extractKey(rawNotification, "id", String.class));
        super.setTitle(this.extractKey(rawNotification, "title", String.class));
        super.setSiteId(this.extractKey(rawNotification, "siteId", String.class));
        super.setPageId(TracsNotification.NOT_SET);
    }

    public String getUrl() {
        String announcementUrl = TracsClient.makeUrl("SITE");
        if (this.getPageId() == null) {
            announcementUrl += this.getSiteId();
        } else {
            announcementUrl += this.getSiteId() + "/page/" + this.getPageId();
        }
        return announcementUrl;
    }

    public boolean hasPageId() {
        return !TracsNotification.NOT_SET.equals(super.getPageId());
    }

    @Override
    public String getType() {
        return NotificationTypes.ANNOUNCEMENT;
    }

    private <T> T extractKey(JsonObject notification, String key, Class<T> returnType) {
        if (notification == null) { return returnType.cast(TracsNotificationAbs.NOT_SET); }
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
