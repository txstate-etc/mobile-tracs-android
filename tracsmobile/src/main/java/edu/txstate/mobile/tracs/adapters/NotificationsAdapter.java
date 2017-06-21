package edu.txstate.mobile.tracs.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
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

    public NotificationsAdapter(NotificationsBundle notifications, Context context) {
        this.tracsAppNotifications = notifications;
        this.context = context;
        for (int i = 0; i < this.tracsAppNotifications.size(); i++) {
            idMap.put(tracsAppNotifications.get(i).getDispatchId(), tracsAppNotifications.get(i).getDispatchId().hashCode());
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
        this.tracsAppNotifications.remove(notification);
        try {
            this.idMap.remove(TracsAppNotification.class.cast(notification).getDispatchId());
        } catch (ClassCastException e) {
            Log.e(TAG, "Could not remove notification from id map");
        }
        this.notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        String mapId = TracsNotification.class.cast(getItem(position)).getDispatchId();
        return this.idMap.get(mapId);
    }

    @Override
    public int getSwipeLayoutResourceId(int i) {
        return R.id.swipe_layout;
    }

    @Override
    public View generateView(int position, ViewGroup viewGroup) {
        View view = LayoutInflater.from(context).inflate(R.layout.notification_row, null);
        return view;
    }

    @Override
    public void fillValues(int position, View swipeView) {
        if (swipeView == null) {
            swipeView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.notification_row, null);
        }
        TracsNotification content = TracsNotification.class.cast(getItem(position));

        SwipeLayout swipeLayout = swipeView.findViewById(getSwipeLayoutResourceId(R.id.swipe_layout));
        swipeLayout.addSwipeListener(new SimpleSwipeListener() {
            @Override
            public void onOpen(SwipeLayout layout) {
                super.onOpen(layout);
            }
        });

        swipeView.findViewById(R.id.delete).setOnClickListener(v -> {
            Tracker tracker = AnalyticsApplication.getDefaultTracker();
            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory(context.getString(R.string.notification_event))
                    .setAction(context.getString(R.string.cleared_event))
                    .setLabel(content.getType())
                    .build());
            deleteNotification(position, swipeLayout);
        });

        RowHolder rowHolder = new RowHolder();
        rowHolder.fontAwesomeIcon = swipeView.findViewById(R.id.notification_icon);
        rowHolder.siteName = swipeView.findViewById(R.id.notification_site_name);
        rowHolder.titleText = swipeView.findViewById(R.id.notification_title);
        rowHolder.row = swipeView.findViewById(R.id.notification_row);


        rowHolder.titleText.setText(content.getTitle());
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

        swipeView.setTag(rowHolder);
        rowHolder.row.setOnClickListener(v -> {
            TracsNotification notification = (TracsNotification) getItem(position);
            Tracker tracker = AnalyticsApplication.getDefaultTracker();
            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory(context.getString(R.string.notification_event))
                    .setAction(context.getString(R.string.click_event))
                    .setLabel(notification.getType())
                    .build());
            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra("url", notification.getUrl());
            context.startActivity(intent);
            new StatusUpdate().updateRead(notification);
        });
    }

    private void deleteNotification(int position, SwipeLayout swipeLayout) {
        TracsAppNotification notification = (TracsAppNotification) getItem(position);
        remove(notification);
        removeShownLayouts(swipeLayout);
        new StatusUpdate().updateCleared(notification);
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        closeAllItems();
    }

    class RowHolder {
        FontAwesome fontAwesomeIcon;
        TextView siteName;
        TextView titleText;
        RelativeLayout row;
    }
}
