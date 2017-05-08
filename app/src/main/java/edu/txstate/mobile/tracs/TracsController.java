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

import java.util.HashMap;
import java.util.Map;

import edu.txstate.mobile.tracs.util.AppStorage;
import edu.txstate.mobile.tracs.util.FileDownloader;
import edu.txstate.mobile.tracs.util.LoginStatus;
import edu.txstate.mobile.tracs.util.Registrar;
import edu.txstate.mobile.tracs.util.TracsClient;
import edu.txstate.mobile.tracs.util.http.HttpQueue;
import edu.txstate.mobile.tracs.util.http.requests.TracsLoginRequest;
import edu.txstate.mobile.tracs.util.http.requests.TracsSessionRequest;
import edu.txstate.mobile.tracs.util.http.responses.TracsSession;


class TracsController {
    private static final String TAG = "TracsController";
    private final String loginUrl = AnalyticsApplication.getContext().getString(R.string.cas_login_tracs);
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
        this.urlToLoad = "https://tracs.txstate.edu/portal";

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
            tracsView.loadUrl(loginUrl);
        } else {
            HttpQueue requestQueue=HttpQueue.getInstance(context);
            Map<String, String> headers = new HashMap<>();
            requestQueue.addToRequestQueue(new TracsSessionRequest<>(
                    TracsSession.class, headers,
                    TracsController.this::onResponse,
                    error->Log.wtf(TAG,error)
                ), TAG
            );
        }
    }

    void setDownloadListener(DownloadListener downloadListener) {
        this.tracsView.setDownloadListener(downloadListener);
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

    private void onResponse(TracsSession session) {
        String storedNetId = AppStorage.get(AppStorage.USERNAME, context);
        String fetchedNetId = session.getUserEid();

        if (storedNetId.equals(fetchedNetId)) { //Session is valid
            tracsView.loadUrl(urlToLoad);
            LoginStatus.getInstance().login();
        } else { //Session is not valid
            LoginStatus.getInstance().logout();
            HttpQueue.getInstance(AnalyticsApplication.getContext()).addToRequestQueue(
                    new TracsLoginRequest(TracsClient.LOGIN_URL,
                            response -> {
                                CookieManager.getInstance().setCookie(urlToLoad, "JSESSIONID=" + response + "; Path=/");
                                tracsView.loadUrl(urlToLoad);
                            },
                            error -> tracsView.loadUrl(loginUrl)), TAG);
        }
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
            String loginUrl = "https://login.its.txstate.edu/login?" +
                    "service=https%3A%2F%2Ftracs.txstate.edu%2Fsakai-login-tool%2Fcontainer";
            String loginSuccessUrl = "https://tracs.txstate.edu/sakai-login-tool/container?ticket";
            String logoutUrl = "https://login.its.txstate.edu/logout?url=https://tracs.txstate.edu";



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
                Registrar.getInstance().getJwt();
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