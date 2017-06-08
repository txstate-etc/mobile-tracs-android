package edu.txstate.mobile.tracs;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.webkit.WebView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;

import edu.txstate.mobile.tracs.util.SettingsStore;

public class AnalyticsApplication extends Application {
    private Tracker tracker;

    //This is only applicable to storing a specific activity in a static
    //context field: getApplicationContext() should eliminate memory leaks.
    @SuppressLint("StaticFieldLeak")
    private static Context context;

    public void onCreate() {
        super.onCreate();
        Iconify.with(new FontAwesomeModule());
        context = this.getApplicationContext();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
            TracsWebView.setWebContentsDebuggingEnabled(true);
        }
        SettingsStore.getInstance().saveSettings();
    }

    public static Context getContext() {
        return context;
    }

    synchronized public Tracker getDefaultTracker() {
        if (tracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            tracker = analytics.newTracker("UA-24962120-4");
        }
        return tracker;
    }
}
