package edu.txstate.mobile.tracs.util;

import android.content.Intent;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
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
        TracsClient.getInstance().verifySession(IntegrationServer.this::onResponse);
    }

    public void onResponse(String sessionId) {
        if (sessionId == null) {
            loadFailedLoginIntent(new VolleyError("Could not log user in!"));
        }
        HttpQueue requestQueue = HttpQueue.getInstance(AnalyticsApplication.getContext());

        String url = integrationServerUrl +
                     AnalyticsApplication.getContext().getString(R.string.dispatch_notifications) +
                     "?token=" +
                     FirebaseInstanceId.getInstance().getToken();

        Map<String, String> headers = new HashMap<>();
        requestQueue.addToRequestQueue(new DispatchNotificationRequest(
                url, headers, this.listener, this::loadFailedLoginIntent), this);
    }

    private void loadFailedLoginIntent(VolleyError error) {
        Log.e(TAG, "Could not log user in!");
        LoginStatus.getInstance().logout();
        String url = AnalyticsApplication.getContext().getString(R.string.tracs_cas_login);
        Intent intent = new Intent(AnalyticsApplication.getContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("url", url);
        AnalyticsApplication.getContext().startActivity(intent);
    }
}
