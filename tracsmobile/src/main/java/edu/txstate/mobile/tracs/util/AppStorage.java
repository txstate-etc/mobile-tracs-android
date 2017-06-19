package edu.txstate.mobile.tracs.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import edu.txstate.mobile.tracs.R;

@SuppressLint("ApplySharedPref")
public class AppStorage {

    private static final int DEFAULT_MODE = Context.MODE_PRIVATE;
    public static final String USERNAME = "username";
    public static final String SETTINGS = "settings";
    public static final String SESSION_ID = "sessionId";
    public static final String PASSWORD = "password";

    private AppStorage() {}

    private static SharedPreferences getPrefs(Context context) {
        String user_data = context.getText(R.string.user_data).toString();
        return context.getSharedPreferences(user_data, DEFAULT_MODE);
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
            AppStorage.getPrefs(context).edit().putString(key, value).commit();
        }
    }

    public static void remove(String key, Context context) {
        if (keyIsValid(key)) {
            AppStorage.getPrefs(context).edit().remove(key).commit();
        }
    }

    private static boolean keyIsValid(String key) {
        //This is here in case validation of keys is ever desired.
        return true;
    }

    public static void clear(Context context) {
        SharedPreferences prefs = AppStorage.getPrefs(context);
        prefs.edit().clear().commit();
    }

    public static boolean credentialsAreStored(Context context) {
        String username = AppStorage.getPrefs(context).getString(USERNAME, "");
        String password = AppStorage.getPrefs(context).getString(PASSWORD, "");
        return username.length() > 0 && password.length() > 0;
    }
}
