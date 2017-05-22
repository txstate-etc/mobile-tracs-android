package edu.txstate.mobile.tracs.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.HashMap;

import edu.txstate.mobile.tracs.NotificationsActivity;
import edu.txstate.mobile.tracs.R;
import edu.txstate.mobile.tracs.gestures.NotificationTouchListener;
import edu.txstate.mobile.tracs.notifications.NotificationsBundle;
import edu.txstate.mobile.tracs.notifications.TracsAppNotification;
import edu.txstate.mobile.tracs.notifications.tracs.TracsNotification;
import edu.txstate.mobile.tracs.util.FontAwesome;

public class NotificationsAdapter extends BaseAdapter {

    private static final String TAG = "NotificationsAdapter";
    private int badgeCount;

    private NotificationsBundle tracsAppNotifications;
    private Context context;
    HashMap<String, Integer> idMap = new HashMap<>();

    public NotificationsAdapter(NotificationsBundle notifications, Context context) {
        this.tracsAppNotifications = notifications;
        this.context = context;
        for (int i = 0; i < this.tracsAppNotifications.size(); i++) {
            idMap.put(tracsAppNotifications.get(i).getDispatchId(), i);
        }
        this.badgeCount = this.tracsAppNotifications.totalUnread();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public int getCount() {
        return tracsAppNotifications.size();
    }

    @Override
    public Object getItem(int position) {
        return tracsAppNotifications.get(position);
    }

    public void remove(Object notification) {
        TracsAppNotification toBeRemoved = (TracsAppNotification) notification;
        if (!toBeRemoved.hasBeenRead()) {
            this.badgeCount--;
        }
        this.tracsAppNotifications.remove(notification);
        notifyDataSetChanged();
        NotificationsActivity.class.cast(this.context).setBadgeCount(badgeCount);
    }

    @Override
    public long getItemId(int position) {
        String mapId = TracsNotification.class.cast(getItem(position)).getDispatchId();
        return idMap.get(mapId);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.notification_row, parent, false);
        }
        RowHolder rowHolder = new RowHolder();
        rowHolder.fontAwesomeIcon = (FontAwesome) convertView.findViewById(R.id.notification_icon);
        rowHolder.siteName = (TextView) convertView.findViewById(R.id.notification_site_name);
        rowHolder.titleText = (TextView) convertView.findViewById(R.id.notification_title);

        TracsNotification content = TracsNotification.class.cast(getItem(position));

        int typeface = content.hasBeenRead() ? Typeface.NORMAL : Typeface.BOLD;
        rowHolder.titleText.setTypeface(null, typeface);
        rowHolder.siteName.setTypeface(null, typeface);


        rowHolder.titleText.setText(content.getTitle());
        rowHolder.siteName.setText(content.getSiteName());
        rowHolder.fontAwesomeIcon.setText(R.string.fa_bullhorn);

        convertView.setTag(rowHolder);
        convertView.setOnTouchListener(new NotificationTouchListener(parent.getContext()));
        return convertView;
    }

    static class RowHolder {
        FontAwesome fontAwesomeIcon;
        TextView siteName;
        TextView titleText;
    }
}
