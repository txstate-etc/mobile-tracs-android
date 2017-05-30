package edu.txstate.mobile.tracs;

import android.os.Bundle;
import android.view.Menu;

public class SupportActivity extends BaseTracsActivity {

    private static final String TAG = "SupportActivity";
    private static final String SCREEN_NAME = "Support";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_contact);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume(){
        super.onResume();
        super.hitScreenView(SCREEN_NAME);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.setupOptionsMenu(menu);
        super.optionsMenu.findItem(R.id.menu_tracs_support).setEnabled(false);
        return true;
    }
}
