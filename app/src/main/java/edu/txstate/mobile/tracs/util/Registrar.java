package edu.txstate.mobile.tracs.util;


import android.content.Context;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import edu.txstate.mobile.tracs.AnalyticsApplication;
import edu.txstate.mobile.tracs.BuildConfig;
import edu.txstate.mobile.tracs.util.http.HttpQueue;
import edu.txstate.mobile.tracs.util.http.requests.DispatchRegistrationRequest;
import edu.txstate.mobile.tracs.util.http.requests.JwtRequest;

public class Registrar {

    private Map<String, String> registration = new HashMap<>();
    private String jwt;

    private static final String tokenUrl = "https://dispatchqa1.its.qual.txstate.edu:3000/token.pl";
    private static final String dispatchUrl = "https://dispatchqa1.its.qual.txstate.edu/registrations";
    private static final String TAG = "Registrar";
    private static Registrar registrar;

    private Registrar() {
        init();
    }

    private void init() {
        Context context = AnalyticsApplication.getContext();
        registration.put("token", FirebaseInstanceId.getInstance().getToken());
        registration.put("platform", "android");
        registration.put("app_id", BuildConfig.APPLICATION_ID);
        registration.put("user_id", AppStorage.get(AppStorage.USERNAME, context));
    }

    public static Registrar getInstance() {
        if (registrar == null) {
            registrar = new Registrar();
        }
        return registrar;
    }

    private JSONObject getJsonRegistration() {
        JSONObject regInfo = new JSONObject();
        if (registrar == null) {
            return regInfo;
        }
        regInfo = new JSONObject(registration);
        return regInfo;
    }

    public void registerDevice() {
        HttpQueue requestQueue = HttpQueue.getInstance(AnalyticsApplication.getContext());
        Map<String, String> headers = new HashMap<>();
        requestQueue.addToRequestQueue(new JwtRequest(
                tokenUrl,
                headers,
                Registrar.getInstance()::receiveJwt,
                error -> Log.wtf(TAG, error.getMessage())
        ), TAG);
    }

    private void receiveJwt(String jwt) {
        String url = dispatchUrl + "?jwt=" + jwt;
        HttpQueue requestQueue = HttpQueue.getInstance(AnalyticsApplication.getContext());
        JSONObject regInfo = Registrar.getInstance().getJsonRegistration();
        DispatchRegistrationRequest registerRequest = new DispatchRegistrationRequest(url, regInfo);
        requestQueue.addToRequestQueue(registerRequest, TAG);
    }
}
