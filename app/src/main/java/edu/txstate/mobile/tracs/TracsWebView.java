package edu.txstate.mobile.tracs;


import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
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

    @SuppressLint({"AddJavascriptInterface", "SetJavaScriptEnabled"})
    private void init(Context context) {
        this.context = context;
        this.fileDownloader = new FileDownloader(this.context);
        setWebViewClient(new TracsWebViewClient());
        setWebChromeClient(new TracsWebChromeClient());
        if (Build.VERSION.SDK_INT >= 19) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        WebSettings tracs = getSettings();
        tracs.setJavaScriptCanOpenWindowsAutomatically(true);
        tracs.setJavaScriptEnabled(true);
        tracs.setBuiltInZoomControls(true);
        tracs.setDisplayZoomControls(false);
        addJavascriptInterface(this, "TracsWebView");
        setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> downloadFile(url, mimetype));
    }

    @JavascriptInterface
    public void deliver(String username, String password, boolean privateDevice) {
        AppStorage.put(AppStorage.USERNAME, username, context);
        AppStorage.remove(AppStorage.PASSWORD, context);
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);

        boolean shouldStorePassword = keyguardManager.isKeyguardSecure() && privateDevice;
        if (shouldStorePassword) {
            AppStorage.put(AppStorage.PASSWORD, password, context);
        }
    }


    public void loadUrl(String url, boolean shouldVerifySession) {
        this.urlToLoad = url;
        if (shouldVerifySession) {
            TracsClient.getInstance().verifySession(TracsWebView.this::onLoginResponse);
        } else {
            loadUrl(this.urlToLoad);
        }
    }

    @Override
    public void postUrl(String url, byte[] postData) {
        Log.i(TAG, url);
//        super.postUrl(url, postData);
    }



    private void onLoginResponse(String session) {
        if (session != null) {
            LoginStatus.getInstance().login();
            AppStorage.put(AppStorage.SESSION_ID, session, AnalyticsApplication.getContext());
            CookieManager.getInstance().setCookie(context.getString(R.string.tracs_base), "JSESSIONID=" + session + "; Path=/;");
            this.loadUrl(this.urlToLoad, false);
            return;
        }
        loadUrl(context.getString(R.string.tracs_login), false);
    }

    private void downloadFile(String url, String mimetype) {
        fileDownloader.downloadFile(url, mimetype);
    }

    private void loadHtml(String html) {
        loadData(html, "text/html", null);
    }

    private void setSessionId(String sessionId) {
        AppStorage.put(AppStorage.SESSION_ID, sessionId, context);
    }

    private class TracsWebChromeClient extends WebChromeClient {
        private static final String TAG = "TracsWebChromeClient";
        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            Log.i(TAG, consoleMessage.message());
            return super.onConsoleMessage(consoleMessage);
        }



        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
            Log.i(TAG, resultMsg.toString());
            return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg);
        }
    }

    private class TracsWebViewClient extends WebViewClient {
        private static final String TAG = "TracsWebViewClient";

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.i(TAG, url);
            return false;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            TracsWebView.this.loadHtml(PageLoader.getInstance().loadHtml("html/no_internet.html"));
            Log.wtf(TAG, description);
        }

        @SuppressLint("ApplySharedPref")
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            Context context = AnalyticsApplication.getContext();
            String loginUrl = context.getString(R.string.tracs_login);
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

            if (url.contains(loginSuccessUrl)) {
                SharedPreferences prefs = AnalyticsApplication.getContext().getSharedPreferences("cas", Context.MODE_PRIVATE);
                prefs.edit().putString("user-agent", getSettings().getUserAgentString()).commit();
                String cookies = CookieManager.getInstance().getCookie(url);
                String newCookie = null;
                if (cookies != null) {
                    newCookie = cookies.split("=")[1];
                }
                LoginStatus.getInstance().login();
                TracsWebView.this.setSessionId(newCookie);
                Registrar.getInstance().registerDevice();
            }

            if (logoutUrl.equals(url)) {
                LoginStatus.getInstance().logout();
                //TODO: What the hell am I doing, I made an observer pattern to handle this.
                AppStorage.remove(AppStorage.USERNAME, AnalyticsApplication.getContext());
                AppStorage.remove(AppStorage.PASSWORD, AnalyticsApplication.getContext());
                AppStorage.remove(AppStorage.SESSION_ID, AnalyticsApplication.getContext());
            }
        }
    }
}
