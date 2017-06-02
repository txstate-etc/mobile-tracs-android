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

        ShapeDrawable.ShaderFactory shaderFactory = new ShapeDrawable.ShaderFactory() {
            @Override
            public Shader resize(int width, int height) {
                LinearGradient lg = new LinearGradient(width / 2, 0, width / 2, height,
                        0xFF698E91, 0xFF1C3F63, Shader.TileMode.REPEAT);
                return lg;
            }
        };
        PaintDrawable p = new PaintDrawable();
        p.setShape(new RectShape());
        p.setShaderFactory(shaderFactory);

        View content = findViewById(android.R.id.content);
        content.setBackgroundDrawable(p);

        ImageView horizontalPhone = (ImageView) findViewById(R.id.horizontal_phone_icon);

        RotateAnimation rotateCW = new RotateAnimation(0.0f, -90.0f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        rotateCW.setDuration(1500);
        rotateCW.setStartOffset(1000);
        rotateCW.setRepeatCount(Animation.INFINITE);
        rotateCW.setRepeatMode(Animation.REVERSE);

        int screenSize = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        int iconSize;
        switch (screenSize) {
            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                iconSize = 48;
                break;
            default:
                iconSize = 34;
                break;
        }

        IconDrawable horizPhoneIcon = new IconDrawable(this,
                FontAwesomeIcons.fa_mobile_phone)
                .colorRes(R.color.betaIconColor)
                .sizeDp(iconSize);

        AnimationSet animSet = new AnimationSet(true);
        animSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animSet.setFillAfter(true);
        animSet.setFillEnabled(true);
        animSet.addAnimation(rotateCW);

        horizontalPhone.setImageDrawable(horizPhoneIcon);
        //Dormant animation
//        horizontalPhone.startAnimation(animSet);
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
