package edu.txstate.mobileapp.tracscompanion.util;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.HashMap;
import java.util.Map;

import edu.txstate.mobileapp.tracscompanion.listeners.DispatchListener;
import edu.txstate.mobileapp.tracscompanion.listeners.CheckRegistrationListener;
import edu.txstate.mobileapp.tracscompanion.notifications.DispatchNotification;
import edu.txstate.mobileapp.tracscompanion.notifications.NotificationsBundle;
import edu.txstate.mobileapp.tracscompanion.requests.AsyncTaskFactory;
import edu.txstate.mobileapp.tracscompanion.requests.Task;
import edu.txstate.mobileapp.tracscompanion.util.http.HttpQueue;
import edu.txstate.mobileapp.tracscompanion.util.http.requests.DispatchNotificationRequest;

/**
 * Singleton Integration Server
 */
public class IntegrationServer {
    private static String integrationServerUrl;
    private static IntegrationServer integrationServer;
    private static final String TAG = "IntegrationServer";

    private IntegrationServer() {
        integrationServerUrl = "http://ajt79.its.txstate.edu:3000/";
    }

    public static IntegrationServer getInstance() {
        if (integrationServer == null) {
            integrationServer = new IntegrationServer();
        }
        return integrationServer;
    }

    public void getRegistrationStatus(CheckRegistrationListener listener, String userId) {
        AsyncTask<String, Void, String> registrationTask = AsyncTaskFactory.createTask(Task.CHECK_REGISTRATION, listener);
        if (registrationTask != null) {
            registrationTask.execute(integrationServerUrl, userId);
        }
    }

    public void getDispatchNotifications(Response.Listener<NotificationsBundle> listener, Context context) {
        HttpQueue requestQueue = HttpQueue.getInstance(context.getApplicationContext());
        String url = integrationServerUrl + "?user=" + AppStorage.get(AppStorage.USERNAME, context);
        Map<String, String> headers = new HashMap<>();
        Response.ErrorListener errorHandler = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.wtf(TAG, error.getMessage());
            }
        };
        requestQueue.addToRequestQueue(new DispatchNotificationRequest(
                url, headers,
                listener, errorHandler));
    }
}
