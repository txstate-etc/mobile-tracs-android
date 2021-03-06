package edu.txstate.mobile.tracs;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.iid.FirebaseInstanceId;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import java.util.Observable;
import java.util.Observer;

import edu.txstate.mobile.tracs.util.AppStorage;
import edu.txstate.mobile.tracs.util.LoginStatus;
import edu.txstate.mobile.tracs.util.MenuController;
import edu.txstate.mobile.tracs.util.http.HttpQueue;
import edu.txstate.mobile.tracs.util.http.requests.NotificationCountRequest;

public abstract class BaseTracsActivity extends AppCompatActivity implements Observer {
    private Tracker analyticsTracker;
    private BroadcastReceiver messageReceiver;
    private static final String TAG = "BaseTracsActivity";
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

        analyticsTracker = AnalyticsApplication.getDefaultTracker();
        analyticsTracker.enableExceptionReporting(true);

        messageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                setBadgeCount(getCurrentBadgeCount()+1);
            }
        };
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return MenuController.handleMenuClick(item.getItemId(), this)
               || super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (deviceIsNotSecured()) {
            AppStorage.remove(AppStorage.PASSWORD, AnalyticsApplication.getContext());
        }
        LoginStatus.getInstance().addObserver(this);
        this.registerReceiver(messageReceiver, new IntentFilter("badge_count"));
        getBadgeCount(LoginStatus.getInstance().isUserLoggedIn());
    }

    @Override
    protected void onPause() {
        LoginStatus.getInstance().deleteObserver(this);
        this.unregisterReceiver(messageReceiver);
        cancelRequests();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelRequests();
    }

    private boolean deviceIsNotSecured() {
        KeyguardManager keyguardManager = (KeyguardManager) AnalyticsApplication.getContext().getSystemService(Context.KEYGUARD_SERVICE);
        boolean keyguardIsSecure = keyguardManager.isKeyguardSecure();

        return !keyguardIsSecure;
    }

    void hitScreenView(String screen) {
        analyticsTracker.setScreenName(screen);
        analyticsTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    void setupOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        this.optionsMenu = menu;
        boolean isLoggedIn = LoginStatus.getInstance().isUserLoggedIn();
        MenuItem notificationIcon = menu.findItem(R.id.menu_notifications);
        notificationIcon.setActionView(R.layout.notification_menu_item);
        notificationIcon.getActionView()
                .setOnClickListener(v ->
                        MenuController.handleMenuClick(notificationIcon.getItemId(),
                                BaseTracsActivity.this));
        if (!isLoggedIn) {
            updateNotificationButtonStatus(false);
            updateSettingsMenuStatus(false);
        }

    }

    @Override
    public void update(Observable loginStatus, Object isLoggedIn) {
        boolean loggedIn = (Boolean) isLoggedIn;
        updateNotificationButtonStatus(loggedIn);
        updateSettingsMenuStatus(loggedIn);
        getBadgeCount(loggedIn);
    }

    private void updateNotificationButtonStatus(boolean loggedIn) {
        if (this.optionsMenu != null) {
            MenuItem notification = this.optionsMenu.findItem(R.id.menu_notifications);
            notification.getActionView().setEnabled(loggedIn);
            notification.getActionView().setAlpha(loggedIn ? 1.0f : 0.5f);
        }
    }

    private void updateSettingsMenuStatus(boolean loggedIn) {
        if (this.optionsMenu != null) {
            MenuItem settingsMenu = this.optionsMenu.findItem(R.id.menu_notification_settings);
            settingsMenu.setEnabled(loggedIn);
        }
    }

    private void getBadgeCount(boolean loggedIn) {
        if (!loggedIn) {
             setBadgeCount(0);
             return;
        }
        String url = getString(R.string.dispatch_base) + getString(R.string.dispatch_notifications)
                + "?token=" + FirebaseInstanceId.getInstance().getToken();
        NotificationCountRequest badgeCount = new NotificationCountRequest(BaseTracsActivity.this::badgeCountResponse, BaseTracsActivity.this::badgeCountError
        );
        HttpQueue.getInstance(this).addToRequestQueue(badgeCount, null);
    }

    private void badgeCountResponse(Integer badgeCount) {
        setBadgeCount(badgeCount);
    }

    @SuppressWarnings("unused")
    private void badgeCountError(VolleyError error) {
        setBadgeCount(0);
    }

    void setBadgeCount(int count) {
        String badgeCount = String.valueOf(count);
        if (this.optionsMenu != null) {
            View menuItem = this.optionsMenu.findItem(R.id.menu_notifications).getActionView();
            TextView badge = menuItem.findViewById(R.id.notification_badge);
            badge.setText(badgeCount);
            if (count == 0) {
                badge.setVisibility(View.INVISIBLE);
            } else {
                badge.setVisibility(View.VISIBLE);
            }
        }
    }

    int getCurrentBadgeCount() {
        int badgeCount = 0;
        if (this.optionsMenu != null) {
            View menuItem = this.optionsMenu.findItem(R.id.menu_notifications).getActionView();
            TextView badge = menuItem.findViewById(R.id.notification_badge);
            try {
                badgeCount = Integer.valueOf(badge.getText().toString());
            } catch (Exception e) {
                Log.e("BaseActivity", "Badge count could not be parsed.");
            }
        }
        return badgeCount;
    }

    private void cancelRequests() {
        HttpQueue.getInstance(this).getRequestQueue().cancelAll(this);
    }
}
