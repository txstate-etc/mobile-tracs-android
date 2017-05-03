package edu.txstate.mobileapp.tracscompanion;

import android.annotation.SuppressLint;
import android.content.Context;
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

import edu.txstate.mobileapp.tracscompanion.util.AppStorage;
import edu.txstate.mobileapp.tracscompanion.util.FileDownloader;
import edu.txstate.mobileapp.tracscompanion.util.LoginStatus;
import edu.txstate.mobileapp.tracscompanion.util.TracsClient;
import edu.txstate.mobileapp.tracscompanion.util.http.HttpQueue;
import edu.txstate.mobileapp.tracscompanion.util.http.requests.TracsLoginRequest;
import edu.txstate.mobileapp.tracscompanion.util.http.requests.TracsSessionRequest;
import edu.txstate.mobileapp.tracscompanion.util.http.responses.TracsSession;


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


    private void getUserEid() {
        Map<String, String> headers = new HashMap<>();
        HttpQueue.getInstance(AnalyticsApplication.getContext()).addToRequestQueue(
                new TracsSessionRequest<>(
                        TracsSession.class, headers,
                        TracsController.this::onUserEidReturned,
                        error -> Log.wtf(TAG, error.getMessage())
                ), TAG
        );
    }

    private void onUserEidReturned(TracsSession session) {
        AppStorage.put(AppStorage.USERNAME, session.getUserEid(), context);
    }

    private void onResponse(TracsSession session) {
        String storedNetId = AppStorage.get(AppStorage.USERNAME, context);
        String fetchedNetId = session.getUserEid();

        //Session is good if this check passes
        if (storedNetId.equals(fetchedNetId)) {
            tracsView.loadUrl(urlToLoad);
            LoginStatus.getInstance().login();
        } else {
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
                String cookies = CookieManager.getInstance().getCookie(url);
                String newCookie = cookies.split("=")[1];
                String oldCookie = AppStorage.get(AppStorage.SESSION_ID, context);
                LoginStatus.getInstance().login();
                if (!newCookie.equals(oldCookie)) {
                    setSessionId(cookies.split("=")[1]);
                } else {
                    TracsController.this.getUserEid();
                }
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
