package edu.txstate.mobile.tracs.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonStreamParser;

public class SettingsStore {
    private static final String TAG = "SettingsStore";
    private final boolean DEFAULT_SETTING = true;
    private JsonObject settings = new JsonObject();

    private static SettingsStore notificationSettings;

    private SettingsStore() {}

    public static SettingsStore getInstance() {
        if (notificationSettings == null) {
            notificationSettings = new SettingsStore(new JsonObject());
        }
        return notificationSettings;
    }

    private SettingsStore(JsonObject settings) {
        this.settings = settings;
    }

    public void putFromString(String settings) {
        if ("".equals(settings)) { return; }
        JsonStreamParser parser = new JsonStreamParser(settings);
        if (parser.hasNext()) {
            this.settings = parser.next().getAsJsonObject();
        }
    }

    public void put(String settingId, boolean isEnabled) {
        this.settings.addProperty(settingId, isEnabled);
    }

    public Boolean get(String settingId) {
        if (this.settings.has(settingId)) {
            return this.settings.get(settingId).getAsBoolean();
        }
        return DEFAULT_SETTING;
    }

    public JsonObject getSettings() {
        return this.settings;
    }

    public String toString() {
        return this.settings.toString();
    }
}
