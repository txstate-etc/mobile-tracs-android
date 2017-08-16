package edu.txstate.mobile.tracs.notifications.tracs

import android.util.Log
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import edu.txstate.mobile.tracs.notifications.NotificationTypes
import edu.txstate.mobile.tracs.util.TracsClient

class TracsDiscussion : TracsNotificationAbs {

    var authoredBy: String? = null
    var topicId: Long? = 0
    var messageId: Long? = 0

    companion object {
        val TAG = "TracsDiscussion"
    }

    constructor()

    constructor(rawNotification: JsonObject) {
        super.setTitle(extractKey(rawNotification, "entityTitle") as String)
        authoredBy = extractKey(rawNotification, "authoredBy") as String
        topicId = extractKey(rawNotification, "topicId") as Long
        messageId = extractKey(rawNotification, "messageId") as Long
    }

    override fun getUrl(): String {
        var discussionUrl = TracsClient.makeUrl("SITE")
        if (this.pageId == null) {
            discussionUrl += this.siteId
        } else {
            discussionUrl += "${this.siteId}/tool/${this.pageId}"
        }
        return discussionUrl
    }

    override fun getType(): String {
        return NotificationTypes.DISCUSSION
    }

    fun extractKey(notification: JsonObject?, key: String): Any? {
        if (notification == null) {
            return TracsNotificationAbs.NOT_SET
        }
        val jsonValue: JsonElement? = notification.get(key)

        if (jsonValue == null) {
            Log.e(TAG, "Tried to find key: $key and failed")
            return null
        }

        if (key == "topicId" || key == "messageId") {
            return jsonValue.asString.toLong()
        } else {
            return jsonValue.asString
        }
    }

    override fun hasPageId(): Boolean {
        return TracsNotification.NOT_SET != super.getPageId()
    }
}