package edu.txstate.mobileapp.mobileandroid.notifications;

import android.app.ListActivity;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

import java.util.ArrayList;
import java.util.HashMap;

public class NotificationsListLoader extends ArrayAdapter<TracsAppNotification> {

    HashMap<String, Integer> idMap = new HashMap<>();

    public NotificationsListLoader(@NonNull Context context,
                            @LayoutRes int resource,
                            NotificationsBundle notifications) {
        super(context, resource, notifications.getAllNotifications());
        ArrayList<TracsAppNotification> tracsNotifications = notifications.getAllNotifications();
        for (int i = 0; i < tracsNotifications.size(); i++) {
            idMap.put(tracsNotifications.get(i).toString(), i);
        }
    }

    @Override
    public long getItemId(int position) {
        String item = "" + getItem(position);
        return idMap.get(item);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
