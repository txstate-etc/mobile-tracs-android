package edu.txstate.mobile.tracs.util

import android.content.Context
import android.util.AttributeSet
import android.widget.ExpandableListView

class SettingsListView : ExpandableListView {

    companion object {
        val TAG = "SettingsListView"
    }

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr)

}