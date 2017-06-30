package edu.txstate.mobile.tracs.util.http.listeners

import edu.txstate.mobile.tracs.notifications.util.SiteSet

interface NotificationDataListener {
    fun onResponse(data: SiteSet)
}