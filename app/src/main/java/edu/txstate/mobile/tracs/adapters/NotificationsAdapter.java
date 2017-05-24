package edu.txstate.mobile.tracs.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.swipe.adapters.BaseSwipeAdapter;

import java.util.HashMap;

import edu.txstate.mobile.tracs.AnalyticsApplication;
import edu.txstate.mobile.tracs.MainActivity;
import edu.txstate.mobile.tracs.NotificationsActivity;
import edu.txstate.mobile.tracs.R;
import edu.txstate.mobile.tracs.notifications.NotificationsBundle;
import edu.txstate.mobile.tracs.notifications.TracsAppNotification;
import edu.txstate.mobile.tracs.notifications.tracs.TracsNotification;
import edu.txstate.mobile.tracs.util.FontAwesome;
import edu.txstate.mobile.tracs.util.async.StatusUpdate;

public class NotificationsAdapter extends BaseSwipeAdapter {

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

    public void clear() {
        for (int i = 0; i < getCount(); i++) {
            remove(getItem(i));
        }
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
    public int getSwipeLayoutResourceId(int i) {
        return R.id.swipe_layout;
    }

    @Override
    public View generateView(int i, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.notification_row, viewGroup, false);
    }

    @Override
    public void fillValues(int position, View convertView) {
        if (convertView == null) {
            convertView = ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.notification_row, null);
        }
        RowHolder rowHolder = new RowHolder();
        rowHolder.fontAwesomeIcon = (FontAwesome) convertView.findViewById(R.id.notification_icon);
        rowHolder.siteName = (TextView) convertView.findViewById(R.id.notification_site_name);
        rowHolder.titleText = (TextView) convertView.findViewById(R.id.notification_title);
        rowHolder.row = (RelativeLayout) convertView.findViewById(R.id.notification_row);

        TracsNotification content = TracsNotification.class.cast(getItem(position));

        int typeface;
        if (!content.hasBeenRead()) {
            typeface = Typeface.BOLD;
            rowHolder.fontAwesomeIcon.setBackgroundColor(AnalyticsApplication
                    .getContext().getResources().getColor(R.color.unreadNotificationBackground));
        } else {
            typeface = Typeface.NORMAL;
            rowHolder.fontAwesomeIcon.setBackgroundColor(AnalyticsApplication
                    .getContext().getResources().getColor(R.color.readNotificationBackground));
        }


        rowHolder.titleText.setTypeface(null, typeface);
        rowHolder.titleText.setText(content.getTitle());
        rowHolder.siteName.setText(content.getSiteName());
        rowHolder.fontAwesomeIcon.setText(R.string.fa_bullhorn);

        convertView.findViewById(R.id.delete).setOnClickListener(v -> {
            deleteNotification(position);
        });

        convertView.setTag(rowHolder);
        rowHolder.row.setOnClickListener(v -> {
            TracsNotification notification = (TracsNotification) getItem(position);
            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra("url", notification.getUrl());
            context.startActivity(intent);
            new StatusUpdate().updateRead(notification);
        });
    }

    private void deleteNotification(int position) {
        TracsAppNotification notification = (TracsAppNotification) getItem(position);
        remove(notification);
        closeItem(position);
        new StatusUpdate().updateCleared(notification);
    }

    class RowHolder {
        FontAwesome fontAwesomeIcon;
        TextView siteName;
        TextView titleText;
        RelativeLayout row;
    }
}
