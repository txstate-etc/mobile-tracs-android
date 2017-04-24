package edu.txstate.mobileapp.tracscompanion;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.securepreferences.SecurePreferences;

import edu.txstate.mobileapp.tracscompanion.util.AppInstanceId;
import edu.txstate.mobileapp.tracscompanion.util.FileDownloader;


class TracsController {
    private static final String TAG = "TracsController";

    private String url;
    private FileDownloader fileDownloader;
    private Context context;
    private WebView tracsView;

    TracsController(WebView view, String url) {
        this.context = view.getContext();
        this.tracsView = view;
        Init(url);
    }

    private void Init(String url) {
        this.url = url;
        this.fileDownloader = new FileDownloader(this.context);
        this.tracsView.setWebViewClient(new TracsWebViewClient());
    }

    void downloadFile(String url, String mimetype) {
        this.fileDownloader.downloadFile(url, mimetype);
    }

    void javaScriptEnabled(boolean isEnabled) {
        this.tracsView.getSettings().setJavaScriptEnabled(isEnabled);
        this.tracsView.addJavascriptInterface(this, "TracsController");
    }

    void zoomEnabled(boolean isEnabled) {
        this.tracsView.getSettings().setBuiltInZoomControls(isEnabled);
        this.tracsView.getSettings().setDisplayZoomControls(false);
    }

    void loadUrl() {
        tracsView.loadUrl(this.url);
    }

    void setDownloadListener(DownloadListener downloadListener) {
        this.tracsView.setDownloadListener(downloadListener);
    }

    @JavascriptInterface
    public void deliver(String username, String password) {
        String encryptionKey = AppInstanceId.getKey(context).toString();
        SharedPreferences prefs = new SecurePreferences(this.context, encryptionKey, "user_based_prefs.xml");
        String currentPass = prefs.getString("password", "");

        //A null object is returned if the stored info can't be decrypted.
        if (currentPass == null) { currentPass = ""; }

        if (!currentPass.equals(password)) {
            prefs.edit().putString("username", username).apply();
            prefs.edit().putString("password", password).apply();
        }
    }

    private class TracsWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (url.contains("login.its.txstate.edu")) {
                String javascript = "document.getElementsByTagName('form')[0].onsubmit = function() {\n" +
                        "\tvar username, password;\n" +
                        "\tvar inputs = document.getElementsByTagName('input');\n" +
                        "\tfor (var i = 0; i < inputs.length; i++) {\n" +
                        "\t\tif (inputs[i].name.toLowerCase() === 'password') {\n" +
                        "\t\t\tpassword = inputs[i] == null ? \"\" : inputs[i].value;\n" +
                        "\t\t} else if (inputs[i].name.toLowerCase() === 'username') {\n" +
                        "\t\t\tusername = inputs[i] === null ? \"\" : inputs[i].value;\n" +
                        "\t\t}\n" +
                        "\t}\n" +
                        "\twindow.TracsController.deliver(username, password);\n" +
                        "\treturn true;\n" +
                        "}";
                view.loadUrl("javascript:" + javascript);
            }
        }
    }
}
