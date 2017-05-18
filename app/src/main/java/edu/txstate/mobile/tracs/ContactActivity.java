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

import edu.txstate.mobile.tracs.util.LoginStatus;
import edu.txstate.mobile.tracs.util.MenuController;

public class ContactActivity extends AppCompatActivity {

    private Tracker analyticsTracker;

    private static final String TAG = "ContactActivity";
    private static final String SCREEN_NAME = "Contact Info";
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

        setContentView(R.layout.activity_contact);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        analyticsTracker.setScreenName(SCREEN_NAME);
        analyticsTracker.send(new HitBuilders.ScreenViewBuilder().build());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.findItem(R.id.menu_notifications).setIcon(
                new IconDrawable(this, FontAwesomeIcons.fa_bell_o)
                        .colorRes(R.color.colorAccent)
                        .actionBarSize()
        ).setEnabled(LoginStatus.getInstance().isUserLoggedIn());
        menu.findItem(R.id.menu_contact_us).setVisible(false);
        this.optionsMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuId = item.getItemId();
        return MenuController.handleMenuClick(menuId, this) || super.onOptionsItemSelected(item);
    }
}
