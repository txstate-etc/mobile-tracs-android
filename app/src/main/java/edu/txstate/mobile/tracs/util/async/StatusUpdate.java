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

public class StatusUpdate {
    private Context context = AnalyticsApplication.getContext();

    public void updateSeen(NotificationsBundle bundle) {
        new Seen().execute(bundle);
    }

    public void updateRead(TracsAppNotification notification) {
        new Read().execute(notification);
    }

    public void updateCleared(TracsAppNotification notification) {
        new Cleared().execute(notification);
    }

    private String formUrl(TracsAppNotification notification) {
        String url = context.getString(R.string.dispatch_base) + context.getString(R.string.dispatch_notifications);
        url += "/";
        url += notification.getDispatchId();
        url += "?token=";
        url += FirebaseInstanceId.getInstance().getToken();
        return url;
    }

    private void sendUpdate(TracsAppNotification notification, NotificationStatus status) {
        String url = formUrl(notification);

        //Should not need to cancel this request
        HttpQueue.getInstance(context).addToRequestQueue(
                new DispatchUpdateRequest(url, status),
                null
        );
    }

    private class Seen extends AsyncTask<NotificationsBundle, Void, Void> {

        @Override
        protected Void doInBackground(NotificationsBundle... tracsAppNotifications) {
            NotificationsBundle notifications = tracsAppNotifications[0];
            NotificationStatus status = new NotificationStatus(true, false, false);
            for (int i = 0; i < notifications.size(); i++) {
                TracsAppNotification notification = notifications.get(i);
                if (!notification.hasBeenSeen()) {
                    sendUpdate(notification, status);
                }
            }
            return null;
        }
    }

    private class Read extends AsyncTask<TracsAppNotification, Void, Void> {
        @Override
        protected Void doInBackground(TracsAppNotification... tracsAppNotifications) {
            TracsAppNotification notification = tracsAppNotifications[0];
            NotificationStatus status = new NotificationStatus(true, true, false);
            if (!notification.hasBeenRead()) {
                sendUpdate(notification, status);
            }
            return null;
        }
    }

    private class Cleared extends AsyncTask<TracsAppNotification, Void, Void> {
        @Override
        protected Void doInBackground(TracsAppNotification... tracsAppNotifications) {
            TracsAppNotification notification = tracsAppNotifications[0];
            NotificationStatus status = new NotificationStatus(true, notification.hasBeenRead(), true);
            if (!notification.hasBeenCleared()) {
                sendUpdate(notification, status);
            }
            return null;
        }
    }
}

