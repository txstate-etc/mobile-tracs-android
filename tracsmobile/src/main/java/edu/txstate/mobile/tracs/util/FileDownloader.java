package edu.txstate.mobile.tracs.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import edu.txstate.mobile.tracs.AnalyticsApplication;
import edu.txstate.mobile.tracs.R;

public class FileDownloader {

    private static final String TAG = "FileDownloader";

    private Context context;
    private DownloadManager downloadManager;
    private BroadcastReceiver onDownloadCompletion;

    private static final String DOWNLOAD_MANAGER_PACKAGE_NAME = "com.android.providers.downloads";
    private long downloadId = -1;
    private LongSparseArray<Boolean> downloadStatus = new LongSparseArray<>();

    public FileDownloader(Context context) {
        this.context = context;

        if (downloadManagerIsDisabled()) {
            downloadManagerDisabledAlert();
        } else {
            this.downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        }
    }

    private boolean downloadManagerIsDisabled() {
        int state = this.context.getPackageManager().getApplicationEnabledSetting(DOWNLOAD_MANAGER_PACKAGE_NAME);
        return (
                state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED ||
                        state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER ||
                        state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED
        );
    }

    private void goToSettings() {
        try {
            Intent appSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            appSettings.setData(Uri.parse("package:" + DOWNLOAD_MANAGER_PACKAGE_NAME));
            context.startActivity(appSettings);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Intent genericSettings = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
            context.startActivity(genericSettings);
        }
    }

    public void downloadFile(String url, final String mimeType) {
        if (Build.VERSION.SDK_INT >= 23 && userHasDeniedStorage()) {
            //Display prompt for downloading permissions
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            View titleView = inflater.inflate(R.layout.download_dialog_title, null);
            builder.setCustomTitle(titleView)
                    .setPositiveButton("OK", (dialogInterface, i) -> {
                        goToDownloadSettings();
                    })
                    .setNegativeButton("CANCEL", ((dialogInterface, i) -> {
                        Toast toast = Toast.makeText(context, "Cancelling download...", Toast.LENGTH_LONG);
                        toast.show();
                    }));

            AlertDialog dialog = builder.create();
            dialog.setOnShowListener((dialogInterface -> {
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(AnalyticsApplication.getContext().getResources().getColor(R.color.unreadBullhornColor));
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(AnalyticsApplication.getContext().getResources().getColor(R.color.unreadBullhornColor));
            }));
            dialog.show();
            return;
        }
        String fileName = "";
        String cookies = android.webkit.CookieManager.getInstance().getCookie(url);
        boolean urlIsNotEmpty = !(url == null || url.isEmpty());
        if (urlIsNotEmpty) {
            fileName = this.extractFilenameFromUrl(url);
        }
        if (downloadManagerIsDisabled()) {
            downloadManagerDisabledAlert();
        } else {
            if (downloadManager == null) {
                this.downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            }
            String dirName = Environment.DIRECTORY_DOWNLOADS;

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setDescription("Downloading file from TRACS...");
            request.setTitle(fileName);
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(dirName, fileName);
            request.addRequestHeader("Cookie", cookies);
            request.setMimeType(mimeType);

            this.onDownloadCompletion = new TypedBroadcastReceiver(mimeType);
            this.context.getApplicationContext().registerReceiver(this.onDownloadCompletion, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

            DownloadManager.Query isDownloadingQuery = new DownloadManager.Query();

            isDownloadingQuery.setFilterByStatus(
                    DownloadManager.STATUS_PAUSED |
                            DownloadManager.STATUS_PENDING |
                            DownloadManager.STATUS_RUNNING |
                            DownloadManager.STATUS_SUCCESSFUL
            );

            this.downloadId = downloadManager.enqueue(request);
            this.downloadStatus.put(downloadId, false);
        }
    }

    private boolean userHasDeniedStorage() {
        return ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void goToDownloadSettings() {
        try {
            Intent appSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", context.getPackageName(), null);
            appSettings.setData(uri);
            context.startActivity(appSettings);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Intent genericSettings = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
            context.startActivity(genericSettings);
        }
    }

    private String extractFilenameFromUrl(String url) {
        int indexOfLastForwardSlash = url.lastIndexOf("/") + 1;
        String filename = url.substring(indexOfLastForwardSlash);
        filename = filename.replaceAll("%20", "_");
        return filename;
    }

    private void downloadManagerDisabledAlert() {
        Alert downloadManagerDisabled = new Alert(new android.view.ContextThemeWrapper(this.context, R.style.TxStateAlert), "Download Manager Disabled", "Would you like to enable it?");

        downloadManagerDisabled
                .setPositiveButton("Yes", (dialog, which) -> goToSettings())
                .setNegativeButton("Disable Downloads", (dialog, which) -> {
                    //User refuses to enable download manager
                })
                .show();
    }

    private class TypedBroadcastReceiver extends BroadcastReceiver {
        public TypedBroadcastReceiver(String mimeType) {
            super();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            long intentDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            boolean downloadIsForMe = intentDownloadId >= 0 && intentDownloadId == downloadId;
            if (downloadIsForMe && DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(intentDownloadId);
                Cursor cursor = null;
                String uri;
                String mime;
                try {
                    cursor = downloadManager.query(query);

                    if (cursor.moveToFirst()) {
                        int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            uri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                            mime = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE));
                            Uri localUri = Uri.parse(uri);
                            openFile(localUri, mime, intentDownloadId);
                        }
                    }
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }

            }
        }

        private void openFile(Uri file, String mimeType, Long downloadId) {
            if (FileDownloader.this.downloadStatus.get(downloadId)) {
                return;
            }
            boolean usingNougatOrAbove = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N;
            if (usingNougatOrAbove) { //We have to have a content URI vs. a file URI
                file = downloadManager.getUriForDownloadedFile(downloadId);
            }
            Intent openFileIntent = new Intent(Intent.ACTION_VIEW);
            openFileIntent.setDataAndType(file, mimeType);
            openFileIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            openFileIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            openFileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                FileDownloader.this.context.startActivity(openFileIntent);
                FileDownloader.this.downloadStatus.put(downloadId, true);
            } catch (ActivityNotFoundException e) {
                Toast noActivity = Toast.makeText(context, "Could not open downloaded file", Toast.LENGTH_LONG);
                noActivity.show();
                e.printStackTrace();
            }
        }
    }
}
