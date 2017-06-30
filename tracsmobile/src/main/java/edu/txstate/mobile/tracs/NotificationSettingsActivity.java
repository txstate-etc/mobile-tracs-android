package edu.txstate.mobile.tracs;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import edu.txstate.mobile.tracs.adapters.SettingsAdapter;
import edu.txstate.mobile.tracs.util.AppStorage;
import edu.txstate.mobile.tracs.util.LoginStatus;
import edu.txstate.mobile.tracs.util.SettingsStore;
import edu.txstate.mobile.tracs.util.TracsClient;
import edu.txstate.mobile.tracs.util.http.HttpQueue;
import edu.txstate.mobile.tracs.util.http.requests.TracsSiteRequest;
import edu.txstate.mobile.tracs.util.http.requests.UserSitesRequest;

public class NotificationSettingsActivity extends BaseTracsActivity {

    private static final String TAG = "NotificationSettingsActivity";
    private static final String SCREEN_NAME = "Settings";
    private int expectedSites, retrievedSites;
    private LinkedHashMap<String, String> siteNames;
    private SettingsAdapter adapter;
    private long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_notification_settings);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        super.hitScreenView(SCREEN_NAME);
        TracsClient.getInstance().verifySession(NotificationSettingsActivity.this::onSessionVerified);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LoginStatus.getInstance().deleteObserver(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.setupOptionsMenu(menu);
        super.optionsMenu.findItem(R.id.menu_notification_settings).setEnabled(false);
        return true;
    }

    private void onSessionVerified(String session) {
        if (session != null) {
            AppStorage.put(AppStorage.SESSION_ID, session, AnalyticsApplication.getContext());
            startTime = System.nanoTime();
            HttpQueue.getInstance(AnalyticsApplication.getContext()).addToRequestQueue(
                    new UserSitesRequest(NotificationSettingsActivity.this::onSiteIdResponse,
                            NotificationSettingsActivity.this::onSiteIdError), this
            );
        } else {
            findViewById(R.id.loading_spinner).setVisibility(View.GONE);
            Toast errorToast = Toast.makeText(this, "Error loading settings.", Toast.LENGTH_LONG);
            errorToast.show();
        }
    }

    private void onSiteIdResponse(LinkedHashMap<String, String> siteIds) {
        this.siteNames = siteIds;
        this.expectedSites = siteIds.size();
        for (Map.Entry pair : siteNames.entrySet()) {
            HttpQueue queue = HttpQueue.getInstance(AnalyticsApplication.getContext());
            TracsSiteRequest siteNameRequest = new TracsSiteRequest(
                    pair.getKey().toString(),
                    this::onSiteNameResponse,
                    this::onSiteNameError
            );
            queue.addToRequestQueue(siteNameRequest, this);
        }
    }

    @SuppressLint("LongLogTag")
    private void onSiteIdError(VolleyError error) {
        Log.e(TAG, "Could not retrieve site id");
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

    @SuppressLint("LongLogTag")
    private void onSiteNameError(String siteId) {
        Log.e(TAG, "Could not get site name for: " + siteId);
    }

    @SuppressLint("LongLogTag")
    private void displayListView() {
        findViewById(R.id.loading_spinner).setVisibility(View.GONE);
        Log.i(TAG, "Load Time: " + (System.nanoTime() - startTime) / 1_000_000 + "ms");
        ExpandableListView settingsListView = (ExpandableListView) findViewById(R.id.settings_list);
        adapter = new SettingsAdapter(this, this.siteNames);
        settingsListView.setAdapter(adapter);
        for (int groupPosition = 0; groupPosition < adapter.getGroupCount(); groupPosition++) {
            settingsListView.expandGroup(groupPosition);
        }
        saveSettings();
    }

    public static void saveSettings() {
        SettingsStore.getInstance().saveSettings();
    }
}