package edu.txstate.mobileapp.tracscompanion.notifications.tracs;

import com.google.gson.JsonObject;

import edu.txstate.mobileapp.tracscompanion.notifications.NotificationTypes;

public class TracsNotificationError extends TracsNotificationAbs {

    private static final String TAG = "TracsNotificationError";
    private int statusCode;

    public TracsNotificationError(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    @Override
    public String getUrl() {
        return null;
    }

    @Override
    public void setPageId(String pageId) {
        throw new UnsupportedOperationException("Operation is not implemented");
    }

    @Override
    public String getType() {
        return NotificationTypes.ERROR;
    }

    @Override
    public <T> T extractKey(JsonObject notification, String key, Class<T> returnType) {
        return null;
    }
}
