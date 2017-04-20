package edu.txstate.mobileapp.tracscompanion.requests;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonStreamParser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import edu.txstate.mobileapp.tracscompanion.listeners.CheckRegistrationListener;
import edu.txstate.mobileapp.tracscompanion.util.JsonResponse;

class RegisterStatusRequest extends AsyncTask<String, Void, String> {
    private static final String TAG = "RegisterStatusRequest";
    private CheckRegistrationListener listener;

    private RegisterStatusRequest() {}

    RegisterStatusRequest(CheckRegistrationListener listener) {
        this.listener = listener;
    }

    @Override
    protected String doInBackground(String... params) {
        String dataReceived = "";
        try {
            String userId = params[1];
            String url = params[0] + "?userId=" + userId;
            URL urlParam = new URL(url);
            HttpURLConnection client = (HttpURLConnection) urlParam
                    .openConnection();
            dataReceived += JsonResponse.parse(client.getInputStream());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        return dataReceived;
    }

    @Override
    protected void onPostExecute(String dataReceived) {
        JsonElement data = new JsonArray();
        JsonArray registrations = new JsonArray();
        JsonStreamParser parser = new JsonStreamParser(dataReceived);
        boolean deviceIsRegistered = false;
        if (parser.hasNext()) {
            data = parser.next();
        }

        if (data.isJsonArray()) {
            registrations = (JsonArray) data;
        }

        if (data.isJsonObject()) {
            registrations.add(data);
        }

        for (JsonElement registeredDevice : registrations) {
            Log.i(TAG, registeredDevice.toString());
        }
        listener.onRequestReturned(deviceIsRegistered);
    }
}
