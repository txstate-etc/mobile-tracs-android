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
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.android.volley.VolleyError;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.io.IOException;
import java.io.InputStream;

import edu.txstate.mobile.tracs.util.AppStorage;
import edu.txstate.mobile.tracs.util.FileDownloader;
import edu.txstate.mobile.tracs.util.LoginStatus;
import edu.txstate.mobile.tracs.util.PageLoader;
import edu.txstate.mobile.tracs.util.Registrar;
import edu.txstate.mobile.tracs.util.SettingsStore;
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
    public void deliver(String username, String password) {
        AppStorage.put(AppStorage.USERNAME, username.toLowerCase().trim(), context);
        AppStorage.remove(AppStorage.PASSWORD, context);
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);

        boolean shouldStorePassword = keyguardManager.isKeyguardSecure();
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

    private class TracsWebViewClient extends WebViewClient {
        private static final String TAG = "TracsWebViewClient";

        private int attempts = 0;
        private final int maxAttempts = 5;
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            TracsWebView.class.cast(view).loadHtml(PageLoader.getInstance().loadHtml("html/no_internet.html"));
            Log.e(TAG, description);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            Tracker tracker = AnalyticsApplication.getDefaultTracker();
            tracker.send(new HitBuilders.EventBuilder()
            .setCategory("Link")
            .setAction(context.getString(R.string.click_event))
            .setLabel(url)
            .build());
            return super.shouldInterceptRequest(view, url);
        }

        @SuppressLint("ApplySharedPref")
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            Context context = AnalyticsApplication.getContext();
            String loginUrl = context.getString(R.string.tracs_cas_login);
            String loginSuccessUrl = context.getString(R.string.tracs_login_success);
            String logoutUrl = context.getString(R.string.tracs_logout);

            if (url.equals(loginUrl)) {
                LoginStatus.getInstance().logout();
                view.evaluateJavascript("document.querySelector('form input[name=\"publicWorkstation\"]').style = \"display:none;\"", null);
                view.evaluateJavascript("document.querySelector('form label[for=\"publicWorkstation\"]').style = \"display:none;\"", null);
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
                Registrar.getInstance().registerDevice(this::onResponse, this::onRegisterError);
                SharedPreferences prefs = AnalyticsApplication.getContext().getSharedPreferences("cas", Context.MODE_PRIVATE);
                prefs.edit().putString("user-agent", view.getSettings().getUserAgentString()).commit();
                String cookies[] = CookieManager.getInstance().getCookie(url).split(";");
                String newCookie = null;
                for (String cookie : cookies) {
                    String cookieParts[] = cookie.split("=");
                    if ("JSESSIONID".equals(cookieParts[0].trim())) {
                        newCookie = cookieParts[1];
                    }
                }
                TracsWebView.class.cast(view).setSessionId(newCookie);
            }

            if (logoutUrl.equals(url)) {
                LoginStatus.getInstance().logout();
                Registrar.getInstance().unregisterDevice();
                SettingsStore.getInstance().clear();
                AppStorage.clear(AnalyticsApplication.getContext());
            }
        }

        private void onRegisterError(VolleyError volleyError) {
            attempts += 1;
            Log.e(TAG, "Failure Count: " + attempts);
            if (haveAttemptsLeft()) {
                Registrar.getInstance().registerDevice(this::onResponse, this::onRegisterError);
            }
        }

        private void onResponse() {
            SettingsStore.getInstance().saveSettings();
            LoginStatus.getInstance().login();
        }

        private boolean haveAttemptsLeft() {
            return this.attempts < this.maxAttempts;
        }
    }
}