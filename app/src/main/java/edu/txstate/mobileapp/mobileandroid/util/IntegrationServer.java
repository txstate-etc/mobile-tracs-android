package edu.txstate.mobileapp.mobileandroid.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonStreamParser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import edu.txstate.mobileapp.mobileandroid.notifications.DispatchNotification;
import edu.txstate.mobileapp.mobileandroid.notifications.NotificationsBundle;
import edu.txstate.mobileapp.mobileandroid.notifications.listeners.NotificationListener;
import edu.txstate.mobileapp.mobileandroid.notifications.tracs.TracsAnnouncement;

/**
 * Singleton Integration Server
 */
public class IntegrationServer {
    private static String integrationServerUrl;
    private HttpURLConnection client;
    private static IntegrationServer integrationServer;
    private static final String TAG = "IntegrationServer";
    private NotificationsBundle dispatchNotifications;

    private IntegrationServer() {
        dispatchNotifications = new NotificationsBundle();
        integrationServerUrl = "http://ajt79.its.txstate.edu:3000/";
    }

    public static IntegrationServer getInstance() {
        if (integrationServer == null) {
            integrationServer = new IntegrationServer();
        }
        return integrationServer;
    }

    public void getDispatchNotifications(NotificationListener listener, String userId){
        new DispatchNotifications(listener).execute(integrationServerUrl, userId);
    }

    private class DispatchNotifications extends AsyncTask<String, Void, String> {
        private final String TAG = "DispatchNotif";
        private NotificationListener notificationListener;

        DispatchNotifications(NotificationListener listener) {
            this.notificationListener = listener;
        }

        @Override
        protected String doInBackground(String... params) {
            String dataReceived = "";
            try {
                String userId = params[1];
                URL urlParam = new URL(params[0]);
                client = (HttpURLConnection) urlParam
                        .openConnection();
                dataReceived += JsonResponse.parse(client.getInputStream());
            } catch (IOException e) {
                Log.d(TAG, e.getMessage());
            }
            return dataReceived;
        }

        @Override
        protected void onPostExecute(String notificationData) {
            JsonElement data = new JsonArray();
            JsonArray notifications = new JsonArray();
            JsonStreamParser parser = new JsonStreamParser(notificationData);
            while (parser.hasNext()) {
                data = parser.next();
            }
            if (data.isJsonArray()) {
                notifications = (JsonArray) data;
            }

            if (data.isJsonObject()) {
                notifications.add(data);
            }

            for (JsonElement notification : notifications) {
                if (notification.isJsonObject()) {
                    dispatchNotifications.addOne(new DispatchNotification((JsonObject) notification));
                }
            }
            notificationListener.onNotificationAvailable(dispatchNotifications);
        }
    }
}
