package edu.txstate.mobileapp.mobileandroid.util;

import android.os.AsyncTask;

import edu.txstate.mobileapp.mobileandroid.notifications.listeners.DispatchListener;
import edu.txstate.mobileapp.mobileandroid.requests.AsyncTaskFactory;
import edu.txstate.mobileapp.mobileandroid.requests.Task;

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

    public void getDispatchNotifications(DispatchListener listener, String userId){
        AsyncTask<String, Void, String> dispatchTask = AsyncTaskFactory.createTask(Task.DISPATCH_NOTIFICATIONS, listener);
        if (dispatchTask != null) {
            dispatchTask.execute(integrationServerUrl, userId);
        }
    }
}
