package edu.txstate.mobileapp.mobileandroid.util;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

import edu.txstate.mobileapp.mobileandroid.R;


public class FileDownloader {

    private Context context;
    private DownloadManager downloadManager;
    private BroadcastReceiver onDownloadCompletion;
    private ProgressDialog downloadProgress;

    private ArrayList<String> microsoftMimeTypes = new ArrayList<>(Arrays.asList(
            "application/msword",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.template",
            "application/vnd.ms-word.document.macroEnabled.12",
            "application/vnd.ms-word.template.macroEnabled.12",
            "application/vnd.ms-excel",
            "application/vnd.ms-excel",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.template",
            "application/vnd.ms-excel.sheet.macroEnabled.12",
            "application/vnd.ms-excel.template.macroEnabled.12",
            "application/vnd.ms-excel.addin.macroEnabled.12",
            "application/vnd.ms-excel.sheet.binary.macroEnabled.12",
            "application/vnd.ms-powerpoint",
            "application/vnd.ms-powerpoint",
            "application/vnd.ms-powerpoint",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "application/vnd.openxmlformats-officedocument.presentationml.template",
            "application/vnd.openxmlformats-officedocument.presentationml.slideshow",
            "application/vnd.ms-powerpoint.addin.macroEnabled.12",
            "application/vnd.ms-powerpoint.presentation.macroEnabled.12",
            "application/vnd.ms-powerpoint.template.macroEnabled.12",
            "application/vnd.ms-powerpoint.slideshow.macroEnabled.12",
            "application/vnd.ms-access"
    ));

    private static final String DOWNLOAD_MANAGER_PACKAGE_NAME = "com.android.providers.downloads";
    private long downloadId = -1;

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

            this.onDownloadCompletion = new TypedBroadcastReceiver(mimeType);
            this.context.registerReceiver(this.onDownloadCompletion, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

            DownloadManager.Query isDownloadingQuery = new DownloadManager.Query();

            isDownloadingQuery.setFilterByStatus(
                    DownloadManager.STATUS_PAUSED |
                    DownloadManager.STATUS_PENDING |
                    DownloadManager.STATUS_RUNNING |
                    DownloadManager.STATUS_SUCCESSFUL
            );

            Cursor downloadStatusCursor = downloadManager.query(isDownloadingQuery);
            int filePathColumn = downloadStatusCursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);

            for (downloadStatusCursor.moveToFirst(); !downloadStatusCursor.isAfterLast(); downloadStatusCursor.moveToNext()) {
                String newFileName = downloadStatusCursor.getString(filePathColumn).substring(downloadStatusCursor.getString(filePathColumn).lastIndexOf("/") + 1);
                if (fileName.equals(newFileName)) {
                    //TODO: Figure out if multiple file names are ok to download.
                }
            }
            this.downloadId = downloadManager.enqueue(request);
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
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        goToSettings();
                    }
                })
                .setNegativeButton("Disable Downloads", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //TODO: Implement any necessary cancel actions
                    }
                })
                .show();
    }

    private class TypedBroadcastReceiver extends BroadcastReceiver {
        private String mimeType;

        public TypedBroadcastReceiver(String mimeType) {
            super();
            this.mimeType = mimeType;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            long intentDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            boolean downloadIsForMe = intentDownloadId >= 0 && intentDownloadId == downloadId;
            if (downloadIsForMe) {
                Uri localUri = downloadManager.getUriForDownloadedFile(downloadId);
                openFile(localUri);
            }
        }

        private void openFile(Uri file) {
            Intent openFileIntent;
            if (microsoftMimeTypes.contains(mimeType)) {
                openFileIntent = new Intent(Intent.ACTION_VIEW);
                openFileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                openFileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                openFileIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                openFileIntent.setDataAndTypeAndNormalize(file, mimeType);
            } else {
                openFileIntent = new Intent(Intent.ACTION_VIEW);
                openFileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                openFileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                openFileIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                openFileIntent.setDataAndNormalize(file);
            }

            try {
                context.startActivity(openFileIntent);
            } catch (ActivityNotFoundException e) {
                makeLongToast("No viewer application found");
            }
        }

        private void makeLongToast(String message) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
    }
}


