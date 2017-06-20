package edu.txstate.mobile.tracs;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class Splash extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent mainActivity = new Intent(Splash.this, MainActivity.class);
        startActivity(mainActivity);
        finish();
    }
}
