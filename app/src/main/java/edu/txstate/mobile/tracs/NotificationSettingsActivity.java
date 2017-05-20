package edu.txstate.mobile.tracs;

import android.content.Context;
import android.content.Intent;
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
import java.util.Observable;

import edu.txstate.mobile.tracs.adapters.SettingsAdapter;
import edu.txstate.mobile.tracs.util.AppStorage;
import edu.txstate.mobile.tracs.util.LoginStatus;
import edu.txstate.mobile.tracs.util.SettingsStore;
import edu.txstate.mobile.tracs.util.TracsClient;
import edu.txstate.mobile.tracs.util.http.HttpQueue;
import edu.txstate.mobile.tracs.util.http.SettingsRequest;
import edu.txstate.mobile.tracs.util.http.requests.TracsSiteRequest;
import edu.txstate.mobile.tracs.util.http.requests.UserSitesRequest;

public class NotificationSettingsActivity extends BaseTracsActivity {

    private static final String TAG = "NotificationSettingsActivity";
    private static final String SCREEN_NAME = "SettingsStore";
    private int expectedSites, retrievedSites;
    private LinkedHashMap<String, String> siteNames;
    private SettingsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_notification_settings);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        SettingsStore.getInstance().putFromString(AppStorage.get(AppStorage.SETTINGS, AnalyticsApplication.getContext()));
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
        Iterator iterator = siteNames.entrySet().iterator();
        Map<String, String> headers = new HashMap<>();
        while (iterator.hasNext()) {
            Map.Entry pair = (Map.Entry) iterator.next();
            HttpQueue queue = HttpQueue.getInstance(AnalyticsApplication.getContext());
            TracsSiteRequest siteNameRequest = new TracsSiteRequest(
                    pair.getKey().toString(), headers,
                    NotificationSettingsActivity.this::onSiteNameResponse
            );
            queue.addToRequestQueue(siteNameRequest, this);
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
        ExpandableListView settingsListView = (ExpandableListView) findViewById(R.id.settings_list);
        adapter = new SettingsAdapter(this, this.siteNames);
        settingsListView.setAdapter(adapter);
        for (int groupPosition = 0; groupPosition < adapter.getGroupCount(); groupPosition++) {
            settingsListView.expandGroup(groupPosition);
        }
    }

    public static void saveSettings() {
        Context context = AnalyticsApplication.getContext();
        String settingsUrl = context.getString(R.string.dispatch_base) +
                             context.getString(R.string.dispatch_settings);

        //No tag needed, this request doesn't ever need to be cancelled.
        HttpQueue.getInstance(AnalyticsApplication.getContext()).addToRequestQueue(
                new SettingsRequest(settingsUrl,
                        response -> Toast.makeText(context, "Settings updated", Toast.LENGTH_SHORT).show()), null);
    }
}