package edu.txstate.mobile.tracs.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import edu.txstate.mobile.tracs.AnalyticsApplication;
import edu.txstate.mobile.tracs.R;

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

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int widthDesc = MeasureSpec.getMode(widthMeasureSpec);
        int heightDesc = MeasureSpec.getMode(heightMeasureSpec);
        int size;

        int unspecified = MeasureSpec.UNSPECIFIED;

        boolean dimensionsUnspecified = widthDesc == unspecified && heightDesc == unspecified;
        boolean oneDimensionUnspecified = widthDesc == unspecified ^ heightDesc == unspecified;

        if (dimensionsUnspecified) { //Use a default value
            size = getContext().getResources().getDimensionPixelSize(R.dimen.default_size);
        } else if (oneDimensionUnspecified) { //Take the larger non-zero dimension
            size = width > height ? width : height;
        } else { //Take the smaller dimension for use
            size = width > height ? height : width;
        }

        setMeasuredDimension(size, size);
    }
}
