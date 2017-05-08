package edu.txstate.mobile.tracs.notifications.tracs;

import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import edu.txstate.mobile.tracs.notifications.NotificationTypes;
import edu.txstate.mobile.tracs.util.TracsClient;

public class TracsAnnouncement extends TracsNotificationAbs {

    private static final String TAG = "TracsAnnouncement";
    private String pageId;

    public TracsAnnouncement() {}

    public TracsAnnouncement(JsonObject rawNotification) {
        super.setId(this.extractKey(rawNotification, "id", String.class));
        super.setTitle(this.extractKey(rawNotification, "title", String.class));
        super.setSiteId(this.extractKey(rawNotification, "siteId", String.class));
    }

    public String getUrl() {
        String announcementUrl = TracsClient.makeUrl("SITE");
        announcementUrl += this.getSiteId() + "/page/" + this.getPageId();
        return announcementUrl;
    }

    public void setPageId(String pageId) {
        this.pageId = pageId;
    }

    private String getPageId() {
        return this.pageId;
    }

    @Override
    public String getType() {
        return NotificationTypes.ANNOUNCEMENT;
    }

    @Override
    public <T> T extractKey(JsonObject notification, String key, Class<T> returnType) {
        if (notification == null) { return returnType.cast(TracsNotificationAbs.NOT_SET); }
        T value;
        JsonElement jsonValue = notification.get(key);

        if (jsonValue == null) {
            Log.wtf(TAG, "Tried to find key: " + key + " and failed.");
            return null;
        }

        value = returnType.cast(jsonValue.getAsString());
        return value;
    }
}
