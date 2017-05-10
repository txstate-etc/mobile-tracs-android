package edu.txstate.mobile.tracs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.android.volley.VolleyError;

import edu.txstate.mobile.tracs.util.AppStorage;
import edu.txstate.mobile.tracs.util.FileDownloader;
import edu.txstate.mobile.tracs.util.LoginStatus;
import edu.txstate.mobile.tracs.util.Registrar;
import edu.txstate.mobile.tracs.util.http.HttpQueue;
import edu.txstate.mobile.tracs.util.http.requests.TracsLoginRequest;


class TracsController {
    private static final String TAG = "TracsController";
    private final String LOGIN_URL = AnalyticsApplication.getContext().getString(R.string.tracs_login);
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
        LoginStatus.getInstance().logout();
        this.fileDownloader = new FileDownloader(this.context);
        this.tracsView.setWebViewClient(new TracsWebViewClient());

        if (Build.VERSION.SDK_INT >= 19) {
            this.tracsView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            this.tracsView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        this.tracsView.getSettings().setJavaScriptEnabled(true);
        this.tracsView.getSettings().setBuiltInZoomControls(true);
        this.tracsView.getSettings().setDisplayZoomControls(false);
        this.tracsView.addJavascriptInterface(this, "TracsController");
    }

    void downloadFile(String url, String mimetype) {
        this.fileDownloader.downloadFile(url, mimetype);
    }

    void loadUrl(String url) {
        this.urlToLoad = url;
        String userId = AppStorage.get(AppStorage.USERNAME, context);
        if ("".equals(userId)) {
            LoginStatus.getInstance().logout();
            this.urlToLoad = LOGIN_URL;
            tracsView.loadUrl(this.urlToLoad);
        } else {
            HttpQueue requestQueue=HttpQueue.getInstance(context);
            String loginSessionUrl = AnalyticsApplication.getContext().getString(R.string.tracs_base) +
                    AnalyticsApplication.getContext().getString(R.string.tracs_session_login);
            requestQueue.addToRequestQueue(new TracsLoginRequest(
                    loginSessionUrl, TracsController.this::onResponse,
                    TracsController.this::onLoginError), TAG);
        }
    }

    void setDownloadListener(DownloadListener downloadListener) {
        this.tracsView.setDownloadListener(downloadListener);
    }

    private void onLoginError(VolleyError error) {
        try {
            Log.wtf(TAG, new String(error.networkResponse.data));
        } catch (NullPointerException e) {
            Log.wtf(TAG, "Couldn't login, no error given.");
        }
        this.urlToLoad = LOGIN_URL;
        tracsView.loadUrl(this.urlToLoad);
    }

    private void onResponse(String session) {
        LoginStatus.getInstance().login();
        AppStorage.put(AppStorage.SESSION_ID, session, AnalyticsApplication.getContext());
        CookieManager.getInstance().setCookie(this.urlToLoad, "JSESSIONID=" + session + "; Path=/;");
        tracsView.loadUrl(this.urlToLoad);

    }

    private void setSessionId(String sessionId) {
        AppStorage.put(AppStorage.SESSION_ID, sessionId, context);
    }

    @JavascriptInterface
    public void deliver(String username, String password) {
        String userNameAndPass = AppStorage.get(AppStorage.USERNAME, context)
                               + AppStorage.get(AppStorage.PASSWORD, context);

        if (!userNameAndPass.equals(username + password)) {
            AppStorage.put(AppStorage.USERNAME, username, context);
            AppStorage.put(AppStorage.PASSWORD, password, context);
        }
        LoginStatus.getInstance().login();
    }


    private class TracsWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @SuppressLint("ApplySharedPref")
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Log.wtf(TAG, url);
            Context context = AnalyticsApplication.getContext();
            String loginUrl = context.getString(R.string.tracs_login);
            String loginSuccessUrl = context.getString(R.string.tracs_login_success);
            String logoutUrl = context.getString(R.string.tracs_logout);

            if (url.equals(loginUrl)) {
                LoginStatus.getInstance().logout();
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
                Log.i(TAG, "Registration Info");
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
