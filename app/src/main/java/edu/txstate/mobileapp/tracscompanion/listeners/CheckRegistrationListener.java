package edu.txstate.mobileapp.tracscompanion.listeners;

public interface CheckRegistrationListener extends RequestListener {
    void onRequestReturned(boolean deviceIsRegistered);
}
