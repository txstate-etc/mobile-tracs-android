package edu.txstate.mobileapp.tracscompanion;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.HttpAuthHandler;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.android.volley.Response;

import java.util.HashMap;
import java.util.Map;

import edu.txstate.mobileapp.tracscompanion.listeners.UserIdListener;
import edu.txstate.mobileapp.tracscompanion.requests.AsyncTaskFactory;
import edu.txstate.mobileapp.tracscompanion.requests.Task;
import edu.txstate.mobileapp.tracscompanion.util.AppStorage;
import edu.txstate.mobileapp.tracscompanion.util.FileDownloader;
import edu.txstate.mobileapp.tracscompanion.util.TracsClient;
import edu.txstate.mobileapp.tracscompanion.util.http.HttpQueue;
import edu.txstate.mobileapp.tracscompanion.util.http.requests.TracsSessionRequest;
import edu.txstate.mobileapp.tracscompanion.util.http.responses.TracsSession;


class TracsController implements UserIdListener, Response.Listener<TracsSession> {
    private static final String TAG = "TracsController";

    private String tracsPortalUrl = "https://tracs.txstate.edu/portal";
    private final String loginUrl = "https://login.its.txstate.edu/login?" +
            "service=https%3A%2F%2Ftracs.txstate.edu%2Fsakai-login-tool%2Fcontainer";

    private FileDownloader fileDownloader;
    private Context context;
    private WebView tracsView;

    TracsController(WebView view) {
        this.context = view.getContext();
        this.tracsView = view;
        Init();
    }

    private void Init() {
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
        if ("".equals(AppStorage.get(AppStorage.USERNAME, context))) {
            tracsView.loadUrl(loginUrl);
        } else {
            HttpQueue requestQueue=HttpQueue.getInstance(context);
            Map<String, String> headers = new HashMap<>();
            requestQueue.addToRequestQueue(new TracsSessionRequest<>(
                    TracsSession.class, headers, TracsController.this,
                    error->Log.wtf(TAG,error))
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

        if (!userNameAndPass.equals(password)) {
            AppStorage.put(AppStorage.USERNAME, username, context);
            AppStorage.put(AppStorage.PASSWORD, password, context);
        }
    }

    @Override
    public void onRequestReturned(String userEid) {
        if (!userEid.isEmpty()) {
            AppStorage.put(AppStorage.TRACS_ID, userEid, context);
        }
    }

    private void getUserEid() {
        AsyncTask<String, Void, String> getUserId = AsyncTaskFactory.createTask(Task.TRACS_USER_ID, this);
        getUserId.execute("https://tracs.txstate.edu/direct/session.json",
                AppStorage.get(AppStorage.USERNAME, context),
                AppStorage.get(AppStorage.SESSION_ID, context));
    }

    @Override
    public void onResponse(TracsSession session) {
        String storedNetId = AppStorage.get(AppStorage.USERNAME, context);
        String fetchedNetId = session.getUserEid();

        //Session is good if this check passes
        if (storedNetId.equals(fetchedNetId)) {
            CookieManager.getInstance().setCookie(tracsPortalUrl, "JSESSIONID=" + session.getSessionId());
            tracsView.loadUrl(tracsPortalUrl);
        } else {
            //TODO: Get a god damned session man!
        }
    }

    private class TracsWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
            Log.i(TAG, host);
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

            if ("https://tracs.txstate.edu/portal".equals(url)) {
                String cookies = CookieManager.getInstance().getCookie(url);
                String newCookie = cookies.split("=")[1];
                String oldCookie = AppStorage.get(AppStorage.SESSION_ID, context);
                if (!newCookie.equals(oldCookie)) {
                    setSessionId(cookies.split("=")[1]);
                } else {
                    getUserEid();
                }
            }
        }
    }
}
