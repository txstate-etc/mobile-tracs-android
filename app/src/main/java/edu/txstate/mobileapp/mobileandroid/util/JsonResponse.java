package edu.txstate.mobileapp.mobileandroid.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class JsonResponse {
    public static String parse(InputStream input) throws IOException {
        InputStreamReader inputReader = new InputStreamReader(input);
        int data = inputReader.read();
        StringBuilder output = new StringBuilder();
        while (data >= 0) {
            char current = (char) data;
            output.append(current);
            data = inputReader.read();
        }
        return output.toString();
    }
}
