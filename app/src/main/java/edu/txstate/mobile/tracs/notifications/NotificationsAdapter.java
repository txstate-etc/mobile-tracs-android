package edu.txstate.mobile.tracs.notifications;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.HashMap;

import edu.txstate.mobile.tracs.R;
import edu.txstate.mobile.tracs.notifications.tracs.TracsNotification;
import edu.txstate.mobile.tracs.util.FontAwesome;

public class NotificationsAdapter extends BaseAdapter {

    private NotificationsBundle tracsAppNotifications;
    private Context context;
    HashMap<String, Integer> idMap = new HashMap<>();

    public NotificationsAdapter(NotificationsBundle notifications, Context context) {
        this.tracsAppNotifications = notifications;
        this.context = context;
    }

    @Override
    public int getCount() {
        return tracsAppNotifications.size();
    }

    @Override
    public Object getItem(int position) {
        return tracsAppNotifications.get(position);
    }

    @Override
    public long getItemId(int position) {
        return tracsAppNotifications.get(position).hashCode();
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

        rowHolder.titleText.setText(content.getTitle());
        rowHolder.siteName.setText(content.getSiteName());
        rowHolder.fontAwesomeIcon.setText(R.string.fa_bullhorn);

        convertView.setTag(rowHolder);

        return convertView;
    }

    static class RowHolder {
        FontAwesome fontAwesomeIcon;
        TextView siteName;
        TextView titleText;
    }
}
