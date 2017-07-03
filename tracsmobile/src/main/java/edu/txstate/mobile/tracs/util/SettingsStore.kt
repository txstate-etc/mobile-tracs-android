package edu.txstate.mobile.tracs.util

import android.util.Log
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import edu.txstate.mobile.tracs.AnalyticsApplication
import edu.txstate.mobile.tracs.R
import edu.txstate.mobile.tracs.notifications.NotificationTypes
import edu.txstate.mobile.tracs.util.http.HttpQueue
import edu.txstate.mobile.tracs.util.http.requests.SettingsRequest

class SettingsStore private constructor() {
    companion object {
        private val TAG = "SettingsStore"
        private val SETTING_ENABLED = true
        private val SETTING_DISABLED = false
        private val SETTINGS_NOT_IMPLEMENTED = arrayOf(
                NotificationTypes.ASSESSMENT,
                NotificationTypes.GRADE,
                NotificationTypes.DISCUSSION,
                NotificationTypes.ASSIGNMENT
        )
        var instance: SettingsStore = SettingsStore()
            @JvmStatic
            get() {
                field.getSettingsFromStorage()
                field.setDefaultSettings()
                return field
            }
    }

    private var settings = JsonObject()

    constructor(settings: JsonObject): this() {
        this.settings = settings
    }

    fun put(settingId: String, isEnabled: Boolean) {
        this.settings.addProperty(settingId, isEnabled)
        AppStorage.put(AppStorage.SETTINGS, this.settings.toString(), AnalyticsApplication.getContext())
    }

    fun get(settingId: String): Boolean {
        if (this.settings.has(settingId)) {
            return this.settings.get(settingId).asBoolean
        }
        return SETTING_ENABLED
    }

    private fun setDefaultSettings() {
        SETTINGS_NOT_IMPLEMENTED.map { it -> put(it, SETTING_DISABLED) }
    }

    private fun getSettingsFromStorage() {
        val settings: String? = AppStorage.get(AppStorage.SETTINGS, AnalyticsApplication.getContext())
        if (settings.isNullOrEmpty()) {
            return
        }
        val parser = JsonParser()
        this.settings = parser.parse(settings).asJsonObject
    }

    fun saveSettings() {
        val context = AnalyticsApplication.getContext()
        AppStorage.put(AppStorage.SETTINGS, SettingsStore.instance.toString(), context)
        val settingsUrl = context.resources.getString(R.string.dispatch_base) +
                context.resources.getString(R.string.dispatch_settings)
        HttpQueue.getInstance(context).addToRequestQueue(
                SettingsRequest(settingsUrl, { _ -> Log.i(TAG, "Settings saved") }),
                null)
    }

    fun getSettings(): JsonObject {
        return this.settings
    }

    fun clear() {
        instance = SettingsStore(JsonObject())
    }

    override fun toString(): String {
        return this.settings.toString()
    }
}