package edu.txstate.mobileapp.tracscompanion.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.securepreferences.SecurePreferences;

import edu.txstate.mobileapp.tracscompanion.R;

public class AppStorage {

    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String TRACS_ID = "userEid";
    public static final String SESSION_ID = "sessionId";
    public static final String NOTIFICATION_ID = "notificationId";

    private AppStorage() {}

    private static SharedPreferences getPrefs(Context context) {
        String encryptionKey = AppInstanceId.getKey(context).toString();
        String user_data = context.getText(R.string.user_data).toString();
        return new SecurePreferences(context, encryptionKey, user_data);
    }

    public static String get(String key, Context context) {
        String value = null;
        if (keyIsValid(key)) {
            value = AppStorage.getPrefs(context).getString(key, "");
        }
        return value == null ? "" : value;
    }

    public static void put(String key, String value, Context context) {
        if (keyIsValid(key)) {
            AppStorage.getPrefs(context).edit().putString(key, value).apply();
        }
    }

    private static boolean keyIsValid(String key) {
        return USERNAME.equals(key) || PASSWORD.equals(key)
                || TRACS_ID.equals(key) || SESSION_ID.equals(key)
                || NOTIFICATION_ID.equals(key);
    }
}
