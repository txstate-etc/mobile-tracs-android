package edu.txstate.mobileapp.tracscompanion.requests;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import edu.txstate.mobileapp.tracscompanion.listeners.UserIdListener;
import edu.txstate.mobileapp.tracscompanion.util.JsonResponse;

public class TracsLoginRequest extends AsyncTask<String, Void, String> {
    private static final String TAG = "TracsUserIdRequest";
    private UserIdListener listener;
    private String userId = "";

    TracsLoginRequest (UserIdListener listener) {
        this.listener = listener;
    }

    /**
     *
     * @param params Pass url, userId, password as strings
     * @return String of the response data
     */
    @Override
    protected String doInBackground(String... params) {
        String dataReceived = "";
        try {
            URL urlParam = new URL(params[0] +
                    "?_username=" + params[1] +
                    "&_password=" + params[2]);

            HttpURLConnection client = (HttpURLConnection) urlParam
                    .openConnection();

            String cookie = client.getHeaderField("Cookie");

//            dataReceived += JsonResponse.parse(client.getInputStream());
            dataReceived = cookie;
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        }

        return dataReceived;
    }

    @Override
    protected void onPostExecute(String result) {
        listener.onRequestReturned(userId);
    }
}
