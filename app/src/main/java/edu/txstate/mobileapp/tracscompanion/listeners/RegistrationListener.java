package edu.txstate.mobileapp.tracscompanion.listeners;

public interface RegistrationListener extends RequestListener {
    void onRequestReturned(boolean deviceIsRegistered);
}
