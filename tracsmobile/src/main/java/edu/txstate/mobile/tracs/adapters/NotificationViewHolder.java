package edu.txstate.mobile.tracs.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import edu.txstate.mobile.tracs.R;
import edu.txstate.mobile.tracs.util.FontAwesome;

public class NotificationViewHolder extends RecyclerView.ViewHolder {
    private RelativeLayout normalLayout;

    private FontAwesome notificationIcon;
    private TextView notificationTitle;
    private TextView notificationSite;

    public NotificationViewHolder(View itemView) {
        super(itemView);

        normalLayout = itemView.findViewById(R.id.notification_row);
        notificationIcon = itemView.findViewById(R.id.n_icon);
        notificationSite = itemView.findViewById(R.id.n_site);
        notificationTitle= itemView.findViewById(R.id.n_title);
    }

    public RelativeLayout getNormalLayout() {
        return normalLayout;
    }

    public FontAwesome getNotificationIcon() {
        return notificationIcon;
    }

    public TextView getNotificationTitle() {
        return notificationTitle;
    }

    public TextView getNotificationSite() {
        return notificationSite;
    }
}
