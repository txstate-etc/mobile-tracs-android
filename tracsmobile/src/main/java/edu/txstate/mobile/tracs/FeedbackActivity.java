package edu.txstate.mobile.tracs;

import android.os.Bundle;
import android.view.Menu;
import android.webkit.WebView;

import edu.txstate.mobile.tracs.util.LoginStatus;

public class FeedbackActivity extends BaseTracsActivity {

    private static final String TAG = "FeedbackActivity";
    private static final String SCREEN_NAME = "Feedback";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_feedback);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        super.hitScreenView(SCREEN_NAME);
        final WebView webView = (WebView) findViewById(R.id.feedback_webview);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setSupportZoom(true);
        webView.loadUrl(getString(R.string.feedback_survey));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.setupOptionsMenu(menu);
        super.optionsMenu.findItem(R.id.menu_feedback).setEnabled(false);
        super.optionsMenu.findItem(R.id.menu_notifications).setEnabled(LoginStatus.getInstance().isUserLoggedIn());
        return true;
    }
}
