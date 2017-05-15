package edu.txstate.mobile.tracs;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import java.util.Observable;
import java.util.Observer;

import edu.txstate.mobile.tracs.util.LoginStatus;
import edu.txstate.mobile.tracs.util.MenuController;

public class MainActivity extends AppCompatActivity implements Observer {
    private static final String TAG = "MainActivity";
    private static final String SCREEN_NAME = "TRACS";
    private static final String TRACS_PORTAL_URL = AnalyticsApplication.getContext().getString(R.string.tracs_base)
                                                    + AnalyticsApplication.getContext().getString(R.string.tracs_portal);
    private static Menu optionsMenu;
    private int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE;
    private Tracker analyticsTracker;
    private String urlToLoad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (writePermissionNotGranted()) {
                requestWritePermission();
            }
        }
        super.onCreate(savedInstanceState);

        //Analytics tracker setup for this view.
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        analyticsTracker = application.getDefaultTracker();
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent callingIntent = getIntent();
        String urlToLoad = callingIntent.getStringExtra("url");

        if (urlToLoad == null) {
            urlToLoad = TRACS_PORTAL_URL;
        }

        String shouldLoadNotificationsView = callingIntent.getStringExtra("shouldLoadNotificationsView");
        if ("true".equals(shouldLoadNotificationsView)) {
            Intent intent = new Intent(this, NotificationsActivity.class);
            startActivity(intent);
            finish();
        }

        LoginStatus.getInstance().addObserver(this);
        LoginStatus.getInstance().logout();

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final TracsController tracsWebView = new TracsController((WebView) findViewById(R.id.tracs_webview));

        Log.i(TAG, "MainActivity resumed");
        analyticsTracker.setScreenName(SCREEN_NAME);
        analyticsTracker.send(new HitBuilders.ScreenViewBuilder().build());

        tracsWebView.loadUrl(urlToLoad);
        tracsWebView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> tracsWebView.downloadFile(url, mimetype));
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
        LoginStatus.getInstance().deleteObserver(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.findItem(R.id.menu_notifications).setIcon(
                new IconDrawable(this, FontAwesomeIcons.fa_bell_o)
                        .colorRes(R.color.colorAccent)
                        .actionBarSize()
        ).setEnabled(LoginStatus.getInstance().isUserLoggedIn());
        optionsMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuId = item.getItemId();
        return MenuController.handleMenuClick(menuId, this) || super.onOptionsItemSelected(item);
    }

    @TargetApi(value = Build.VERSION_CODES.M)
    private void requestWritePermission() {
        this.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
    }

    public static void setNotificationsEnabled(boolean shouldEnableNotifications) {
        if (optionsMenu == null) {
            return;
        }
        optionsMenu.findItem(R.id.menu_notifications).setEnabled(shouldEnableNotifications);
    }

    private boolean writePermissionNotGranted() {
        boolean doesNotHaveWritePermission;
        doesNotHaveWritePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
        return doesNotHaveWritePermission;
    }

    @Override
    public void update(Observable loginStatus, Object userIsLoggedIn) {
        setNotificationsEnabled((boolean) userIsLoggedIn);
        Log.i(TAG, "Observer notified: user " + ((boolean) userIsLoggedIn ? "is " : "is not ") + "logged in.");
    }
}
