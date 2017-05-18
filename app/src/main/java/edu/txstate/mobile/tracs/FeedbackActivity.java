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
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import edu.txstate.mobile.tracs.util.LoginStatus;
import edu.txstate.mobile.tracs.util.MenuController;

public class FeedbackActivity extends AppCompatActivity {

    private Tracker analyticsTracker;

    private static final String TAG = "FeedbackActivity";
    private static final String SCREEN_NAME = "Feedback";
    private Menu optionsMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Analytics tracker setup for this view.
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        analyticsTracker = application.getDefaultTracker();
    }

    @Override
    protected void onResume(){
        super.onResume();

        setContentView(R.layout.activity_feedback);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        analyticsTracker.setScreenName(SCREEN_NAME);
        analyticsTracker.send(new HitBuilders.ScreenViewBuilder().build());

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
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.findItem(R.id.menu_notifications).setIcon(
                new IconDrawable(this, FontAwesomeIcons.fa_bell_o)
                        .colorRes(R.color.colorAccent)
                        .actionBarSize()
        ).setEnabled(LoginStatus.getInstance().isUserLoggedIn());
        menu.findItem(R.id.menu_feedback).setVisible(false);
        this.optionsMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuId = item.getItemId();
        return MenuController.handleMenuClick(menuId, this) || super.onOptionsItemSelected(item);
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
}
