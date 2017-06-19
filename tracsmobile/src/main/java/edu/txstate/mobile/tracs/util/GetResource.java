package edu.txstate.mobile.tracs.util;

import android.os.AsyncTask;
import android.webkit.MimeTypeMap;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * This may not be a necessary class anymore since MIME type is detected
 * by the download manager after download.
 */
public class GetResource extends AsyncTask<String, Void, String> {
    @Override
    protected String doInBackground(String... params) {
        URL url;
        HttpURLConnection urlConnection;

        try {
            url = new URL(params[0]);
            urlConnection = (HttpURLConnection) url.openConnection();

            int responseCode = urlConnection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                String extension = MimeTypeMap.getFileExtensionFromUrl(url.toString());

                if (!extension.isEmpty()) {
                    String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
