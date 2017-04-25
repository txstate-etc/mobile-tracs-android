package edu.txstate.mobileapp.tracscompanion.requests;

import android.os.AsyncTask;

import edu.txstate.mobileapp.tracscompanion.listeners.CheckRegistrationListener;
import edu.txstate.mobileapp.tracscompanion.listeners.DispatchListener;
import edu.txstate.mobileapp.tracscompanion.listeners.RequestListener;
import edu.txstate.mobileapp.tracscompanion.listeners.TracsListener;
import edu.txstate.mobileapp.tracscompanion.listeners.UserIdListener;

/**
 * Creates a new request of the given type.
 */
public final class AsyncTaskFactory{

    private AsyncTaskFactory() {}

    public static AsyncTask<String, Void, String> createTask(String taskType, RequestListener listener){
        AsyncTask<String, Void, String> task;
        switch (taskType) {
            case Task.DISPATCH_NOTIFICATIONS:
                task = new DispatchNotificationRequest((DispatchListener) listener);
                break;
            case Task.CHECK_REGISTRATION:
                task = new RegisterStatusRequest((CheckRegistrationListener) listener);
                break;
            case Task.TRACS_NOTIFICATION:
                task = new TracsNotificationRequest((TracsListener) listener);
                break;
            case Task.TRACS_USER_ID:
                task = new TracsUserIdRequest((UserIdListener) listener);
                break;
            case Task.TRACS_LOGIN:
                task = new TracsLoginRequest((UserIdListener) listener);
                break;
            default:
                task = null;
                break;
        }

        return task;
    }
}
