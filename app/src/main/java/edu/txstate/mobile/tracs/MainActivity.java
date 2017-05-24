package edu.txstate.mobile.tracs;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

import edu.txstate.mobile.tracs.util.LoginStatus;

public class MainActivity extends BaseTracsActivity {
    private static final String TAG = "MainActivity";
    private static final String SCREEN_NAME = "TRACS";
    private static final String TRACS_PORTAL_URL = AnalyticsApplication.getContext().getString(R.string.tracs_base)
                                                    + AnalyticsApplication.getContext().getString(R.string.tracs_portal);
    private TracsWebView tracsWebView;

    @SuppressWarnings("unused")
    private int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (writePermissionNotGranted()) {
                requestWritePermission();
            }
        }
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);
        LoginStatus.getInstance().addObserver(this);

        String destinationUrl = getDestinationUrl();
        TracsWebView tracsWebView = (TracsWebView) findViewById(R.id.tracs_webview);
        tracsWebView.loadUrl(destinationUrl, true);
    }

    @Override
    public void onResume() {
        super.onResume();
        super.hitScreenView(SCREEN_NAME);

        if (launchedFromNotification()) {
            goToNotifications();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        super.setupOptionsMenu(menu);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        LoginStatus.getInstance().deleteObserver(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @TargetApi(value = Build.VERSION_CODES.M)
    private void requestWritePermission() {
        this.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
    }

    private boolean launchedFromNotification() {
        Intent callingIntent = getIntent();
        String shouldLoadNotificationsView = callingIntent.getStringExtra("shouldLoadNotificationsView");
        return "true".equals(shouldLoadNotificationsView);
    }

    private void goToNotifications() {
        Intent intent = new Intent(this, NotificationsActivity.class);
        startActivity(intent);
        finish();
    }

    private String getDestinationUrl() {
        String urlToLoad = getIntent().getStringExtra("url");
        if (urlToLoad == null) {
            urlToLoad = TRACS_PORTAL_URL;
        }
        return urlToLoad;
    }

    private boolean writePermissionNotGranted() {
        boolean doesNotHaveWritePermission;
        doesNotHaveWritePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
        return doesNotHaveWritePermission;
    }
}
