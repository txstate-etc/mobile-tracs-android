package edu.txstate.mobile.tracs.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import edu.txstate.mobile.tracs.R;
import edu.txstate.mobile.tracs.util.FontAwesome;

class NotificationViewHolder extends RecyclerView.ViewHolder {
    private RelativeLayout layout;

    private FontAwesome notificationIcon;
    private TextView notificationTitle;
    private TextView notificationSite;

    NotificationViewHolder(View itemView) {
        super(itemView);

        layout = itemView.findViewById(R.id.notification_row);
        notificationIcon = itemView.findViewById(R.id.n_icon);
        notificationSite = itemView.findViewById(R.id.n_site);
        notificationTitle= itemView.findViewById(R.id.n_title);
    }

    RelativeLayout getLayout() {
        return layout;
    }

    FontAwesome getNotificationIcon() {
        return notificationIcon;
    }

    TextView getNotificationTitle() {
        return notificationTitle;
    }

    TextView getNotificationSite() {
        return notificationSite;
    }
}
