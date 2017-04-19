package edu.txstate.mobileapp.mobileandroid.requests;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import edu.txstate.mobileapp.mobileandroid.notifications.listeners.TracsListener;
import edu.txstate.mobileapp.mobileandroid.util.JsonResponse;

class RegisterDeviceRequest extends AsyncTask<String, Void, String> {
    private static final String TAG = "RegisterDeviceRequest";

    RegisterDeviceRequest (TracsListener listener) {

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
    protected void onPostExecute(String data) {

    }
}
