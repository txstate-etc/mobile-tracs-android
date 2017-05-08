package edu.txstate.mobile.tracs.util;

import android.content.Intent;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.HashMap;
import java.util.Map;

import edu.txstate.mobile.tracs.AnalyticsApplication;
import edu.txstate.mobile.tracs.MainActivity;
import edu.txstate.mobile.tracs.R;
import edu.txstate.mobile.tracs.notifications.NotificationsBundle;
import edu.txstate.mobile.tracs.util.http.HttpQueue;
import edu.txstate.mobile.tracs.util.http.requests.DispatchNotificationRequest;
import edu.txstate.mobile.tracs.util.http.requests.TracsLoginRequest;

/**
 * Singleton Integration Server
 */

public class IntegrationServer {
    private static String integrationServerUrl;
    private static IntegrationServer integrationServer;
    private static final String TAG = "IntegrationServer";
    private Response.Listener<NotificationsBundle> listener;
    private IntegrationServer() {
        integrationServerUrl = "http://ajt79.its.txstate.edu:3000/";
    }

    public static IntegrationServer getInstance() {
        if (integrationServer == null) {
            integrationServer = new IntegrationServer();
        }
        return integrationServer;
    }

    public void getRegistrationStatus() {

    }

    public void getDispatchNotifications(Response.Listener<NotificationsBundle> listener) {
        HttpQueue requestQueue = HttpQueue.getInstance(AnalyticsApplication.getContext());
        this.listener = listener;
        if (credentialsAreStored()) {
            requestQueue.addToRequestQueue(new TracsLoginRequest(
                    TracsClient.LOGIN_URL, IntegrationServer.getInstance()::onResponse, IntegrationServer.getInstance()::onLoginError
            ), TAG);
        } else {
            loadFailedLoginIntent();
        }
    }

    private boolean credentialsAreStored() {
        String username = AppStorage.get(AppStorage.USERNAME, AnalyticsApplication.getContext());
        String password = AppStorage.get(AppStorage.PASSWORD, AnalyticsApplication.getContext());

        return !("".equals(username) || "".equals(password)) ;
    }

    public void onResponse(String sessionId) {
        if ("".equals(sessionId)) {
            loadFailedLoginIntent();
        }
        HttpQueue requestQueue = HttpQueue.getInstance(AnalyticsApplication.getContext());
        String url = integrationServerUrl + "?user=" + AppStorage.get(AppStorage.USERNAME, AnalyticsApplication.getContext());
        Map<String, String> headers = new HashMap<>();
        Response.ErrorListener errorHandler = error -> Log.wtf(TAG, error.getMessage());
        requestQueue.addToRequestQueue(new DispatchNotificationRequest(
                url, headers, this.listener, errorHandler), TAG);
    }

    private void onLoginError(VolleyError error) {
        Log.wtf(TAG, "Could not login with stored credentials");
        loadFailedLoginIntent();
    }

    private void loadFailedLoginIntent() {
        LoginStatus.getInstance().logout();
        String url = AnalyticsApplication.getContext().getString(R.string.cas_login_tracs);
        Intent intent = new Intent(AnalyticsApplication.getContext(), MainActivity.class);
        intent.putExtra("url", url);
        AnalyticsApplication.getContext().startActivity(intent);
    }
}
