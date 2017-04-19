package edu.txstate.mobileapp.mobileandroid.requests;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonStreamParser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import edu.txstate.mobileapp.mobileandroid.listeners.RegistrationListener;
import edu.txstate.mobileapp.mobileandroid.util.JsonResponse;

class RegisterDeviceRequest extends AsyncTask<String, Void, String> {
    private static final String TAG = "RegisterDeviceRequest";
    private RegistrationListener listener;

    private RegisterDeviceRequest() {}

    RegisterDeviceRequest (RegistrationListener listener) {
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

        }
        listener.onRequestReturned(deviceIsRegistered);
    }
}
