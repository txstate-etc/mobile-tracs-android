package edu.txstate.mobile.tracs.notifications.util

class SiteSet : HashSet<NotificationData>() {
    fun get(siteId: String): NotificationData? {
        this.filter { it.siteId == siteId }
            .forEach { return it }
        return null
    }

    fun isComplete(): Boolean {
        var setComplete = true
        this.forEach {
            if (!it.isComplete()) setComplete = false
        }
        return setComplete
    }

    fun contains(siteId: String): Boolean {
        this.filter { it.siteId == siteId }
            .forEach { return true }
        return false
    }
}