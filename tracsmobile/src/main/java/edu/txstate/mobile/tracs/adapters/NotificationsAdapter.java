package edu.txstate.mobile.tracs.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.HashMap;

import edu.txstate.mobile.tracs.AnalyticsApplication;
import edu.txstate.mobile.tracs.BuildConfig;
import edu.txstate.mobile.tracs.MainActivity;
import edu.txstate.mobile.tracs.R;
import edu.txstate.mobile.tracs.notifications.NotificationsBundle;
import edu.txstate.mobile.tracs.notifications.TracsAppNotification;
import edu.txstate.mobile.tracs.notifications.tracs.TracsNotification;
import edu.txstate.mobile.tracs.util.FontAwesome;
import edu.txstate.mobile.tracs.util.async.StatusUpdate;

public class NotificationsAdapter extends BaseSwipeAdapter {

    private static final String TAG = "NotificationsAdapter";

    private NotificationsBundle tracsAppNotifications;
    private Context context;
    private HashMap<String, Integer> idMap = new HashMap<>();
    private int lastDeletedPosition = -1;
    private final NotificationSwipeListener swipeListener;

    public NotificationsAdapter(NotificationsBundle notifications, Context context) {
        this.tracsAppNotifications = notifications;
        this.context = context;
        this.swipeListener = new NotificationSwipeListener();
        for (int i = 0; i < this.tracsAppNotifications.size(); i++) {
            idMap.put(tracsAppNotifications.get(i).getDispatchId(), tracsAppNotifications.get(i).getDispatchId().hashCode());
        }
    }

    @Override
    public boolean hasStableIds() {
        return false;
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
        this.tracsAppNotifications.remove(notification);
        try {
            this.idMap.remove(TracsAppNotification.class.cast(notification).getDispatchId());
        } catch (ClassCastException e) {
            Log.e(TAG, "Could not remove notification from id map");
        }
    }

    @Override
    public long getItemId(int position) {
        String mapId = TracsNotification.class.cast(getItem(position)).getDispatchId();
        return this.idMap.get(mapId);
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe_layout;
    }

    @Override
    public View generateView(int position, ViewGroup viewGroup) {
        View swipeView = LayoutInflater.from(context).inflate(R.layout.notification_row, null);
        return swipeView;
    }

    @Override
    public void fillValues(int position, View swipeView) {
        TracsNotification content = TracsNotification.class.cast(getItem(position));

        Swiperoo swipeLayout = (Swiperoo) swipeView;
        swipeLayout.removeSwipeListener(swipeListener);
        swipeLayout.addSwipeListener(swipeListener);

        swipeLayout.findViewById(R.id.delete).setOnClickListener(v -> {
            swipeLayout.close(false);
            deleteNotification(content, position, swipeLayout);
            this.lastDeletedPosition = position;
            Tracker tracker = AnalyticsApplication.getDefaultTracker();
            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory(context.getString(R.string.notification_event))
                    .setAction(context.getString(R.string.cleared_event))
                    .setLabel(content.getType())
                    .build());
        });

        RowHolder rowHolder = new RowHolder();
        rowHolder.fontAwesomeIcon = swipeLayout.findViewById(R.id.notification_icon);
        rowHolder.siteName = swipeLayout.findViewById(R.id.notification_site_name);
        rowHolder.titleText = swipeLayout.findViewById(R.id.notification_title);
        rowHolder.row = swipeLayout.findViewById(R.id.notification_row);


        rowHolder.titleText.setText(String.valueOf(position) + " - " + content.getTitle());
        rowHolder.siteName.setText(content.getSiteName());

        rowHolder.fontAwesomeIcon.setText(R.string.fa_bullhorn);

        swipeLayout.setBackgroundColor(context.getResources().getColor(R.color.readNotificationBackground));

        int typeface;
        if (!content.hasBeenRead()) {
            typeface = Typeface.BOLD;
            rowHolder.fontAwesomeIcon.setBackground(context.getResources().getDrawable(R.drawable.notification_icon_bg));
            rowHolder.fontAwesomeIcon.setTextColor(context.getResources().getColor(R.color.unreadBullhornColor));
        } else {
            typeface = Typeface.NORMAL;
            rowHolder.fontAwesomeIcon.setBackgroundColor(context.getResources().getColor(R.color.readNotificationBackground));
            rowHolder.fontAwesomeIcon.setTextColor(context.getResources().getColor(R.color.readBullhornColor));
        }

        rowHolder.titleText.setTypeface(null, typeface);

        swipeLayout.setTag(rowHolder);
        rowHolder.row.setOnClickListener(v -> {
            Tracker tracker = AnalyticsApplication.getDefaultTracker();
            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory(context.getString(R.string.notification_event))
                    .setAction(context.getString(R.string.click_event))
                    .setLabel(content.getType())
                    .build());
            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra("url", content.getUrl());
            new StatusUpdate().updateRead(content);
            context.startActivity(intent);
        });
    }

    private boolean hasBeenDeleted(int position) {
        return this.lastDeletedPosition == position;
    }

    private void clearDeletion(int position) {
        this.lastDeletedPosition = -1;
    }

    private void deleteNotification(TracsNotification notification, int position, SwipeLayout swipeLayout) {
        remove(notification);
        this.notifyDataSetChanged();
        Log.i(TAG, "Notification Deleted");
        if (!BuildConfig.DEBUG) {
            new StatusUpdate().updateCleared(notification);
        }
    }

    private class NotificationSwipeListener extends SimpleSwipeListener {
        @Override
        public void onOpen(SwipeLayout layout) {
            super.onOpen(layout);
        }

        @Override
        public void onClose(SwipeLayout layout) {
            super.onClose(layout);
        }
    }

    static class RowHolder {
        FontAwesome fontAwesomeIcon;
        TextView siteName;
        TextView titleText;
        RelativeLayout row;
    }
}

class Swiperoo extends SwipeLayout {

    public Swiperoo(Context context) {
        super(context);
    }

    public Swiperoo(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Swiperoo(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

}