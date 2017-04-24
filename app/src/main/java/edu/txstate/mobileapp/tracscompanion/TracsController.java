package edu.txstate.mobileapp.tracscompanion;

import android.content.Context;
import android.os.AsyncTask;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import edu.txstate.mobileapp.tracscompanion.listeners.UserIdListener;
import edu.txstate.mobileapp.tracscompanion.requests.AsyncTaskFactory;
import edu.txstate.mobileapp.tracscompanion.requests.Task;
import edu.txstate.mobileapp.tracscompanion.util.AppStorage;
import edu.txstate.mobileapp.tracscompanion.util.FileDownloader;


class TracsController implements UserIdListener {
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
        //Before loading the url we should check to see if the user has a valid
        //session ID stored and if they do, make sure the cookie is set. If they do not
        //then the program should log them in to CAS and get the JSESSIONID and store it
        //then come back here and load the url with the right cookie.
        tracsView.loadUrl(this.url);
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
    public void onRequestReturned() {

    }

    @Override
    public void onRequestReturned(String userEid) {
        if (!userEid.isEmpty()) {
            AppStorage.put(AppStorage.TRACS_ID, userEid, context);
        }
        Toast.makeText(context, AppStorage.get(AppStorage.TRACS_ID, context), Toast.LENGTH_LONG).show();
    }

    private void getUserEid() {
        AsyncTask<String, Void, String> getUserId = AsyncTaskFactory.createTask(Task.TRACS_USER_ID, this);
        getUserId.execute("https://tracs.txstate.edu/direct/session.json",
                AppStorage.get(AppStorage.USERNAME, context),
                AppStorage.get(AppStorage.SESSION_ID, context));
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
