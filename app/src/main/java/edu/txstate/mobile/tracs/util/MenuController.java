package edu.txstate.mobile.tracs.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import edu.txstate.mobile.tracs.MainActivity;
import edu.txstate.mobile.tracs.NotificationsActivity;
import edu.txstate.mobile.tracs.R;
import edu.txstate.mobile.tracs.SettingsActivity;

public class MenuController {
    private static final String MAIN_ACTIVITY = "MainActivity";
    private static final String NOTIFICATIONS_ACTIVITY = "NotificationActivity";
    private static final String SETTINGS_ACTIVITY = "SettingsActivity";

    public static boolean handleMenuClick(int menuId, Context context) {
        switch (menuId) {
            case R.id.menu_notifications:
                Intent notificationsIntent = new Intent(context, NotificationsActivity.class);
                checkBackStack(notificationsIntent, context, NOTIFICATIONS_ACTIVITY);
                context.startActivity(notificationsIntent);
                break;
            case R.id.menu_settings:
                Intent settingsIntent = new Intent(context, SettingsActivity.class);
                checkBackStack(settingsIntent, context, SETTINGS_ACTIVITY);
                context.startActivity(settingsIntent);
                break;
            case R.id.menu_home:
                Intent homeIntent = new Intent(context, MainActivity.class);
                checkBackStack(homeIntent, context, MAIN_ACTIVITY);
                context.startActivity(homeIntent);
                break;
            default:
                return false;
        }
        return true;
    }

    private static void checkBackStack(Intent intent, Context context, String menuName) {
        String callingActivity = Activity.class.cast(context).getLocalClassName();
        if (callingActivity.equals(menuName)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        }
    }
}
