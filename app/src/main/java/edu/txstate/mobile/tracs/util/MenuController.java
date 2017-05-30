package edu.txstate.mobile.tracs.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import edu.txstate.mobile.tracs.AboutActivity;
import edu.txstate.mobile.tracs.SupportActivity;
import edu.txstate.mobile.tracs.FeedbackActivity;
import edu.txstate.mobile.tracs.MainActivity;
import edu.txstate.mobile.tracs.NotificationSettingsActivity;
import edu.txstate.mobile.tracs.NotificationsActivity;
import edu.txstate.mobile.tracs.R;

public class MenuController {
    private static final String HOME_ACTIVITY = "MainActivity";
    private static final String NOTIFICATIONS_ACTIVITY = "NotificationActivity";
    private static final String ABOUT_ACTIVITY = "edu.txstate.mobile.tracs.AboutActivity";
    private static final String SETTINGS_ACTIVITY = "NotificationSettingsActivity";
    private static final String FEEDBACK_ACTIVITY = "FeedbackActivity";
    private static final String CONTACT_ACTIVITY = "SupportActivity";

    private static final String DUBLABS_APP = "edu.txstate.mobileapp";

    public static boolean handleMenuClick(int menuId, Context context) {
        switch (menuId) {
            case R.id.menu_notifications:
                launchIntent(NOTIFICATIONS_ACTIVITY, NotificationsActivity.class, context);
                break;
            case R.id.menu_notification_settings:
                launchIntent(SETTINGS_ACTIVITY, NotificationSettingsActivity.class, context);
                break;
            case R.id.menu_about_us:
                launchIntent(ABOUT_ACTIVITY, AboutActivity.class, context);
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
            case R.id.menu_feedback:
                launchIntent(FEEDBACK_ACTIVITY, FeedbackActivity.class, context);
                break;
            case R.id.menu_tracs_support:
                launchIntent(CONTACT_ACTIVITY, SupportActivity.class, context);
                break;
            case android.R.id.home:
                launchIntent(HOME_ACTIVITY, MainActivity.class, context);
                break;
            default:
                return false;
        }
        return true;
    }

    private static void configureBackstack(Intent intent, Context context, String menuName) {
        String callingActivity = Activity.class.cast(context).getLocalClassName();
        if (callingActivity.equals(menuName)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        }
    }

    private static void launchIntent(String activity, Class clazz, Context context) {
        Intent intent = new Intent(context, clazz);
        configureBackstack(intent, context, activity);
        context.startActivity(intent);
    }
}
