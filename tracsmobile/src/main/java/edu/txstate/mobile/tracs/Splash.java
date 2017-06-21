package edu.txstate.mobile.tracs;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class Splash extends AppCompatActivity {
    private final String extraTitle = "shouldLoadNotificationsView";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String shouldLoadNotifications = getIntent().getStringExtra(extraTitle);
        Intent mainActivity = new Intent(Splash.this, MainActivity.class);
        mainActivity.putExtra(extraTitle, shouldLoadNotifications);
        startActivity(mainActivity);
        finish();
    }
}
