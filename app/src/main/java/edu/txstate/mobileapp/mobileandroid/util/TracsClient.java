package edu.txstate.mobileapp.mobileandroid.util;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonStreamParser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import edu.txstate.mobileapp.mobileandroid.notifications.NotificationTypes;
import edu.txstate.mobileapp.mobileandroid.notifications.NotificationsBundle;
import edu.txstate.mobileapp.mobileandroid.notifications.TracsAppNotification;
import edu.txstate.mobileapp.mobileandroid.notifications.listeners.NotificationListener;
import edu.txstate.mobileapp.mobileandroid.notifications.tracs.TracsAnnouncement;
import edu.txstate.mobileapp.mobileandroid.notifications.tracs.TracsNotification;

public class TracsClient {
    static final String tracsUrl = "https://tracs.txstate.edu";
    static final String tracsBase = tracsUrl + "/direct";
    static final String announcementUrl = tracsBase + "/announcement/";
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
                desiredUrl =  tracsBase;
                break;
            case NotificationTypes.ASSIGNMENT:
                desiredUrl =  tracsBase;
                break;
            case NotificationTypes.DISCUSSION:
                desiredUrl =  tracsBase;
                break;
            case NotificationTypes.GRADE:
                desiredUrl =  tracsBase;
                break;
            default:
                desiredUrl = tracsBase;
        }
        return desiredUrl;
    }

    public void getAnnouncements(NotificationsBundle notifications, NotificationListener listener) {
        for(TracsAppNotification notification : notifications) {
            if (NotificationTypes.ANNOUNCEMENT.equals(notification.getType())) {

            }
        }
        new GetTracsNotification(listener).execute(announcementUrl, entityId);
    }

    private class GetTracsNotification extends AsyncTask<String, Void, String> {
        private final String TAG = "GetTracsNotification";
        private final String FORBIDDEN_ERROR = "Access to resource is forbidden";
        private NotificationListener listener;

        GetTracsNotification(NotificationListener listener) {
            this.listener = listener;
        }

        @Override
        protected String doInBackground(String... params) {
            String entityId = params[1];
            String response = "";
            try {
                URL url = new URL(params[0] + entityId + ".json");
                HttpURLConnection client = (HttpURLConnection) url.openConnection();
                client.setRequestProperty("Cookie", "JSESSIONID=f0048618-1342-4170-b084-0b1360f5102a.tracs-app-mcs-1-7;");
                if (client.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN) {
                    //TODO: Add logic to login to tracks
                    throw new IOException(FORBIDDEN_ERROR);
                }
                response = JsonResponse.parse(client.getInputStream());
            } catch (IOException e) {
                Log.d(TAG, e.getMessage());
            }
            return response;
        }

        @Override
        protected void onPostExecute(String notificationData) {
            TracsNotification announcement;
            if (notificationData.isEmpty()) {
                announcement = new TracsAnnouncement();
            } else {
                JsonStreamParser parser = new JsonStreamParser(notificationData);
                JsonObject notification = null;

                while (parser.hasNext()) {
                    notification = (JsonObject) parser.next();
                }
                announcement = new TracsAnnouncement(notification);
            }
            listener.onNotificationAvailable(announcement);
        }
    }
}
