package edu.txstate.mobileapp.tracscompanion.util;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;

import static android.content.Context.MODE_PRIVATE;

public final class AppInstanceId {
    private static final String FILE_NAME = "instance_id";
    private static final String TAG = "AppInstanceId";

    private static final String INSTANCE_ID_TAG = "InstanceID ";
    private static final String SHARED_PREF_KEY_TAG = "SharedPrefsKey ";
    private AppInstanceId() {}

    public static UUID getInstanceId(Context context) {
        UUID id;
        if (instanceIdExists(context)) {
            id = readInstanceId(context);
        } else {
            id = createInstanceId(context);
        }
        return id;
    }

    public static UUID getKey(Context context) {
        UUID key;
        if (keyExists(context)) {
            key = readKey(context);
        } else {
            key = createKey(context);
        }
        return key;
    }

    private static void store(Context context, String id, String tag) {
        FileOutputStream deviceStorage = null;
        String line = tag + id;
        try {
            deviceStorage = context.openFileOutput(FILE_NAME, MODE_PRIVATE);
            deviceStorage.write(line.getBytes());
            deviceStorage.close();
        } catch (IOException writeError) {
            //This sucks.
            try {
                if (deviceStorage != null) {
                    deviceStorage.close();
                }
            } catch (IOException closeError) {
                Log.e(TAG, closeError.getMessage());
            }
            Log.e(TAG, writeError.getMessage());
        }
    }

    private static UUID createInstanceId(Context context) {
        UUID instanceId = UUID.randomUUID();
        store(context, instanceId.toString(), INSTANCE_ID_TAG);
        return instanceId;
    }

    private static boolean instanceIdExists(Context context) {
        FileInputStream deviceStorage = null;
        try {
            deviceStorage = context.openFileInput(FILE_NAME);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

        return deviceStorage != null;
    }

    private static UUID readInstanceId(Context context) {
        String instanceId = "";
        try {
            FileInputStream deviceStorage = context.openFileInput(FILE_NAME);
            BufferedReader reader = new BufferedReader( new InputStreamReader(deviceStorage));
            String line = reader.readLine();
            while (line != null) {
                if (line.contains(INSTANCE_ID_TAG)) {
                    instanceId = line.split(" ")[1];
                }
                line = reader.readLine();
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

        if (instanceId.isEmpty()) {
            return null;
        } else {
            return UUID.fromString(instanceId);
        }
    }

    private static UUID createKey(Context context) {
        UUID key = UUID.randomUUID();
        store(context, key.toString(), SHARED_PREF_KEY_TAG);
        return key;
    }

    private static UUID readKey(Context context) {
        String sharedPrefsKey = "";
        try {
            FileInputStream deviceStorage = context.openFileInput(FILE_NAME);
            BufferedReader reader = new BufferedReader( new InputStreamReader(deviceStorage));
            String line = reader.readLine();
            while(line != null) {
                if (line.contains(SHARED_PREF_KEY_TAG)) {
                    sharedPrefsKey = line.split(" ")[1];
                }
                line = reader.readLine();
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

        if (sharedPrefsKey.isEmpty()) {
            return null;
        } else {
            return UUID.fromString(sharedPrefsKey);
        }
    }

    private static boolean keyExists(Context context) {
        FileInputStream deviceStorage;
        String key = "";
        try {
            deviceStorage = context.openFileInput(FILE_NAME);

            BufferedReader reader = new BufferedReader(new InputStreamReader(deviceStorage));

            String line = reader.readLine();
            while(line != null) {
                if (line.contains(SHARED_PREF_KEY_TAG)) {
                    key = line.split(" ")[1];
                    Log.i(TAG, key);
                }
                line = reader.readLine();
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        return !key.isEmpty();
    }
}
