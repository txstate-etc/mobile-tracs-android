package edu.txstate.mobile.tracs;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import java.util.Observer;

import edu.txstate.mobile.tracs.util.LoginStatus;
import edu.txstate.mobile.tracs.util.MenuController;
import edu.txstate.mobile.tracs.util.http.HttpQueue;

public abstract class BaseTracsActivity extends AppCompatActivity implements Observer {
    private Tracker analyticsTracker;
    Menu optionsMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorHeader));
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorHeaderText));
        toolbar.setOverflowIcon(new IconDrawable(this, FontAwesomeIcons.fa_ellipsis_v)
                .colorRes(R.color.colorHeaderIcons)
                .actionBarSize()
        );

        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            IconDrawable homeIcon = new IconDrawable(this, FontAwesomeIcons.fa_home)
                    .colorRes(R.color.colorHeaderIcons)
                    .actionBarSize();
            actionBar.setHomeAsUpIndicator(homeIcon);
        }

        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        analyticsTracker = application.getDefaultTracker();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return MenuController.handleMenuClick(item.getItemId(), this) || super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LoginStatus.getInstance().addObserver(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LoginStatus.getInstance().deleteObserver(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelRequests();
    }

    void hitScreenView(String screen) {
        analyticsTracker.setScreenName(screen);
        analyticsTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    void setupOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.findItem(R.id.menu_notifications).setIcon(
                new IconDrawable(this, FontAwesomeIcons.fa_bell_o)
                        .colorRes(R.color.colorHeaderIcons)
                        .actionBarSize()
        ).setEnabled(LoginStatus.getInstance().isUserLoggedIn());
        this.optionsMenu = menu;
    }

    private void cancelRequests() {
        HttpQueue.getInstance(this).getRequestQueue().cancelAll(this);
    }
}
