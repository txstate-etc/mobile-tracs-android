package edu.txstate.mobile.tracs;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.VolleyError;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.JsonObject;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Observable;
import java.util.Set;

import edu.txstate.mobile.tracs.adapters.NotificationsRVAdapter;
import edu.txstate.mobile.tracs.notifications.DispatchNotification;
import edu.txstate.mobile.tracs.notifications.NotificationTypes;
import edu.txstate.mobile.tracs.notifications.NotificationsBundle;
import edu.txstate.mobile.tracs.notifications.TracsAppNotification;
import edu.txstate.mobile.tracs.notifications.tracs.TracsNotification;
import edu.txstate.mobile.tracs.notifications.util.NotificationData;
import edu.txstate.mobile.tracs.notifications.util.SiteSet;
import edu.txstate.mobile.tracs.util.IntegrationServer;
import edu.txstate.mobile.tracs.util.SwipeUtil;
import edu.txstate.mobile.tracs.util.TracsClient;
import edu.txstate.mobile.tracs.util.async.StatusUpdate;
import edu.txstate.mobile.tracs.util.http.HttpQueue;
import edu.txstate.mobile.tracs.util.http.requests.TracsPageIdRequest;
import edu.txstate.mobile.tracs.util.http.requests.TracsSiteRequest;

