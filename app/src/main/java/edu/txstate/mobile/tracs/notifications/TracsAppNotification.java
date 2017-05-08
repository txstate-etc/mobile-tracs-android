package edu.txstate.mobile.tracs.notifications;

import com.google.gson.JsonObject;

public interface TracsAppNotification {
    /**
     * getId should return a String representation of the ID of this notification, regardless
     * of original ID type.
     *
     * @return The ID for this notification
     */
    String getId();

    /**
     * Retrieves the type of the notification
     *
     * @return True if this notification has been read (navigated to).
     */
    String getType();

    /**
     * Extracts a value from a given key, attempting to return it as the type specified.
     * @param notification JsonObject with keys and values to extract
     * @param key key to pull a value from
     * @param returnType the type of the key you are extracting, i.e. String.class
     * @param <T> String, Double, Float, etc
     * @return The extracted value as the type specified.
     */
    <T> T extractKey(JsonObject notification, String key, Class<T> returnType);


}
