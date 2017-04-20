package edu.txstate.mobileapp.tracscompanion;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

public class AnalyticsApplication extends Application {
    private Tracker tracker;

    synchronized public Tracker getDefaultTracker() {
        if (tracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            tracker = analytics.newTracker("UA-24962120-4");
        }

        return tracker;
    }
}
