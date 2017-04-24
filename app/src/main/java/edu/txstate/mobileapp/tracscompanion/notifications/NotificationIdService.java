package edu.txstate.mobileapp.tracscompanion.notifications;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class NotificationIdService extends FirebaseInstanceIdService {

    private static final String TAG = "NotificationIdService";

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        Log.d(TAG, refreshedToken);
    }
}
