package edu.txstate.mobile.tracs.util;

import android.content.Context;
import android.content.Intent;

import edu.txstate.mobile.tracs.NotificationsActivity;
import edu.txstate.mobile.tracs.R;
import edu.txstate.mobile.tracs.SettingsActivity;

public class MenuController {
    public static boolean handleMenuClick(int menuId, Context context) {
        switch (menuId) {
            case R.id.notifications_menu:
                Intent notificationsIntent = new Intent(context, NotificationsActivity.class);
                context.startActivity(notificationsIntent);
                break;
            case R.id.menu_settings:
                Intent settingsIntent = new Intent(context, SettingsActivity.class);
                context.startActivity(settingsIntent);
                break;
            default:
                return false;
        }
        return true;
    }
}
