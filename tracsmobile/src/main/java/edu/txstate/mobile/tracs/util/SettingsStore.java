package edu.txstate.mobile.tracs.util;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.txstate.mobile.tracs.AnalyticsApplication;
import edu.txstate.mobile.tracs.R;
import edu.txstate.mobile.tracs.notifications.NotificationTypes;
import edu.txstate.mobile.tracs.util.http.HttpQueue;
import edu.txstate.mobile.tracs.util.http.requests.SettingsRequest;

public class SettingsStore {
    private static final String TAG = "SettingsStore";
    private static final boolean SETTING_ENABLED = true;
    private static final boolean SETTING_DISABLED = false;
    private static final String[] SETTINGS_NOT_IMPLEMENTED = {
            NotificationTypes.ASSESSMENT,
            NotificationTypes.GRADE,
            NotificationTypes.DISCUSSION,
            NotificationTypes.ASSIGNMENT
    };
    private JsonObject settings = new JsonObject();

    private static SettingsStore notificationSettings;

    private SettingsStore() {}

    public static SettingsStore getInstance() {
        if (notificationSettings == null) {
            notificationSettings = new SettingsStore(new JsonObject());
            notificationSettings.getSettingsFromStorage();
            notificationSettings.setDefaultSettings();
        }
        return notificationSettings;
    }

    private SettingsStore(JsonObject settings) {
        this.settings = settings;
    }

    public void put(String settingId, boolean isEnabled) {
        this.settings.addProperty(settingId, isEnabled);
        AppStorage.put(AppStorage.SETTINGS, this.settings.toString(), AnalyticsApplication.getContext());
    }

    public Boolean get(String settingId) {
        if (this.settings.has(settingId)) {
            return this.settings.get(settingId).getAsBoolean();
        }
        return SETTING_ENABLED;
    }

    private void setDefaultSettings() {
        for (String setting : SETTINGS_NOT_IMPLEMENTED) {
            put(setting, SETTING_DISABLED);
        }
    }

    private void getSettingsFromStorage() {
        String settings = AppStorage.get(AppStorage.SETTINGS, AnalyticsApplication.getContext());
        if (settings == null || "".equals(settings)) {
            return;
        }
        JsonParser parser = new JsonParser();
        this.settings = parser.parse(settings).getAsJsonObject();
    }

    public void saveSettings() {
        Context context = AnalyticsApplication.getContext();
        AppStorage.put(AppStorage.SETTINGS, SettingsStore.getInstance().toString(), AnalyticsApplication.getContext());
        String settingsUrl = context.getString(R.string.dispatch_base) +
                context.getString(R.string.dispatch_settings);

        HttpQueue.getInstance(AnalyticsApplication.getContext()).addToRequestQueue(
                new SettingsRequest(settingsUrl, response ->
                        Log.i(TAG, "Settings saved")
                ), null);
    }

    public JsonObject getSettings() {
        return this.settings;
    }

    public void clear() {
        notificationSettings = new SettingsStore(new JsonObject());
    }

    public String toString() {
        return this.settings.toString();
    }
}
