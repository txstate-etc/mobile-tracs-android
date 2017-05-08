package edu.txstate.mobile.tracs.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import edu.txstate.mobile.tracs.AnalyticsApplication;

@SuppressLint("AppCompatCustomView")
public class FontAwesome extends TextView {
    public FontAwesome(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public FontAwesome(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FontAwesome(Context context) {
        super(context);
        init();
    }

    public void init() {
        Typeface typeFace = Typeface.createFromAsset(AnalyticsApplication.getContext().getAssets(),
                "fonts/fontawesome-webfont.ttf");
        setTypeface(typeFace);
    }
}
