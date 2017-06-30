package edu.txstate.mobile.tracs.util

import android.util.Log
import com.android.volley.Response
import com.google.firebase.iid.FirebaseInstanceId
import edu.txstate.mobile.tracs.AnalyticsApplication
import edu.txstate.mobile.tracs.BuildConfig
import edu.txstate.mobile.tracs.R
import edu.txstate.mobile.tracs.util.http.HttpQueue
import edu.txstate.mobile.tracs.util.http.listeners.RegisterCallback
import edu.txstate.mobile.tracs.util.http.requests.DispatchRegistrationRequest
import edu.txstate.mobile.tracs.util.http.requests.DispatchUnregisterRequest
import edu.txstate.mobile.tracs.util.http.requests.JwtRequest
import org.json.JSONObject

class Registrar {
    private val registration = HashMap<String, String>()
    private lateinit var registerCallback: RegisterCallback
    private lateinit var errorListener: Response.ErrorListener

    companion object {
        val TOKEN_URL: String = AnalyticsApplication.getContext().getString(R.string.jwt_url)
        val DISPATCH_URL: String = AnalyticsApplication.getContext().getString(R.string.dispatch_registration)
        val TAG = "Registrar"
        @JvmStatic
        val instance = Registrar()
    }

    init {
        updateRegistration()
    }

    private fun updateRegistration() {
        val context = AnalyticsApplication.getContext()
        registration.put("token", FirebaseInstanceId.getInstance().token ?: "")
        registration.put("platform", "android")
        registration.put("app_id", BuildConfig.APPLICATION_ID)
        registration.put("user_id", AppStorage.get(AppStorage.USERNAME, context))
    }

    private fun getJsonRegistration(): JSONObject {
        updateRegistration()
        return JSONObject(Registrar.instance.registration)
    }

    fun registerDevice(registerCallback: RegisterCallback, errorListener: Response.ErrorListener) {
        this.registerCallback = registerCallback
        this.errorListener = errorListener
        val requestQueue = HttpQueue.getInstance(AnalyticsApplication.getContext())
        val jwtRequest = JwtRequest(
                TOKEN_URL,
                Registrar.instance::receiveJwt,
                { _ -> Log.e(TAG, "Could not retrieve a valid JWT") }
        )
        requestQueue.addToRequestQueue(jwtRequest, this)
    }

    fun unregisterDevice() {
        val requestQueue = HttpQueue.getInstance(AnalyticsApplication.getContext())
        val token = FirebaseInstanceId.getInstance().token
        val unregisterRequest = DispatchUnregisterRequest(DISPATCH_URL, token)
        requestQueue.addToRequestQueue(unregisterRequest, this)
    }

    private fun receiveJwt(jwt: String) {
        val url = "$DISPATCH_URL?jwt=$jwt"
        val requestQueue = HttpQueue.getInstance(AnalyticsApplication.getContext())
        val regInfo = Registrar.instance.getJsonRegistration()
        val registerRequest = DispatchRegistrationRequest(url, regInfo, registerCallback, errorListener)
        requestQueue.addToRequestQueue(registerRequest, this)
    }
}