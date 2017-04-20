package edu.txstate.mobileapp.tracscompanion.requests;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonStreamParser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import edu.txstate.mobileapp.tracscompanion.listeners.TracsListener;
import edu.txstate.mobileapp.tracscompanion.notifications.tracs.TracsAnnouncement;
import edu.txstate.mobileapp.tracscompanion.notifications.tracs.TracsNotification;
import edu.txstate.mobileapp.tracscompanion.util.JsonResponse;

class TracsNotificationRequest extends AsyncTask<String, Void, String> {
    private final String TAG = "GetTracsNotification";
    private final String FORBIDDEN_ERROR = "Access to resource is forbidden";
    private TracsListener listener;

    TracsNotificationRequest(TracsListener listener) {
        this.listener = listener;
    }

    @Override
    protected String doInBackground(String... params) {
        String entityId = params[1];
        String response = "";
        try {
            URL url = new URL(params[0] + entityId + ".json");
            HttpURLConnection client = (HttpURLConnection) url.openConnection();
            client.setRequestProperty("Cookie", "JSESSIONID=aa52c1a3-9d3b-4328-a98a-db7803d7bc98.tracs-app-mcs-1-7;");
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
        listener.onRequestReturned(announcement);
    }
}
