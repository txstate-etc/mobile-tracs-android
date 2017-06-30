package edu.txstate.mobile.tracs.notifications.util

class NotificationData(var siteId: String) {
    var siteName: String? = ""
        set(value) {
            field = value
            siteNameChanged = true
        }
    var announcementPageId: String? = ""
        set(value) {
            field = value
            announcementPageIdChanged = true
        }
    var discussionPageId: String? = ""
        set(value) {
            field = value
            discussionPageIdChanged = true
        }

    private var siteNameChanged = false
    private var announcementPageIdChanged = false
    private var discussionPageIdChanged = false

    override fun equals(other: Any?): Boolean {
        return other is NotificationData && other.siteId == this.siteId
    }

    override fun hashCode(): Int {
        return this.siteId.hashCode()
    }

    override fun toString(): String {
        return this.siteName ?: ""
    }

    fun hasSiteName(): Boolean {
        return this.siteNameChanged
    }

    fun hasAnnouncementPageId(): Boolean {
        return this.announcementPageIdChanged
    }

    fun hasDiscussionPageId(): Boolean {
        return this.discussionPageIdChanged
    }

    fun isComplete(): Boolean {
        return hasSiteName() && hasAnnouncementPageId() && hasDiscussionPageId()
    }
}