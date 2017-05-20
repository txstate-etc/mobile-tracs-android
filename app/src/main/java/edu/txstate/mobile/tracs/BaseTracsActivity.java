package edu.txstate.mobile.tracs;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.widget.IconTextView;

import java.util.Observable;
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
        cancelRequests();
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
        this.optionsMenu = menu;
        MenuItem notificationIcon = menu.findItem(R.id.menu_notifications);
        notificationIcon.setActionView(R.layout.notification_menu_item);
        notificationIcon.getActionView().setOnClickListener(v -> MenuController.handleMenuClick(notificationIcon.getItemId(), BaseTracsActivity.this));
        updateNotificationButtonStatus(LoginStatus.getInstance().isUserLoggedIn());
    }

    @Override
    public void update(Observable loginStatus, Object isLoggedIn) {
        boolean loggedIn = (Boolean) isLoggedIn;
        updateNotificationButtonStatus(loggedIn);
    }

    private void updateNotificationButtonStatus(boolean loggedIn) {
        MenuItem notification = this.optionsMenu.findItem(R.id.menu_notifications);
        notification.getActionView().setEnabled(loggedIn);
        notification.getActionView().setAlpha(loggedIn ? 1.0f : 0.5f);
    }

    private void cancelRequests() {
        HttpQueue.getInstance(this).getRequestQueue().cancelAll(this);
    }
}
