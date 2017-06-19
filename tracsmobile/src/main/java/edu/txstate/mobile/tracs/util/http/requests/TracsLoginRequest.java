package edu.txstate.mobile.tracs.util.http.requests;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import edu.txstate.mobile.tracs.AnalyticsApplication;
import edu.txstate.mobile.tracs.R;
import edu.txstate.mobile.tracs.util.AppStorage;
import edu.txstate.mobile.tracs.util.http.listeners.LoginListener;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class TracsLoginRequest {

    private static final String TAG = "TracsLoginRequest";

    private static LoginListener loginListener;
    private static OkHttpClient client;
    private static Request loginRequest, loginVerify;

    private static final String LOGIN_URL = AnalyticsApplication.getContext().getResources().getString(R.string.tracs_base)
            + AnalyticsApplication.getContext().getResources().getString(R.string.tracs_session_login);
    private static final String SESSION_VERIFY_URL = AnalyticsApplication.getContext().getResources().getString(R.string.tracs_base)
            + AnalyticsApplication.getContext().getResources().getString(R.string.tracs_session);

    private TracsLoginRequest() {}

    public static void execute(LoginListener listener) {
        loginListener = listener;
        client = new OkHttpClient().newBuilder()
                .followSslRedirects(false)
                .followRedirects(false)
                .build();
        new TracsLoginTask().execute();
    }


    private static class TracsLoginTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            okhttp3.Response response = null;
            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            String formBody = "eid=" + AppStorage.get(AppStorage.USERNAME, AnalyticsApplication.getContext())
                    + "&pw=" + AppStorage.get(AppStorage.PASSWORD, AnalyticsApplication.getContext());
            RequestBody body = RequestBody.create(mediaType, formBody);
            loginRequest = new Request.Builder()
                    .url(LOGIN_URL)
                    .post(body)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .build();
            try {
                response = client.newCall(loginRequest).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            boolean sessionIsPresent = response != null
                                     && response.body() != null
                                     && response.header("Set-Cookie") != null;
            if (sessionIsPresent) {
                String[] cookies = response.header("Set-Cookie").split(";");
                for (String cookie : cookies) {
                    String name = null;
                    String value = null;
                    if (cookie != null && cookie.length() >= 2) {
                        name = cookie.split("=")[0];
                        value = cookie.split("=")[1];
                    }
                    if ("JSESSIONID".equals(name)) {
                        return value;
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String session) {
            new TracsLoginVerification().execute(session);
        }
    }

    private static class TracsLoginVerification extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            okhttp3.Response response = null;
            String jSessionId = strings[0];
            loginVerify = new Request.Builder()
                    .url(SESSION_VERIFY_URL)
                    .get()
                    .addHeader("Cookie", "JSESSIONID=" + jSessionId + ";")
                    .build();
            try {
                response = client.newCall(loginVerify).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
                if (response != null && response.body() != null) {
                if (response.code() != 200) {
                    return null;
                }
                JSONObject body = null;
                try {
                    body = new JSONObject(response.body().string());
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }

                try {
                    if (body != null && body.getString("userEid").equals(AppStorage.get(AppStorage.USERNAME, AnalyticsApplication.getContext()))) {
                        return jSessionId;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String sessionId) {
            TracsLoginRequest.loginListener.onResponse(sessionId);
        }
    }

}
