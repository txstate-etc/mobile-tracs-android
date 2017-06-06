package edu.txstate.mobile.tracs;


import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.IOException;
import java.io.InputStream;

import edu.txstate.mobile.tracs.util.AppStorage;
import edu.txstate.mobile.tracs.util.FileDownloader;
import edu.txstate.mobile.tracs.util.LoginStatus;
import edu.txstate.mobile.tracs.util.PageLoader;
import edu.txstate.mobile.tracs.util.Registrar;
import edu.txstate.mobile.tracs.util.TracsClient;

public class TracsWebView extends WebView {

    private static final String TAG = "TracsWebView";

    private Context context;
    private FileDownloader fileDownloader;
    private String urlToLoad;

    public TracsWebView(Context context) {
        super(context);
        init(context);
    }

    public TracsWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TracsWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @SuppressLint({"AddJavascriptInterface", "SetJavaScriptEnabled", "ClickableViewAccessibility"})
    private void init(Context context) {
        this.context = context;
        this.fileDownloader = new FileDownloader(this.context);
        setWebViewClient(new TracsWebViewClient());
        setWebChromeClient(new TracsWebChromeClient(this.context));

        getSettings().setSupportZoom(true);
        getSettings().setBuiltInZoomControls(true);
        getSettings().setDisplayZoomControls(false);
        getSettings().setJavaScriptEnabled(true);
        getSettings().setLoadWithOverviewMode(true);
        getSettings().setUseWideViewPort(true);
        addJavascriptInterface(this, "TracsWebView");
        setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> downloadFile(url, mimetype));
    }

    @JavascriptInterface
    public void deliver(String username, String password, boolean privateDevice) {
        AppStorage.put(AppStorage.USERNAME, username.toLowerCase(), context);
        AppStorage.remove(AppStorage.PASSWORD, context);
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);

        boolean shouldStorePassword = keyguardManager.isKeyguardSecure() && privateDevice;
        if (shouldStorePassword) {
            AppStorage.put(AppStorage.PASSWORD, password, context);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (this.canGoBack()) {
                        this.goBack();
                        return true;
                    }
            }
        }
        return super.onKeyDown(keyCode, event);

    }

    public void loadUrl(String url, boolean shouldVerifySession) {
        this.urlToLoad = url;
        if (shouldVerifySession) {
            TracsClient.getInstance().verifySession(TracsWebView.this::onLoginResponse);
        } else {
            loadUrl(this.urlToLoad);
        }
    }

    private void onLoginResponse(String session) {
        if (session != null) {
            LoginStatus.getInstance().login();
            setSessionId(session);
            CookieManager.getInstance().setCookie(context.getString(R.string.tracs_base), "JSESSIONID=" + session + "; Path=/;");
            this.loadUrl(this.urlToLoad, false);
        } else {
            loadUrl(context.getString(R.string.tracs_cas_login), false);
        }
    }

    private void downloadFile(String url, String mimetype) {
        fileDownloader.downloadFile(url, mimetype);
    }

    public void loadHtml(String html) {
        loadData(html, "text/html", null);
    }

    public void setSessionId(String sessionId) {
        AppStorage.put(AppStorage.SESSION_ID, sessionId, context);
    }

    private class TracsWebChromeClient extends WebChromeClient {
        Context context;

        public TracsWebChromeClient(Context context) {
            super();
            this.context = context;
        }
    }

    private class TracsWebViewClient extends WebViewClient {
        private static final String TAG = "TracsWebViewClient";

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return false;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            TracsWebView.class.cast(view).loadHtml(PageLoader.getInstance().loadHtml("html/no_internet.html"));
            Log.wtf(TAG, description);
        }

        @SuppressLint("ApplySharedPref")
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            Log.i(TAG, url);

            Context context = AnalyticsApplication.getContext();
            String loginUrl = context.getString(R.string.tracs_cas_login);
            String loginSuccessUrl = context.getString(R.string.tracs_login_success);
            String logoutUrl = context.getString(R.string.tracs_logout);

            if (url.equals(loginUrl)) {
                LoginStatus.getInstance().logout();
                String javascript;
                try {
                    InputStream input = context.getAssets().open("js/logins.js");
                    byte[] buffer = new byte[input.available()];
                    input.read(buffer);
                    input.close();
                    javascript = Base64.encodeToString(buffer, Base64.NO_WRAP);
                    view.loadUrl("javascript:(function() {" +
                            "var parent = document.getElementsByTagName('head').item(0);" +
                            "var script = document.createElement('script');" +
                            "script.type = 'text/javascript';" +
                            "script.innerHTML = window.atob('" + javascript + "');" +
                            "parent.appendChild(script)" +
                            "})()");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (url.equals(loginSuccessUrl)) {
                SharedPreferences prefs = AnalyticsApplication.getContext().getSharedPreferences("cas", Context.MODE_PRIVATE);
                prefs.edit().putString("user-agent", view.getSettings().getUserAgentString()).commit();
                String cookies = CookieManager.getInstance().getCookie(url);
                String newCookie = null;
                if (cookies != null) {
                    newCookie = cookies.split("=")[1];
                }
                LoginStatus.getInstance().login();
                TracsWebView.class.cast(view).setSessionId(newCookie);
                Registrar.getInstance().registerDevice();
            }

            if (logoutUrl.equals(url)) {
                LoginStatus.getInstance().logout();
                AppStorage.remove(AppStorage.USERNAME, AnalyticsApplication.getContext());
                AppStorage.remove(AppStorage.PASSWORD, AnalyticsApplication.getContext());
                AppStorage.remove(AppStorage.SESSION_ID, AnalyticsApplication.getContext());
            }
        }
    }
}