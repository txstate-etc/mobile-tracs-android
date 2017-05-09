package edu.txstate.mobile.tracs.util.async;

import android.content.Context;
import android.os.AsyncTask;

import com.google.firebase.iid.FirebaseInstanceId;

import edu.txstate.mobile.tracs.AnalyticsApplication;
import edu.txstate.mobile.tracs.NotificationsActivity;
import edu.txstate.mobile.tracs.R;
import edu.txstate.mobile.tracs.notifications.NotificationsBundle;
import edu.txstate.mobile.tracs.notifications.TracsAppNotification;
import edu.txstate.mobile.tracs.util.NotificationStatus;
import edu.txstate.mobile.tracs.util.http.HttpQueue;
import edu.txstate.mobile.tracs.util.http.requests.DispatchUpdateRequest;

public class StatusUpdate extends AsyncTask<NotificationsBundle, Void, Void> {
    @Override
    protected Void doInBackground(NotificationsBundle... tracsAppNotifications) {
        NotificationsBundle notifications = tracsAppNotifications[0];
        Context context = AnalyticsApplication.getContext();
        for (int i = 0; i < notifications.size(); i++) {
            TracsAppNotification notification = notifications.get(i);
            if (!notification.hasBeenSeen()) {
                String dispatchId = notification.getDispatchId();
                String url = context.getString(R.string.dispatch_base) + context.getString(R.string.dispatch_notifications);
                url += "/";
                url += dispatchId;
                url += "?token=";
                url += FirebaseInstanceId.getInstance().getToken();
                NotificationStatus status = new NotificationStatus(true, false, false);
                HttpQueue.getInstance(AnalyticsApplication.getContext()).addToRequestQueue(
                        new DispatchUpdateRequest(url, dispatchId, status),
                        NotificationsActivity.TAG);
            }
        }
        return null;
    }
}
