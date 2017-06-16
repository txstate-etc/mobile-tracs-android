package edu.txstate.mobile.tracs.util;

import android.content.Context;
import android.content.SharedPreferences;

import edu.txstate.mobile.tracs.AnalyticsApplication;
import edu.txstate.mobile.tracs.BuildConfig;

public class VersionTracker {
    private static VersionTracker versionTracker;

    private VersionTracker() {}

    public static VersionTracker getInstance() {
        if (versionTracker == null) {
            versionTracker = new VersionTracker();
        }
        return versionTracker;
    }

    /**
     * This method will let you know that the user is launching a new version of the app, or
     * has never launched the app. This will only return True once.
     * @return True if this is a newly released version
     */
    public boolean isNewVersion() {
        String version = getCurrentVersion();
        boolean isNewVersion = !version.equalsIgnoreCase(getPreviousVersion());
        if (isNewVersion) {
            updateVersion();
        }
        return isNewVersion;
    }

    private String getPreviousVersion() {
        SharedPreferences prefs = AnalyticsApplication.getContext().getSharedPreferences("version", Context.MODE_PRIVATE);
        return prefs.getString("previousVersion", "");
    }

    private String getCurrentVersion() {
        return BuildConfig.VERSION_NAME;
    }

    private void updateVersion() {
        getPrefs().edit().putString("previousVersion", getCurrentVersion()).apply();
    }

    private SharedPreferences getPrefs() {
        return AnalyticsApplication.getContext().getSharedPreferences("version", Context.MODE_PRIVATE);
    }
}
