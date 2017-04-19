package edu.txstate.mobileapp.mobileandroid.requests;

import android.os.AsyncTask;

import edu.txstate.mobileapp.mobileandroid.listeners.DispatchListener;
import edu.txstate.mobileapp.mobileandroid.listeners.RequestListener;
import edu.txstate.mobileapp.mobileandroid.listeners.TracsListener;



public final class AsyncTaskFactory{

    private AsyncTaskFactory() {}

    public static AsyncTask<String, Void, String> createTask(String taskType, RequestListener listener){
        AsyncTask<String, Void, String> task;
        switch (taskType) {
            case Task.DISPATCH_NOTIFICATIONS:
                task = new DispatchNotificationRequest((DispatchListener) listener);
                break;
            case Task.REGISTER_DEVICE:
                task = null;
                break;
            case Task.TRACS_NOTIFICATION:
                task = new TracsNotificationRequest((TracsListener) listener);
                break;
            default:
                task = null;
                break;
        }

        return task;
    }
}