public class NotificationsActivity extends BaseTracsActivity {
    public static final String TAG = "NotificationsActivity";
    private static final String SCREEN_NAME = "Notifications";
    private NotificationsBundle tracsNotifications;
    private NotificationsBundle dispatchNotifications = new NotificationsBundle();
    private NotificationsRVAdapter adapter;
    private RecyclerView notificationsList;
    private BroadcastReceiver messageReceiver;
    private long startTime;
    private SiteSet siteIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_notifications);
        notificationsList = (RecyclerView) findViewById(R.id.rv_notifications);
        notificationsList.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
        super.onCreate(savedInstanceState);
        messageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                HttpQueue.getInstance(NotificationsActivity.this).getRequestQueue().cancelAll(NotificationsActivity.this);
                NotificationsActivity.this.clearNotifications();
                refreshNotifications();
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        clearNotifications();
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        super.hitScreenView(SCREEN_NAME);
        this.registerReceiver(messageReceiver, new IntentFilter("badge_count"));
    }

    private void init() {
        refreshNotifications();
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(messageReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.setupOptionsMenu(menu);
        MenuItem notifications = super.optionsMenu.findItem(R.id.menu_notifications);
        notifications.setVisible(false);
        MenuItem clearAll = super.optionsMenu.findItem(R.id.clear_all);
        clearAll.setIcon(
                new IconDrawable(this, FontAwesomeIcons.fa_times_circle_o)
                        .colorRes(R.color.colorHeaderIcons)
                        .actionBarSize()
        );
        clearAll.setOnMenuItemClickListener(
                item -> {
                    NotificationsActivity.this.promptClearAll();
                    return false;
                }
        );
        clearAll.setVisible(false);
        return true;
    }

    private void clearNotifications() {
        if (this.dispatchNotifications != null) {
            this.dispatchNotifications.removeAll();
        }
        if (this.tracsNotifications != null) {
            this.tracsNotifications.removeAll();
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void refreshNotifications() {
        startTime = System.nanoTime();
        if (this.tracsNotifications != null ){
            this.tracsNotifications.deleteObservers();
        }
        this.tracsNotifications = new NotificationsBundle();
        this.tracsNotifications.addObserver(this);
        IntegrationServer.getInstance()
                .getDispatchNotifications(NotificationsActivity.this::onDispatchResponse);
    }

    private void promptClearAll() {
        AlertDialog clearPrompt = new ClearAllDialog(this, this::clearAll);
        clearPrompt.show();
    }

    private void clearAll(View view) {
        this.notificationsList.setAdapter(null);
        this.tracsNotifications.removeAll();
        this.dispatchNotifications.removeAll();
        Log.i(TAG, "Notifications have been cleared");
    }

    private void onDispatchResponse(NotificationsBundle response) {
        if (response.size() == 0) {
            displayListView();
            return;
        }
        this.dispatchNotifications = response;
        this.siteIds = extractSiteIds(response);
        TracsClient tracs = TracsClient.getInstance();
        tracs.getSiteData(this.siteIds, this::onSiteDataResponse);
    }

    private void onSiteDataResponse(SiteSet data) {
        if (data.size() == 0) {
            displayListView();
        }
        this.siteIds = data;
        ArrayList<DispatchNotification> toBeRemoved = new ArrayList<>();
        for (TracsAppNotification notification: this.dispatchNotifications) {
            DispatchNotification dNotification = (DispatchNotification) notification;
            if (!this.siteIds.contains(dNotification.getSiteId())) {
                toBeRemoved.add(dNotification);
            }
        }
        for (DispatchNotification d : toBeRemoved) {
            this.dispatchNotifications.remove(d);
        }
        TracsClient tracs = TracsClient.getInstance();
        tracs.getNotifications(this.dispatchNotifications, NotificationsActivity.this::onTracsResponse, AnalyticsApplication.getContext());
    }

    private void onTracsResponse(TracsNotification response) {
        if (response.getType().equals(NotificationTypes.ERROR)) {
            Log.e(TAG, "Error retrieving notifications from TRACS");
            this.dispatchNotifications.remove(response.getDispatchId());
            if (this.dispatchNotifications.size() == 0) {
                displayListView();
            }
        } else {
            this.tracsNotifications.add(response);
        }
    }

    @Override
    public void update(Observable tracsNotifications, Object newNotification) {
        TracsNotification notification;
        try {
            notification = (TracsNotification) newNotification;
            NotificationData siteData = this.siteIds.get(notification.getSiteId());
            if (siteData != null) {
                notification.setSiteName(siteData.getSiteName());
                switch (notification.getType()) {
                    case NotificationTypes.ANNOUNCEMENT:
                        notification.setPageId(siteData.getAnnouncementPageId());
                        break;
                    case NotificationTypes.DISCUSSION:
                        notification.setPageId(siteData.getDiscussionPageId());
                        break;
                    default:
                        break;
                }
            }
        } catch (ClassCastException e) {
            Log.e(TAG, e.getMessage());
        }
        if (allRequestsAreBack()) {
            displayListView();
        }
    }

    private boolean allRequestsAreBack() {
        if (this.dispatchNotifications.size() > this.tracsNotifications.size()) { return false; }
        int totalSiteNames = 0;
        int totalPageIds = 0;
        for (TracsAppNotification notification : this.tracsNotifications) {
            TracsNotification tracsNote = TracsNotification.class.cast(notification);
            if (tracsNote.hasSiteName()) { totalSiteNames++; }
            if (tracsNote.hasPageId()) { totalPageIds++; }
        }
        boolean siteNamesDone = totalSiteNames == tracsNotifications.size();
        boolean pageIdsDone = totalPageIds == tracsNotifications.size();
        return siteNamesDone && pageIdsDone;
    }

    private void displayListView() {
        findViewById(R.id.loading_spinner).setVisibility(View.GONE);
        adapter = new NotificationsRVAdapter(tracsNotifications, this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        notificationsList.setLayoutManager(linearLayoutManager);
        this.notificationsList.setAdapter(adapter);

        setSwipeForRecyclerView();
        StatusUpdate.updateSeen(this.tracsNotifications);
        long loadTime = (System.nanoTime() - this.startTime) / 1_000_000;
        String notificationsLoaded = String.valueOf(this.notificationsList.getAdapter().getItemCount());
        Tracker tracker = AnalyticsApplication.getDefaultTracker();
        Log.i(TAG, "Notification Retrieval Time: " + loadTime + "ms");
        tracker.send(new HitBuilders.TimingBuilder()
                .setCategory(getResources().getString(R.string.notification_event))
                .setValue(loadTime)
                .setVariable("notifications")
                .setLabel(notificationsLoaded)
                .build()
        );
    }

    private void setSwipeForRecyclerView() {
        SwipeUtil swipeHelper = new SwipeUtil(0, ItemTouchHelper.LEFT, this) {

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int i) {
                int swipedPosition = viewHolder.getAdapterPosition();
                ((NotificationsRVAdapter) notificationsList.getAdapter()).remove(swipedPosition);
            }

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                return super.getSwipeDirs(recyclerView, viewHolder);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeHelper);
        itemTouchHelper.attachToRecyclerView(notificationsList);

        swipeHelper.setLeftSwipeLabel("Delete");
        swipeHelper.setLeftColorCode(getResources().getColor(R.color.dismissBackground));
    }

    private SiteSet extractSiteIds(NotificationsBundle bundle) {
        SiteSet siteIds = new SiteSet();
        for(TracsAppNotification notification : bundle) {
            DispatchNotification dNotification = (DispatchNotification) notification;
            siteIds.add(new NotificationData(dNotification.getSiteId()));
        }
        return siteIds;
    }
}
