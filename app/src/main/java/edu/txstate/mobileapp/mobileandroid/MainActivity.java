package edu.txstate.mobileapp.mobileandroid;

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

import java.util.UUID;

import edu.txstate.mobileapp.mobileandroid.util.AppInstanceId;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";
    private int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (writePermissionNotGranted()) {
                requestWritePermission();
            }
        }
        super.onCreate(savedInstanceState);

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
        Log.i("INFO", "MainActivity resumed");
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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[],
                                           int[] grantResults) {
        //TODO: Implement something that happens when a permission is granted, if necessary
    }
}
