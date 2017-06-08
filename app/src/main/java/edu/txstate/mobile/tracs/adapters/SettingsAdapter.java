package edu.txstate.mobile.tracs.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.SwitchCompat;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.txstate.mobile.tracs.AnalyticsApplication;
import edu.txstate.mobile.tracs.NotificationSettingsActivity;
import edu.txstate.mobile.tracs.R;
import edu.txstate.mobile.tracs.notifications.NotificationTypes;
import edu.txstate.mobile.tracs.util.AppStorage;
import edu.txstate.mobile.tracs.util.SettingsStore;

public class SettingsAdapter extends BaseExpandableListAdapter {

    private static final String TAG = "SettingsAdapter";

    private interface Settings {
        String DEFAULT = "Notification Types";
        String SITES = "Sites";
        String PROJECTS = "Projects"; //Future use
    }

    private Context context;
    private List<String> settingHeaders;
    private HashMap<String, ArrayList<Pair<String, String>>> settings;

    public SettingsAdapter(Context context, LinkedHashMap<String, String> settings) {
        this.context = context;
        this.settingHeaders = new LinkedList<>();
        this.settings = new HashMap<>();


        ArrayList<Pair<String, String>> defaultSettings = new ArrayList<>();
        ArrayList<Pair<String, String>> coursesSettings = new ArrayList<>();

        defaultSettings.add(new Pair<>(NotificationTypes.ANNOUNCEMENT, context.getString(R.string.announcement_setting)));
//        defaultSettings.add(new Pair<>(NotificationTypes.DISCUSSION, context.getString(R.string.discussion_setting)));
//        defaultSettings.add(new Pair<>(NotificationTypes.GRADE, context.getString(R.string.grade_setting)));
//        defaultSettings.add(new Pair<>(NotificationTypes.ASSESSMENT, context.getString(R.string.assessment_setting)));
//        defaultSettings.add(new Pair<>(NotificationTypes.ASSIGNMENT, context.getString(R.string.assignment_setting)));

        Collections.sort(defaultSettings, (o1, o2) -> {
            int order = String.CASE_INSENSITIVE_ORDER.compare(o1.second, o2.second);
            if (order == 0) {
                order = o1.second.compareTo(o2.second);
            }
            return order;
        });

        for (Map.Entry namePair : settings.entrySet()) {
            Pair<String, String> settingPair = new Pair<>(namePair.getKey().toString(), namePair.getValue().toString());
            coursesSettings.add(settingPair);
        }

        settingHeaders.add(Settings.DEFAULT);
        settingHeaders.add(Settings.SITES);

        this.settings.put(Settings.DEFAULT, defaultSettings);
        this.settings.put(Settings.SITES, coursesSettings);
    }



    @Override
    public int getGroupCount() {
        return this.settingHeaders.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        String groupName = (String) getGroup(groupPosition);
        return this.settings.get(groupName).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.settingHeaders.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        String groupName = (String) getGroup(groupPosition);
        return this.settings.get(groupName).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return getGroup(groupPosition).hashCode();
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        String groupName = (String) getGroup(groupPosition);
        return this.settings.get(groupName).get(childPosition).hashCode();
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
            convertView = inflater.inflate(R.layout.notification_settings_header, parent, false);
        }

        TextView settingsHeader = (TextView) convertView.findViewById(R.id.settings_header);
        settingsHeader.setTypeface(null, Typeface.BOLD);
        settingsHeader.setText((String) getGroup(groupPosition));

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
            convertView = inflater.inflate(R.layout.notification_settings_row, parent, false);
        }

        RowHolder rowHolder = new RowHolder();

        Pair<String, String> setting = (Pair<String, String>) getChild(groupPosition, childPosition);
        rowHolder.settingStatus = (SwitchCompat) convertView.findViewById(R.id.setting_toggle);
        rowHolder.settingStatus.setChecked(SettingsStore.getInstance().get(setting.first));
        rowHolder.settingId = setting.first;

        rowHolder.settingStatus.setText(setting.second);
        rowHolder.settingStatus.setOnClickListener(this::onClicked);

        convertView.setTag(rowHolder);
        SettingsStore.getInstance().put(rowHolder.settingId, rowHolder.settingStatus.isChecked());

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    static class RowHolder {
        SwitchCompat settingStatus;
        String settingId;
    }

    private void onClicked(View view) {
        View parent = (View) view.getParent();
        RowHolder tag = (RowHolder) parent.getTag();
        SettingsStore.getInstance().put(tag.settingId, tag.settingStatus.isChecked());
        AppStorage.put(AppStorage.SETTINGS, SettingsStore.getInstance().toString(), AnalyticsApplication.getContext());
        NotificationSettingsActivity.saveSettings();
    }
}
