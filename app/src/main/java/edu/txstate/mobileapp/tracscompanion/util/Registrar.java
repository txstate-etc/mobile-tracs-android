package edu.txstate.mobileapp.tracscompanion.util;


import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class Registrar {
    private String token;
    private String platform;
    private String app_id;
    private String user_id;
    private boolean global_disable;
    private String[] blacklist;

    private static Registrar registrar;

    private Registrar(Context context) {
        token = AppInstanceId.getInstanceId(context).toString();
        platform = "android";

        //TODO: Find out what this is and where it comes from
        app_id = "app-id-goes-here";

        user_id = AppStorage.get(AppStorage.TRACS_ID, context);

        global_disable = false;

        //TODO: Write subclass for blacklist, then turn it into a JsonArray
        blacklist = new String[0];
    }

    public static Registrar getRegistrationInfo(Context context) {
        if (registrar == null) {
            registrar = new Registrar(context);
        }
        return registrar;
    }

    public JsonObject getJsonRegistration() {
        if (registrar == null) {
            return new JsonObject();
        }
        Gson gson = new Gson();
        return gson.toJsonTree(registrar).getAsJsonObject();
    }
}
