package edu.txstate.mobile.tracs.util

import java.util.Observable

class LoginStatus : Observable() {
    companion object {
        @JvmStatic
        var userIsLoggedIn: Boolean = false
        val loginStatus: LoginStatus = LoginStatus()
        @JvmStatic
        val instance: LoginStatus = Companion.loginStatus
    }


    fun isUserLoggedIn() : Boolean {
        return userIsLoggedIn
    }

    fun login() {
        if (!isUserLoggedIn()) {
            userIsLoggedIn = true
            updateObservers()
        }
    }

    fun logout() {
        if (isUserLoggedIn()) {
            userIsLoggedIn = false
            updateObservers()
        }
    }

    private fun updateObservers() {
        instance.setChanged()
        instance.notifyObservers(isUserLoggedIn())
        instance.clearChanged()
    }
}

