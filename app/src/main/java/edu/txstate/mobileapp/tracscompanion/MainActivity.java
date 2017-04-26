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

import edu.txstate.mobileapp.tracscompanion.util.AppStorage;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String SCREEN_NAME = "TRACS";
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

        AppStorage.put(AppStorage.NOTIFICATION_ID, FirebaseInstanceId.getInstance().getToken(), this);
        Log.i(TAG, "Token: " + AppStorage.get(AppStorage.NOTIFICATION_ID, this));

        //Analytics tracker setup for this view.
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        analyticsTracker = application.getDefaultTracker();

        //This is how you check for security setting
        KeyguardManager keyguardManager = (KeyguardManager) this.getSystemService(Context.KEYGUARD_SERVICE);
        Log.d(TAG, keyguardManager.isKeyguardSecure() ? "Device is secure" : "Device is not secure");

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tracs_toolbar);
        setSupportActionBar(toolbar);

        final TracsController tracsWebView = new TracsController((WebView) findViewById(R.id.tracs_webview), "https://tracs.txstate.edu");

        tracsWebView.javaScriptEnabled(true);
        tracsWebView.zoomEnabled(true);
        tracsWebView.loadUrl();

        tracsWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                tracsWebView.downloadFile(url, mimetype);
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuId = item.getItemId();
        switch (menuId) {
            case R.id.notificationsMenu:
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

    private boolean writePermissionNotGranted() {
        boolean doesNotHaveWritePermission;
        doesNotHaveWritePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
        return doesNotHaveWritePermission;
    }
}
