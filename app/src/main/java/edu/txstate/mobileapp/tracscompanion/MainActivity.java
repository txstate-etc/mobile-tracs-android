package edu.txstate.mobileapp.tracscompanion;

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
import android.webkit.DownloadListener;
import android.webkit.WebView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.iid.FirebaseInstanceId;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import java.util.Observable;
import java.util.Observer;

import edu.txstate.mobileapp.tracscompanion.util.AppStorage;
import edu.txstate.mobileapp.tracscompanion.util.LoginStatus;

public class MainActivity extends AppCompatActivity implements Observer {
    private static final String TAG = "MainActivity";
    private static final String SCREEN_NAME = "TRACS";
    private static Menu optionsMenu;
    private int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE;
    private Tracker analyticsTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (writePermissionNotGranted()) {
                requestWritePermission();
            }
        }
        super.onCreate(savedInstanceState);
        LoginStatus.getInstance().addObserver(this);
        LoginStatus.getInstance().logout();

        AppStorage.put(AppStorage.NOTIFICATION_ID, FirebaseInstanceId.getInstance().getToken(), this);

        //Analytics tracker setup for this view.
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        analyticsTracker = application.getDefaultTracker();

        //This is how you check for security setting
        KeyguardManager keyguardManager = (KeyguardManager) this.getSystemService(Context.KEYGUARD_SERVICE);
        Log.d(TAG, keyguardManager.isKeyguardSecure() ? "Device is secure" : "Device is not secure");

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tracs_toolbar);
        setSupportActionBar(toolbar);

        final TracsController tracsWebView = new TracsController((WebView) findViewById(R.id.tracs_webview));

        tracsWebView.loadUrl();
        tracsWebView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> tracsWebView.downloadFile(url, mimetype));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.findItem(R.id.notifications_menu).setIcon(
                new IconDrawable(this, FontAwesomeIcons.fa_bell_o)
                        .colorRes(R.color.colorAccent)
                        .actionBarSize()
        )
                .setEnabled(false);

        menu.findItem(R.id.menu_refresh).setVisible(LoginStatus.getInstance().isUserLoggedIn());
        optionsMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuId = item.getItemId();
        switch (menuId) {
            case R.id.notifications_menu:
                Intent intent = new Intent(this, NotificationsActivity.class);
                startActivity(intent);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onResume() {
        Log.i(TAG, "MainActivity resumed");
        analyticsTracker.setScreenName(SCREEN_NAME);
        analyticsTracker.send(new HitBuilders.ScreenViewBuilder().build());
        super.onResume();
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
        optionsMenu.findItem(R.id.notifications_menu).setEnabled(shouldEnableNotifications);
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
