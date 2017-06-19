package edu.txstate.mobile.tracs.util;


import android.content.Context;
import android.util.Log;

import com.android.volley.Response;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import edu.txstate.mobile.tracs.AnalyticsApplication;
import edu.txstate.mobile.tracs.BuildConfig;
import edu.txstate.mobile.tracs.R;
import edu.txstate.mobile.tracs.util.http.HttpQueue;
import edu.txstate.mobile.tracs.util.http.listeners.RegisterCallback;
import edu.txstate.mobile.tracs.util.http.requests.DispatchRegistrationRequest;
import edu.txstate.mobile.tracs.util.http.requests.DispatchUnregisterRequest;
import edu.txstate.mobile.tracs.util.http.requests.JwtRequest;

public class Registrar {

    private Map<String, String> registration = new HashMap<>();
    private String jwt;
    private RegisterCallback registerCallback;
    private Response.ErrorListener errorListener;


    private static final String TOKEN_URL = AnalyticsApplication.getContext().getString(R.string.jwt_url);
    private static final String DISPATCH_URL = AnalyticsApplication.getContext().getString(R.string.dispatch_registration);
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
        Registrar.getInstance().init(); //In case the data is out of date or not populated
        return new JSONObject(registration);
    }

    public void registerDevice(RegisterCallback registerCallback, Response.ErrorListener errorListener) {
        this.registerCallback = registerCallback;
        this.errorListener = errorListener;
        HttpQueue requestQueue = HttpQueue.getInstance(AnalyticsApplication.getContext());
        JwtRequest jwtRequest = new JwtRequest(
                TOKEN_URL,
                Registrar.getInstance()::receiveJwt,
                error -> Log.e(TAG, "Could not retrieve JWT from service.")
        );
        requestQueue.addToRequestQueue(jwtRequest, this);
    }

    public void unregisterDevice() {
        HttpQueue requestQueue = HttpQueue.getInstance(AnalyticsApplication.getContext());
        String token = FirebaseInstanceId.getInstance().getToken();
        DispatchUnregisterRequest unregisterRequest = new DispatchUnregisterRequest(DISPATCH_URL, token);
        requestQueue.addToRequestQueue(unregisterRequest, this);
    }

    private void receiveJwt(String jwt) {
        String url = DISPATCH_URL + "?jwt=" + jwt;
        HttpQueue requestQueue = HttpQueue.getInstance(AnalyticsApplication.getContext());
        JSONObject regInfo = Registrar.getInstance().getJsonRegistration();
        DispatchRegistrationRequest registerRequest = new DispatchRegistrationRequest(url, regInfo, registerCallback, errorListener);
        requestQueue.addToRequestQueue(registerRequest, this);
    }
}
