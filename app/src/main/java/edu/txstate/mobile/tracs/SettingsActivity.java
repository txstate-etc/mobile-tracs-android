package edu.txstate.mobile.tracs;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;

import com.android.volley.VolleyError;
import com.google.gson.JsonObject;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import edu.txstate.mobile.tracs.adapters.NotificationsAdapter;
import edu.txstate.mobile.tracs.adapters.SettingsAdapter;
import edu.txstate.mobile.tracs.util.LoginStatus;
import edu.txstate.mobile.tracs.util.async.StatusUpdate;
import edu.txstate.mobile.tracs.util.http.HttpQueue;
import edu.txstate.mobile.tracs.util.http.requests.TracsSiteRequest;
import edu.txstate.mobile.tracs.util.http.requests.UserSitesRequest;

public class SettingsActivity extends AppCompatActivity implements Observer {

    private static final String TAG = "SettingsActivity";
    private int expectedSites, retrievedSites;
    private LinkedHashMap<String, String> siteNames;
    private SettingsAdapter adapter;
    private Menu optionsMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        LoginStatus.getInstance().addObserver(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        HttpQueue.getInstance(AnalyticsApplication.getContext()).addToRequestQueue(
                new UserSitesRequest(SettingsActivity.this::onSiteIdResponse,
                        SettingsActivity.this::onSiteIdError), TAG
        );
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
        final ListView settingsList = (ListView) findViewById(R.id.settings_list);
        adapter = new SettingsAdapter(this.siteNames, this.getApplicationContext());
        settingsList.setAdapter(adapter);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (LoginStatus.getInstance().isUserLoggedIn()) {
            optionsMenu.getItem(R.id.notifications_menu).setEnabled(false);
        }
    }
}