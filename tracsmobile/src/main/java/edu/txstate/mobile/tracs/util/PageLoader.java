package edu.txstate.mobile.tracs.util;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import edu.txstate.mobile.tracs.AnalyticsApplication;

@SuppressWarnings("ALL")
public class PageLoader {
    private static final PageLoader pageLoader = new PageLoader();
    private static final String TAG = "PageLoader";
    public static PageLoader getInstance() {
        return pageLoader;
    }

    private PageLoader() {
    }

    public String loadHtml(String filename) {
        InputStream input = null;
        String returnString = null;
        try {
            input = AnalyticsApplication.getContext().getAssets().open(filename);
        } catch (IOException e) {
            Log.e(TAG, "Couldn't open html file");
            returnString = "";
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String line;
        StringBuilder stringBuilder = new StringBuilder();
        String ls = System.getProperty("line.separator");

        try {
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }
            returnString = stringBuilder.toString();
        } catch(IOException e) {
            Log.e(TAG, "Couldn't read html file");
            returnString = "";
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                Log.e(TAG, "File was not open");
                returnString = "";
            }
        }

        return stringBuilder.toString();
    }
}
