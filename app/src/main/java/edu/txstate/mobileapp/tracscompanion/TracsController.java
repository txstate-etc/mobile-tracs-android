package edu.txstate.mobileapp.tracscompanion;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.ViewTreeObserver;
import android.webkit.DownloadListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import edu.txstate.mobileapp.tracscompanion.util.FileDownloader;


@SuppressLint("ViewConstructor")
public class TracsController {
    private String url;
    private ViewTreeObserver.OnScrollChangedListener scrollChangedListener;
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

    public void downloadFile(String url, String mimetype) {
        this.fileDownloader.downloadFile(url, mimetype);
    }

    public void javaScriptEnabled(boolean isEnabled) {
        this.tracsView.getSettings().setJavaScriptEnabled(isEnabled);
    }

    public void zoomEnabled(boolean isEnabled) {
        this.tracsView.getSettings().setBuiltInZoomControls(isEnabled);
        this.tracsView.getSettings().setDisplayZoomControls(false);
    }

    public void loadUrl() {
        tracsView.loadUrl(this.url);
    }

    public void setDownloadListener(DownloadListener downloadListener) {
        this.tracsView.setDownloadListener(downloadListener);
    }

    private class TracsWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
}
