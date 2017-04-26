package edu.txstate.mobileapp.tracscompanion;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

public class AnalyticsApplication extends Application {
    private Tracker tracker;

    //This is only applicable to storing a specific activity in a static
    //context field: getApplicationContext() should eliminate memory leaks.
    @SuppressLint("StaticFieldLeak")
    private static Context context;

    public void onCreate() {
        super.onCreate();
        AnalyticsApplication.context = getApplicationContext();
    }

    synchronized public Tracker getDefaultTracker() {
        if (tracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            tracker = analytics.newTracker("UA-24962120-4");
        }

        return tracker;
    }

    public static Context getAppContext() {
        return AnalyticsApplication.context;
    }
}
