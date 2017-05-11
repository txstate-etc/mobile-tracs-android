package edu.txstate.mobile.tracs;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.JsonObject;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import edu.txstate.mobile.tracs.adapters.SettingsAdapter;
import edu.txstate.mobile.tracs.util.AppStorage;
import edu.txstate.mobile.tracs.util.LoginStatus;
import edu.txstate.mobile.tracs.util.SettingsStore;
import edu.txstate.mobile.tracs.util.http.HttpQueue;
import edu.txstate.mobile.tracs.util.http.SettingsRequest;
import edu.txstate.mobile.tracs.util.http.requests.TracsSiteRequest;
import edu.txstate.mobile.tracs.util.http.requests.UserSitesRequest;

public class SettingsActivity extends AppCompatActivity implements Observer {

    private static final String TAG = "SettingsActivity";
    private static final String SCREEN_NAME = "SettingsStore";
    private int expectedSites, retrievedSites;
    private LinkedHashMap<String, String> siteNames;
    private SettingsAdapter adapter;
    private Tracker analyticsTracker;

    private Menu optionsMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        SettingsStore.getInstance().putFromString(AppStorage.get(AppStorage.SETTINGS, AnalyticsApplication.getContext()));

        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        LoginStatus.getInstance().addObserver(this);
        analyticsTracker = AnalyticsApplication.class.cast(getApplication()).getDefaultTracker();


        analyticsTracker.setScreenName(SCREEN_NAME);
        analyticsTracker.send(new HitBuilders.ScreenViewBuilder().build());
        HttpQueue.getInstance(AnalyticsApplication.getContext()).addToRequestQueue(
                new UserSitesRequest(SettingsActivity.this::onSiteIdResponse,
                        SettingsActivity.this::onSiteIdError), TAG
        );
    }

    @Override
    protected void onPause() {
        super.onPause();
        LoginStatus.getInstance().deleteObserver(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.findItem(R.id.notifications_menu).setIcon(
                new IconDrawable(this, FontAwesomeIcons.fa_bell_o)
                        .colorRes(R.color.colorAccent)
                        .actionBarSize()
        ).setEnabled(LoginStatus.getInstance().isUserLoggedIn());

        menu.findItem(R.id.menu_refresh).setVisible(false);
        optionsMenu = menu;
        return true;
    }

    private void onSiteIdResponse(LinkedHashMap<String, String> siteIds) {
        this.siteNames = siteIds;
        this.expectedSites = siteIds.size();
        Iterator iterator = siteNames.entrySet().iterator();
        Map<String, String> headers = new HashMap<>();
        while (iterator.hasNext()) {
            Map.Entry pair = (Map.Entry) iterator.next();
            HttpQueue queue = HttpQueue.getInstance(AnalyticsApplication.getContext());
            TracsSiteRequest siteNameRequest = new TracsSiteRequest(
                    pair.getKey().toString(), headers,
                    SettingsActivity.this::onSiteNameResponse
            );
            queue.addToRequestQueue(siteNameRequest, TAG);
        }
    }

    private void onSiteIdError(VolleyError error) {
        Log.wtf(TAG, new String(error.networkResponse.data));
    }

    private void onSiteNameResponse(JsonObject siteInfo) {
        String key = siteInfo.get("entityId").getAsString();
        String name = siteInfo.get("entityTitle").getAsString();

        if (key != null && name != null) {
            this.siteNames.put(key, name);
            this.retrievedSites++;
        }
        if (this.retrievedSites >= this.expectedSites) {
            this.retrievedSites = 0;
            displayListView();
        }
    }

    private void displayListView() {
        findViewById(R.id.loading_spinner).setVisibility(View.GONE);
        ListView settingsListView = (ListView) findViewById(R.id.settings_list);
        adapter = new SettingsAdapter(this.siteNames, this);
        settingsListView.setAdapter(adapter);
        int size = settingsListView.getCount();
        for (int i = 0; i < size; i++) {

        }
    }

    @Override
    public void update(Observable o, Object arg) {
        if (LoginStatus.getInstance().isUserLoggedIn()) {
            optionsMenu.getItem(R.id.notifications_menu).setEnabled(false);
        }
    }

    public static void saveSettings() {
        Context context = AnalyticsApplication.getContext();
        String settingsUrl = context.getString(R.string.dispatch_base) +
                             context.getString(R.string.dispatch_settings);
        HttpQueue.getInstance(AnalyticsApplication.getContext()).addToRequestQueue(
                new SettingsRequest(settingsUrl,
                        response -> Toast.makeText(context, "Settings updated", Toast.LENGTH_SHORT).show()), TAG);
    }
}