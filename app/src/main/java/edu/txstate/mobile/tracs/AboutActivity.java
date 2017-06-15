package edu.txstate.mobile.tracs;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.PaintDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.Toast;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.widget.IconTextView;

import edu.txstate.mobile.tracs.util.AppStorage;

public class AboutActivity extends BaseTracsActivity implements View.OnTouchListener {

    private static final String SCREEN_NAME = "About";
    private static final int CLICK_ON_WEBVIEW = 17;
    private static final String TAG = "AboutActivity";
    private boolean firstLoad;
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.firstLoad = getIntent().getBooleanExtra("firstLaunch", false);
        if (this.firstLoad) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        setContentView(R.layout.activity_about_the_app);
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null && this.firstLoad) {
            getSupportActionBar().hide();
        }
        WebView webView = (WebView) findViewById(R.id.about_web_view);
        webView.loadUrl("file:///android_asset/html/welcome_page.html");
        if (firstLoad) {
            webView.setOnTouchListener(this);
        } else {
            webView.setOnTouchListener(null);
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        super.hitScreenView(SCREEN_NAME);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.setupOptionsMenu(menu);
        super.optionsMenu.findItem(R.id.menu_about_us).setEnabled(false);
        return true;
    }

    private void launchMainActivity() {
        Intent mainActivity = new Intent(this, MainActivity.class);
        startActivity(mainActivity);
        finish();
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            launchMainActivity();
            AppStorage.clearFirstLoad();
        }
        return false;
    }
}
