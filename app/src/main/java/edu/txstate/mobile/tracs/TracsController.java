package edu.txstate.mobile.tracs;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceResponse;
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


class TracsController {
    private static final String TAG = "TracsController";
    private String urlToLoad;

    private FileDownloader fileDownloader;
    private Context context;
    private WebView tracsView;

    TracsController(WebView view) {
        this.context = view.getContext();
        this.tracsView = view;
        Init();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void Init() {
        this.fileDownloader = new FileDownloader(this.context);
        this.tracsView.setWebViewClient(new TracsWebViewClient());

        if (Build.VERSION.SDK_INT >= 19) {
            this.tracsView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            this.tracsView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        WebSettings tracs = this.tracsView.getSettings();

        tracs.setJavaScriptCanOpenWindowsAutomatically(true);
        tracs.setJavaScriptEnabled(true);
        tracs.setBuiltInZoomControls(true);
        tracs.setDisplayZoomControls(false);
        this.tracsView.addJavascriptInterface(this, "TracsController");
    }

    void downloadFile(String url, String mimetype) {
        this.fileDownloader.downloadFile(url, mimetype);
    }

    void loadUrl(String url) {
        this.urlToLoad = url;
        TracsClient.getInstance().verifySession(TracsController.this::onLoginResponse);
    }

    void loadHtml(String html) {
        this.tracsView.loadData(html, "text/html", null);
    }

    void setDownloadListener(DownloadListener downloadListener) {
        this.tracsView.setDownloadListener(downloadListener);
    }

    private void onLoginResponse(String session) {
        if (session != null) {
            LoginStatus.getInstance().login();
            AppStorage.put(AppStorage.SESSION_ID, session, AnalyticsApplication.getContext());
            CookieManager.getInstance().setCookie(context.getString(R.string.tracs_base), "JSESSIONID=" + session + "; Path=/;");
            tracsView.loadUrl(this.urlToLoad);
            return;
        }
        tracsView.loadUrl(context.getString(R.string.tracs_cas_login));
    }

    private void setSessionId(String sessionId) {
        AppStorage.put(AppStorage.SESSION_ID, sessionId, context);
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


    private class TracsWebViewClient extends WebViewClient {
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            Log.i(TAG, "URL: " + url);
            return super.shouldInterceptRequest(view, url);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            TracsController.this.loadHtml(PageLoader.getInstance().loadHtml("html/no_internet.html"));
            Log.wtf(TAG, description);
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
                prefs.edit().putString("user-agent", tracsView.getSettings().getUserAgentString()).commit();
                String cookies = CookieManager.getInstance().getCookie(url);
                String newCookie = null;
                if (cookies != null) {
                    newCookie = cookies.split("=")[1];
                }
                LoginStatus.getInstance().login();
                TracsController.this.setSessionId(newCookie);
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
