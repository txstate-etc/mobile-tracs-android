package edu.txstate.mobile.tracs.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.txstate.mobile.tracs.R;
import edu.txstate.mobile.tracs.notifications.NotificationTypes;
import edu.txstate.mobile.tracs.notifications.NotificationsBundle;
import edu.txstate.mobile.tracs.notifications.TracsAppNotification;
import edu.txstate.mobile.tracs.notifications.tracs.TracsAnnouncement;

public class NotificationsRVAdapter extends RecyclerView.Adapter<NotificationViewHolder> {

    private NotificationsBundle tracsNotifications;

    public NotificationsRVAdapter(NotificationsBundle bundle) {
        this.tracsNotifications = bundle;
    }

    @Override
    public NotificationViewHolder onCreateViewHolder(ViewGroup parent, int position) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_row_item, parent, false);
        return new NotificationViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(NotificationViewHolder holder, int position) {
        final TracsAppNotification notification = tracsNotifications.get(position);
        if (NotificationTypes.ANNOUNCEMENT.equals(notification.getType())) {
            TracsAnnouncement announcement = TracsAnnouncement.class.cast(notification);
            holder.getNormalLayout().setVisibility(View.VISIBLE);
            holder.getNotificationIcon().setText(R.string.fa_bullhorn);
            holder.getNotificationTitle().setText(announcement.getTitle());
            holder.getNotificationSite().setText(announcement.getSiteName());
        }
    }

    public void remove(int position) {
        TracsAppNotification notification = tracsNotifications.get(position);
        tracsNotifications.remove(notification.getDispatchId());
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return tracsNotifications.size();
    }
}
