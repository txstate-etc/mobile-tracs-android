package edu.txstate.mobile.tracs.util

import java.util.Observable

class LoginStatus : Observable() {
    companion object {
        var userIsLoggedIn: Boolean = false
        @JvmStatic
        val instance: LoginStatus = LoginStatus()
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

