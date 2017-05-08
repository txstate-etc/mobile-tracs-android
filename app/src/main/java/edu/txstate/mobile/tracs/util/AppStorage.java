package edu.txstate.mobile.tracs.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.securepreferences.SecurePreferences;

import edu.txstate.mobile.tracs.R;

public class AppStorage {

    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String SESSION_ID = "sessionId";
//    public static final String NOTIFICATION_ID = "notificationId";

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

    public static void remove(String key, Context context) {
        if (keyIsValid(key)) {
            AppStorage.getPrefs(context).edit().remove(key).apply();
        }
    }

    private static boolean keyIsValid(String key) {
        return USERNAME.equals(key) || PASSWORD.equals(key)
                || SESSION_ID.equals(key);
    }
}
