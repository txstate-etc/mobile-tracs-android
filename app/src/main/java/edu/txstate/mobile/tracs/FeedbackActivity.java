package edu.txstate.mobile.tracs;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Observable;

import edu.txstate.mobile.tracs.util.LoginStatus;
import edu.txstate.mobile.tracs.util.MenuController;

public class FeedbackActivity extends BaseTracsActivity {

    private static final String TAG = "FeedbackActivity";
    private static final String SCREEN_NAME = "Feedback";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_feedback);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume(){
        super.onResume();
        super.hitScreenView(SCREEN_NAME);
        final WebView webView = (WebView) findViewById(R.id.feedback_webview);

        try {
            String html = readFile("html/blank_page.html");
            webView.loadData(html, "text/html", null);
        } catch (IOException e) {
            Log.wtf(TAG, e.getMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.setupOptionsMenu(menu);
        super.optionsMenu.findItem(R.id.menu_feedback).setVisible(false);
        super.optionsMenu.findItem(R.id.menu_notifications).setEnabled(LoginStatus.getInstance().isUserLoggedIn());
        return true;
    }

    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    private String readFile(String file) throws IOException {
        InputStream input = getAssets().open(file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String         line;
        StringBuilder  stringBuilder = new StringBuilder();
        String         ls = System.getProperty("line.separator");

        try {
            while((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }

            return stringBuilder.toString();
        } finally {
            reader.close();
        }
    }

    @Override
    public void update(Observable o, Object arg) {

    }
}
