package edu.txstate.mobileapp.mobileandroid.util;

import android.os.AsyncTask;

import edu.txstate.mobileapp.mobileandroid.notifications.DispatchNotification;
import edu.txstate.mobileapp.mobileandroid.notifications.NotificationTypes;
import edu.txstate.mobileapp.mobileandroid.notifications.NotificationsBundle;
import edu.txstate.mobileapp.mobileandroid.notifications.TracsAppNotification;
import edu.txstate.mobileapp.mobileandroid.notifications.listeners.TracsListener;
import edu.txstate.mobileapp.mobileandroid.requests.AsyncTaskFactory;
import edu.txstate.mobileapp.mobileandroid.requests.Task;

public class TracsClient {
    private static final String tracsUrl = "https://tracs.txstate.edu";
    private static final String tracsBase = tracsUrl + "/direct";
    private static final String announcementUrl = tracsBase + "/announcement/";
    static final String siteUrl = tracsBase + "/site";
    static final String portalUrl = tracsUrl + "/portal";
    static final String loginUrl = tracsUrl + "/portal/login";
    static final String logoutUrl = tracsUrl + "/portal/pda/?force.logout=yes";
    static final String entityId = "831342dd-fdb6-4878-8b3c-1d29ecb06a14:main:d36eb344-774b-43cf-b2ab-826126161129";

    private static TracsClient tracsClient;

    private TracsClient() {}

    public static TracsClient getInstance() {
        if (tracsClient == null) {
            tracsClient = new TracsClient();
        }
        return tracsClient;
    }

    public static String makeUrl() {
        return tracsBase;
    }

    public static String makeUrl(String type) {
        String desiredUrl;
        switch (type) {
            case NotificationTypes.ANNOUNCEMENT:
                desiredUrl = announcementUrl;
                break;
            case NotificationTypes.ASSESSMENT:
                desiredUrl = tracsBase;
                break;
            case NotificationTypes.ASSIGNMENT:
                desiredUrl = tracsBase;
                break;
            case NotificationTypes.DISCUSSION:
                desiredUrl = tracsBase;
                break;
            case NotificationTypes.GRADE:
                desiredUrl = tracsBase;
                break;
            default:
                desiredUrl = tracsBase;
        }
        return desiredUrl;
    }

    public void getNotifications(NotificationsBundle notifications, TracsListener listener) {
        for (TracsAppNotification notification : notifications) {
            String entityId = DispatchNotification.class.cast(notification).getObjectId();
            AsyncTaskFactory.createTask(Task.TRACS_NOTIFICATION, listener)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, announcementUrl, entityId);
        }
    }
}
