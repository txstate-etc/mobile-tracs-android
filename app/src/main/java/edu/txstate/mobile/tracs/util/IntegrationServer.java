package edu.txstate.mobile.tracs.util;

import android.content.Intent;
import android.util.Log;

import com.android.volley.Response;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;

import edu.txstate.mobile.tracs.AnalyticsApplication;
import edu.txstate.mobile.tracs.MainActivity;
import edu.txstate.mobile.tracs.R;
import edu.txstate.mobile.tracs.notifications.NotificationsBundle;
import edu.txstate.mobile.tracs.util.http.HttpQueue;
import edu.txstate.mobile.tracs.util.http.requests.DispatchNotificationRequest;

/**
 * Singleton Integration Server
 */

public class IntegrationServer {
    private static String integrationServerUrl;
    private static IntegrationServer integrationServer;
    private static final String TAG = "IntegrationServer";
    private Response.Listener<NotificationsBundle> listener;
    private IntegrationServer() {
        integrationServerUrl = AnalyticsApplication.getContext().getString(R.string.dispatch_base);
    }

    public static IntegrationServer getInstance() {
        if (integrationServer == null) {
            integrationServer = new IntegrationServer();
        }
        return integrationServer;
    }

    public void getDispatchNotifications(Response.Listener<NotificationsBundle> listener) {
        this.listener = listener;
        TracsClient.getInstance().verifySession(IntegrationServer.getInstance()::onResponse);
    }

    public void onResponse(String sessionId) {
        if (sessionId == null) {
            loadFailedLoginIntent();
        }
        HttpQueue requestQueue = HttpQueue.getInstance(AnalyticsApplication.getContext());

        String url = integrationServerUrl +
                     AnalyticsApplication.getContext().getString(R.string.dispatch_notifications) +
                     "?token=" +
                     FirebaseInstanceId.getInstance().getToken();

        Map<String, String> headers = new HashMap<>();
        Response.ErrorListener errorHandler = error -> Log.wtf(TAG, error.getMessage());
        requestQueue.addToRequestQueue(new DispatchNotificationRequest(
                url, headers, this.listener, errorHandler), this);
    }

    private void loadFailedLoginIntent() {
        LoginStatus.getInstance().logout();
        String url = AnalyticsApplication.getContext().getString(R.string.tracs_login);
        Intent intent = new Intent(AnalyticsApplication.getContext(), MainActivity.class);
        intent.putExtra("url", url);
        AnalyticsApplication.getContext().startActivity(intent);
    }
}
