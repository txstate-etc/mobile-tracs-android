package edu.txstate.mobileapp.mobileandroid.requests;

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
import edu.txstate.mobileapp.mobileandroid.notifications.listeners.DispatchListener;
import edu.txstate.mobileapp.mobileandroid.util.JsonResponse;

class DispatchNotificationRequest extends AsyncTask<String, Void, String> {
    private final String TAG = "DispatchNotif";
    private DispatchListener notificationListener;

    DispatchNotificationRequest (DispatchListener listener) {
        this.notificationListener = listener;
    }

    @Override
    protected String doInBackground(String... params) {
        String dataReceived = "";
        try {
            String userId = params[1];
            URL urlParam = new URL(params[0]);
            HttpURLConnection client = (HttpURLConnection) urlParam
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
        NotificationsBundle dispatchNotifications = new NotificationsBundle();
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
        notificationListener.onRequestReturned(dispatchNotifications);
    }
}
