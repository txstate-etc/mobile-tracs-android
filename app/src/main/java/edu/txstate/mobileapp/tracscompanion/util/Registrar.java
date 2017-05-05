package edu.txstate.mobileapp.tracscompanion.util;


import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import edu.txstate.mobileapp.tracscompanion.AnalyticsApplication;
import edu.txstate.mobileapp.tracscompanion.BuildConfig;
import edu.txstate.mobileapp.tracscompanion.util.http.HttpQueue;
import edu.txstate.mobileapp.tracscompanion.util.http.requests.DispatchNotificationRequest;
import edu.txstate.mobileapp.tracscompanion.util.http.requests.DispatchRegistrationRequest;
import edu.txstate.mobileapp.tracscompanion.util.http.requests.JwtRequest;

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

    public void getJwt() {
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
        registration.put("jwt", jwt);
        HttpQueue requestQueue = HttpQueue.getInstance(AnalyticsApplication.getContext());
        JSONObject regInfo = Registrar.getInstance().getJsonRegistration();
        JsonObjectRequest registerRequest = new JsonObjectRequest(Request.Method.POST, dispatchUrl,
                regInfo, this::onResponse, this::onError);
        requestQueue.addToRequestQueue(registerRequest, TAG);
    }

    private void onError(VolleyError error) {
        String msg = new String(error.networkResponse.data);
        Log.wtf(TAG, msg);
    }

    private void onResponse(JSONObject response) {
        Log.wtf(TAG, "Response Returned");
    }
}
