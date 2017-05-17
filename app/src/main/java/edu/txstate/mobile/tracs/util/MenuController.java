package edu.txstate.mobile.tracs.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import edu.txstate.mobile.tracs.AnalyticsApplication;
import edu.txstate.mobile.tracs.MainActivity;
import edu.txstate.mobile.tracs.NotificationsActivity;
import edu.txstate.mobile.tracs.R;
import edu.txstate.mobile.tracs.SettingsActivity;

public class MenuController {
    private static final String MAIN_ACTIVITY = "MainActivity";
    private static final String NOTIFICATIONS_ACTIVITY = "NotificationActivity";
    private static final String SETTINGS_ACTIVITY = "SettingsActivity";

    private static final String DUBLABS_APP = "edu.txstate.mobileapp";

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
            case R.id.menu_dublabs:
                Intent dubLabsIntent = context.getPackageManager().getLaunchIntentForPackage(DUBLABS_APP);
                if (dubLabsIntent != null) { //Dublabs app is installed
                    context.startActivity(dubLabsIntent);
                } else { //Dublabs app was not installed
                    try { //to go to the play store and get it
                        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + DUBLABS_APP)));
                    } catch (ActivityNotFoundException e) { //the play store isn't installed
                        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + DUBLABS_APP)));
                    }
                }
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
