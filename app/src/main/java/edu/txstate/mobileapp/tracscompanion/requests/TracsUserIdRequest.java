package edu.txstate.mobileapp.tracscompanion.requests;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonStreamParser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import edu.txstate.mobileapp.tracscompanion.listeners.UserIdListener;
import edu.txstate.mobileapp.tracscompanion.util.AppStorage;
import edu.txstate.mobileapp.tracscompanion.util.JsonResponse;

public class TracsUserIdRequest extends AsyncTask<String, Void, String> {
    private static final String TAG = "TracsUserIdRequest";
    private UserIdListener listener;
    private String userId = "";

    TracsUserIdRequest (UserIdListener listener) {
        this.listener = listener;
    }

    @Override
    protected String doInBackground(String... params) {
        String dataReceived = "";
        try {
            URL urlParam = new URL(params[0]);
            userId = params[1];
            String sessionId = "JSESSIONID=" + params[2];
            HttpURLConnection client = (HttpURLConnection) urlParam
                    .openConnection();
            client.setRequestProperty("Cookie", sessionId);

            dataReceived += JsonResponse.parse(client.getInputStream());
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        }

        return dataReceived;
    }

    @Override
    protected void onPostExecute(String result) {
        JsonObject sessionInfo;
        JsonStreamParser parser = new JsonStreamParser(result);
        String userEid = "";

        //userEid is tracs NetId
        //userId is tracs UUID for users
        while (parser.hasNext()) {
            sessionInfo = (JsonObject) parser.next();
            if (sessionInfo.has("session_collection")) {
                JsonArray sessions = (JsonArray) sessionInfo.get("session_collection");
                for (JsonElement session : sessions) {
                    if (session.isJsonObject()) {
                        JsonObject currentSession = (JsonObject) session;
                        String netId = currentSession.get("userEid").isJsonNull() ? "null" : currentSession.get("userEid").getAsString();
                        boolean idsMatch = userId.equals(netId);
                        if (idsMatch) {
                            userEid = JsonObject.class.cast(session).get("userId").getAsString();
                        }
                    }
                }
            }
        }

        listener.onRequestReturned(userEid);
    }
}
