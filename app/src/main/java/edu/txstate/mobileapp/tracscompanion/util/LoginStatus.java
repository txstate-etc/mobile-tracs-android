package edu.txstate.mobileapp.tracscompanion.util;


import java.util.Observable;
import java.util.Observer;

/**
 * Really basic helper class to track whether the user is logged in
 * in one spot.
 */
public class LoginStatus extends Observable {
    private static boolean userIsLoggedIn;
    private static LoginStatus loginStatus;

    private LoginStatus() {}

    public static LoginStatus getInstance() {
        if (loginStatus == null) {
            loginStatus = new LoginStatus();
        }
        return loginStatus;
    }

    public boolean isUserLoggedIn() {
        return userIsLoggedIn;
    }


    public void login() {
        if (!userIsLoggedIn) {
            userIsLoggedIn = true;
            updateObservers();
        }
    }

    public void logout() {
        if (userIsLoggedIn) {
            userIsLoggedIn = false;
            updateObservers();
        }
    }

    private void updateObservers() {
        loginStatus.setChanged();
        loginStatus.notifyObservers(userIsLoggedIn);
        loginStatus.clearChanged();
    }
}
