package edu.txstate.mobileapp.mobileandroid.util;

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

    private static UUID createInstanceId(Context context) {
        String instanceId = UUID.randomUUID().toString();
        storeInstanceId(context, instanceId);
        return UUID.fromString(instanceId);
    }

    private static void storeInstanceId(Context context, String id) {
        FileOutputStream deviceStorage = null;
        try {
            deviceStorage = context.openFileOutput(FILE_NAME, MODE_PRIVATE);
            deviceStorage.write(id.getBytes());
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
        String instanceId = null;
        try {
            FileInputStream deviceStorage = context.openFileInput(FILE_NAME);
            BufferedReader reader = new BufferedReader( new InputStreamReader(deviceStorage));
            instanceId = reader.readLine();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

        if (instanceId == null) {
            return null;
        } else {
            return UUID.fromString(instanceId);
        }
    }
}
