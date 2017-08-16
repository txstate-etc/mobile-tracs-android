package edu.txstate.mobile.tracs.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import edu.txstate.mobile.tracs.AnalyticsApplication;
import edu.txstate.mobile.tracs.MainActivity;
import edu.txstate.mobile.tracs.R;
import edu.txstate.mobile.tracs.notifications.NotificationTypes;
import edu.txstate.mobile.tracs.notifications.NotificationsBundle;
import edu.txstate.mobile.tracs.notifications.tracs.TracsAnnouncement;
import edu.txstate.mobile.tracs.notifications.tracs.TracsDiscussion;
import edu.txstate.mobile.tracs.notifications.tracs.TracsNotification;
import edu.txstate.mobile.tracs.util.FontAwesome;
import edu.txstate.mobile.tracs.util.async.StatusUpdate;

public class NotificationsRVAdapter extends RecyclerView.Adapter<NotificationViewHolder> {

    private static final String TAG = "NotificationsRVAdapter";
    private NotificationsBundle tracsNotifications;
    private Context context;

    public NotificationsRVAdapter(NotificationsBundle bundle, Context context) {
        this.tracsNotifications = bundle;
        this.context = context;
    }

    @Override
    public NotificationViewHolder onCreateViewHolder(ViewGroup parent, int position) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_row_item, parent, false);

        return new NotificationViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(NotificationViewHolder holder, int position) {
        final TracsNotification notification = (TracsNotification) tracsNotifications.get(position);
        final String onClickUrl = notification.getUrl();
        RelativeLayout layout = holder.getLayout();
        int typeface;

        switch (notification.getType()) {
            case NotificationTypes.ANNOUNCEMENT:
                TracsAnnouncement announcement = TracsAnnouncement.class.cast(notification);
                layout.setBackgroundColor(context.getResources().getColor(R.color.readNotificationBackground));

                holder.getNotificationIcon().setText(R.string.fa_bullhorn);
                holder.getNotificationTitle().setText(announcement.getTitle());
                holder.getNotificationSite().setText(announcement.getSiteName());
                break;
            case NotificationTypes.DISCUSSION:
                TracsDiscussion discussion = TracsDiscussion.class.cast(notification);
                layout.setBackgroundColor(context.getResources().getColor(R.color.readNotificationBackground));

                holder.getNotificationIcon().setText(R.string.fa_comments);
                holder.getNotificationTitle().setText(discussion.getTitle());
                holder.getNotificationSite().setText(discussion.getSiteName());
                break;
            default:
                break;
        }

        FontAwesome nIcon = holder.getNotificationIcon();
        if (!notification.hasBeenRead()) {
            typeface = Typeface.BOLD;
            nIcon.setBackground(context.getResources().getDrawable(R.drawable.notification_icon_bg));
            nIcon.setTextColor(context.getResources().getColor(R.color.unreadBullhornColor));
        } else {
            typeface = Typeface.NORMAL;
            nIcon.setBackgroundColor(context.getResources().getColor(R.color.readNotificationBackground));
            nIcon.setTextColor(context.getResources().getColor(R.color.readBullhornColor));
        }
        holder.getNotificationTitle().setTypeface(null, typeface);

        layout.setOnClickListener(view -> {
            Tracker tracker = AnalyticsApplication.getDefaultTracker();
            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory(context.getString(R.string.notification_event))
                    .setAction(context.getString(R.string.click_event))
                    .setLabel(notification.getType())
                    .build());
            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra("url", onClickUrl);
            StatusUpdate.updateRead(notification);
            context.startActivity(intent);
        });
    }

    public void remove(int position) {
        TracsNotification notification = (TracsNotification) tracsNotifications.get(position);
        tracsNotifications.remove(notification.getDispatchId());
        Tracker tracker = AnalyticsApplication.getDefaultTracker();
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(context.getString(R.string.notification_event))
                .setAction(context.getString(R.string.cleared_event))
                .setLabel(notification.getType())
                .build());
        StatusUpdate.updateCleared(notification);

        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return tracsNotifications.size();
    }
}
