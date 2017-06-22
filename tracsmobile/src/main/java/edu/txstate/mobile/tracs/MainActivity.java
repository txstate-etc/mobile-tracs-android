package edu.txstate.mobile.tracs;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import edu.txstate.mobile.tracs.util.LoginStatus;
import edu.txstate.mobile.tracs.util.VersionTracker;

public class MainActivity extends BaseTracsActivity {
    private static final String TAG = "MainActivity";
    private static final String SCREEN_NAME = "WebView";
    private static final String TRACS_PORTAL_URL = AnalyticsApplication.getContext().getString(R.string.tracs_base)
                                                    + AnalyticsApplication.getContext().getString(R.string.tracs_portal);
    private TracsWebView tracsWebView;
    private final int FILE_CHOOSER_RESULT_CODE = 1;
    private final int REQUEST_CODE_LOLLIPOP = 2;

    private ValueCallback<Uri> uploadMessage;
    private ValueCallback<Uri[]> fileMessage;
    private String cameraPhotoPath;
    private boolean shouldReloadWebView = true;

    @SuppressWarnings("unused")
    private int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (uploadMessage != null) {
                Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
                uploadMessage.onReceiveValue(result);
                uploadMessage = null;
            }
        } else if (requestCode == REQUEST_CODE_LOLLIPOP) {
            Uri[] results = null;
            if (resultCode == Activity.RESULT_OK) {
                if (data == null || data.getData() == null) {
                    if (cameraPhotoPath != null) {
                        results = new Uri[]{Uri.parse(cameraPhotoPath)};
                    }
                } else {
                    String dataString = data.getDataString();
                    if (dataString != null) {
                        results = new Uri[]{Uri.parse(dataString)};
                    }
                }
            }
            fileMessage.onReceiveValue(results);
            fileMessage = null;
        }
        shouldReloadWebView = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (writePermissionNotGranted()) {
                requestWritePermission();
            }
        }
        if (launchedFromNotification()) {
            goToNotifications();
        } else if (VersionTracker.getInstance().isNewVersion()) {
            goToAboutApp();
        }
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);
        LoginStatus.getInstance().addObserver(this);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        shouldReloadWebView = getIntent().getStringExtra("url") != null;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (shouldReloadWebView) {
            String destinationUrl = getDestinationUrl();
            tracsWebView = (TracsWebView) findViewById(R.id.tracs_webview);
            tracsWebView.setWebChromeClient(new TracsWebChromeClient(this));
            tracsWebView.loadUrl(destinationUrl, true);
            getIntent().removeExtra("url");
            shouldReloadWebView = false;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String url = intent.getStringExtra("url");
        if (url != null && !"".equals(url)) {
            getIntent().putExtra("url", url);
        }
        if (launchedFromNotification()) {
            goToNotifications();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        super.hitScreenView(SCREEN_NAME);
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

    public void setUploadMessage(ValueCallback<Uri> uploadMessage) {
        this.uploadMessage = uploadMessage;
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

    private void goToAboutApp() {
        Intent intent = new Intent(this, AboutActivity.class);
        intent.putExtra("firstLaunch", true);
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

    private class TracsWebChromeClient extends WebChromeClient {
        private Context context;

        TracsWebChromeClient (Context context) {
            super();
            this.context = context;
        }

        public void openFileChoose(ValueCallback<Uri> uploadMsg) {
            uploadMessage = uploadMsg;
            Intent fileChooser = new Intent(Intent.ACTION_GET_CONTENT);
            fileChooser.addCategory(Intent.CATEGORY_OPENABLE);
            fileChooser.setType("image/*");
            startActivityForResult(Intent.createChooser(fileChooser, "File Chooser"), FILE_CHOOSER_RESULT_CODE);
        }

        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
            uploadMessage = uploadMsg;
            Intent fileChooser = new Intent(Intent.ACTION_GET_CONTENT);
            fileChooser.addCategory(Intent.CATEGORY_OPENABLE);
            fileChooser.setType("*/*");
            startActivityForResult(Intent.createChooser(fileChooser, "File Chooser"), FILE_CHOOSER_RESULT_CODE);
        }

        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
            uploadMessage = uploadMsg;
            Intent fileChooser = new Intent(Intent.ACTION_GET_CONTENT);
            fileChooser.addCategory(Intent.CATEGORY_OPENABLE);
            fileChooser.setType("image/*");
            startActivityForResult(Intent.createChooser(fileChooser, "File Chooser"), FILE_CHOOSER_RESULT_CODE);
        }

        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            if (fileMessage != null) {
                fileMessage.onReceiveValue(null);
            }
            fileMessage = filePathCallback;

            Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePicture.resolveActivity(Activity.class.cast(context).getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException e) {
                    Log.e(TAG, "Unable to create image file");
                }

                if (photoFile != null) {
                    cameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                    takePicture.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                } else {
                    takePicture = null;
                }
            }

            Intent fileChooser = new Intent(Intent.ACTION_GET_CONTENT);
            fileChooser.addCategory(Intent.CATEGORY_OPENABLE);
            fileChooser.setType("*/*");

            Intent[] intentArray;
            if (takePicture != null) {
                intentArray = new Intent[]{takePicture};
            } else {
                intentArray = new Intent[0];
            }

            Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
            chooserIntent.putExtra(Intent.EXTRA_INTENT, fileChooser);
            chooserIntent.putExtra(Intent.EXTRA_TITLE, "Upload File");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

            startActivityForResult(chooserIntent, REQUEST_CODE_LOLLIPOP);
            return true;
        }

        private File createImageFile() throws IOException {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES);
            return File.createTempFile(
                    imageFileName,
                    ".jpg",
                    storageDir
            );
        }
    }
}
