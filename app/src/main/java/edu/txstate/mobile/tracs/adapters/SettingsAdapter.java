package edu.txstate.mobile.tracs.adapters;

import android.content.Context;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import edu.txstate.mobile.tracs.R;

public class SettingsAdapter extends BaseAdapter {

    private static final String TAG = "SettingsAdapter";

    private Context context;
    private ArrayList<Pair<String, String>> settings;

    public SettingsAdapter(LinkedHashMap<String, String> settings, Context context) {
        this.context = context;
        this.settings = new ArrayList<>();
        for (Map.Entry namePair : settings.entrySet()) {
            Pair<String, String> settingPair = new Pair<>(namePair.getKey().toString(), namePair.getValue().toString());
            this.settings.add(settingPair);
        }
        Pair<String, String> settingPair = new Pair<>("announcements", "Announcements");
        this.settings.add(settingPair);
    }

    @Override
    public int getCount() {
        return settings.size();
    }

    @Override
    public Object getItem(int position) {
        return settings.get(position);
    }

    @Override
    public long getItemId(int position) {
        return settings.get(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.settings_row, parent, false);
        }
        SettingsAdapter.RowHolder rowHolder = new SettingsAdapter.RowHolder();
        Pair<String, String> setting = (Pair<String, String>) getItem(position);

        rowHolder.settingStatus = (Switch) convertView.findViewById(R.id.setting_toggle);
        rowHolder.settingId = setting.first;

        rowHolder.settingStatus.setText(setting.second);
        rowHolder.settingStatus.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 28);
        rowHolder.settingStatus.setOnCheckedChangeListener(this::onSettingChanged);

        convertView.setTag(rowHolder);

        return convertView;
    }

    private void onSettingChanged(CompoundButton button, boolean isChecked) {
        String checkedStatus = isChecked ? "on" : "off";
        Log.i(TAG, button.getText() + " is " + checkedStatus);
    }

    static class RowHolder {
        Switch settingStatus;
        String settingId;
    }
}
