package edu.txstate.mobile.tracs;

import android.content.res.Configuration;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.PaintDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.webkit.WebView;
import android.widget.ImageView;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.widget.IconTextView;

public class AboutActivity extends BaseTracsActivity {

    private static final String SCREEN_NAME = "About";
    private static final String TAG = "AboutActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_about_the_app);
        super.onCreate(savedInstanceState);

        WebView webView = (WebView) findViewById(R.id.about_web_view);
        webView.loadUrl("file:///android_asset/html/welcome_page.html");
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
}
