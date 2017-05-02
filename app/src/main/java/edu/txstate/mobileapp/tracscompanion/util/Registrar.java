package edu.txstate.mobileapp.tracscompanion.util;


import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class Registrar {
    private String token;
    private String platform;
    private String app_id;
    private String user_id;
    private boolean global_disable = false;
    private String[] blacklist;

    private static Registrar registrar;

    private Registrar(Context context) {
        init(context);
    }

    private void init(Context context) {
        token = AppInstanceId.getInstanceId(context).toString();
        platform = "android";

        app_id = AppStorage.get(AppStorage.NOTIFICATION_ID, context);

        user_id = AppStorage.get(AppStorage.USERNAME, context);

        //TODO: Write subclass for blacklist, then turn it into a JsonArray
        blacklist = new String[0];
    }

    public static Registrar getRegistrationInfo(Context context) {
        if (registrar == null) {
            registrar = new Registrar(context);
        }
        return registrar;
    }

    public void refreshRegistartion(Context context) {
        init(context);
    }

    public void toggleGlobalDisable() {
        this.global_disable = !this.global_disable;
    }

    public JsonObject getJsonRegistration() {
        if (registrar == null) {
            return new JsonObject();
        }
        Gson gson = new Gson();
        return gson.toJsonTree(registrar).getAsJsonObject();
    }
}
