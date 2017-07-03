package edu.txstate.mobile.tracs.util

class NotificationStatus() {
    private var seen: Boolean = false
    private var read: Boolean = false
    private var cleared: Boolean = false

    constructor(seen: Boolean, read: Boolean, cleared: Boolean): this() {
        this.seen = seen
        this.read = read
        this.cleared = cleared
    }

    fun hasBeenSeen(): Boolean {
        return this.seen
    }

    fun hasBeenRead(): Boolean {
        return this.read
    }

    fun hasBeenCleared(): Boolean {
        return this.cleared
    }
}