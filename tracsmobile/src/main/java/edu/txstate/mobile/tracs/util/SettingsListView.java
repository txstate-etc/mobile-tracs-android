package edu.txstate.mobile.tracs.util;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ExpandableListView;

public class SettingsListView extends ExpandableListView {
    private static final String TAG = "SettingsListView";

    public SettingsListView(Context context) {
        super(context);
    }

    public SettingsListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SettingsListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
