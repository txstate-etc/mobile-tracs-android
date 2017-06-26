package edu.txstate.mobile.tracs.util;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import edu.txstate.mobile.tracs.R;

public abstract class SwipeUtil extends ItemTouchHelper.SimpleCallback {

    private Drawable background;
    private Drawable deleteIcon;

    private int xMarkMargin;

    private boolean initiated;
    private Context context;
    private int leftColorCode;
    private String leftSwipeLabel;
    private int screenSize;

    public SwipeUtil(int dragDirs, int swipeDirs, Context context) {
        super(dragDirs, swipeDirs);
        this.context = context;
        screenSize = context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK;
    }

    private void init() {
        background = new ColorDrawable();
        xMarkMargin = getMargin(screenSize);
        deleteIcon = new IconDrawable(context, FontAwesomeIcons.fa_trash_o).actionBarSize();
        deleteIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        initiated = true;
    }


    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder viewHolder1) {
        return false;
    }

    @Override
    public abstract void onSwiped(RecyclerView.ViewHolder viewHolder, int i);

    @Override
    public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        return super.getSwipeDirs(recyclerView, viewHolder);
    }

    @Override
    public void onChildDraw(Canvas canvas, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        View itemView = viewHolder.itemView;
        if (!initiated) {
            init();
        }

        int itemHeight = itemView.getBottom() - itemView.getTop();

        ((ColorDrawable) background).setColor(getLeftColorCode());
        background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
        background.draw(canvas);

        int intrinsicWidth = deleteIcon.getIntrinsicWidth();
        int intrinsicHeight = deleteIcon.getIntrinsicHeight();

        int xMarkLeft = itemView.getRight() - xMarkMargin - intrinsicWidth;
        int xMarkRight = itemView.getRight() - xMarkMargin;
        int xMarkTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 4;
        int xMarkBottom = xMarkTop + intrinsicHeight;

        deleteIcon.setBounds(xMarkLeft, xMarkTop, xMarkRight, xMarkBottom);

        Paint paint = new Paint();
        paint.setColor(context.getResources().getColor(R.color.dismissText));
        int textSize, leftMargin;
        switch (screenSize) {
            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                textSize = 24;
                leftMargin = 25;
                break;
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                textSize = 56;
                leftMargin = 40;
                break;
            default:
                textSize = 48;
                leftMargin = 40;
                break;
        }
        paint.setTextSize(textSize);
        paint.setTextAlign(Paint.Align.CENTER);

        canvas.drawText(getLeftSwipeLabel(), xMarkLeft + leftMargin, xMarkBottom + intrinsicHeight - 25, paint);
        deleteIcon.draw(canvas);

        super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    private String getLeftSwipeLabel() {
        return leftSwipeLabel;
    }

    private int getLeftColorCode() {
        return leftColorCode;
    }

    public void setLeftSwipeLabel(String label) {
        this.leftSwipeLabel = label;
    }

    public void setLeftColorCode(int colorCode) {
        this.leftColorCode = colorCode;
    }

    private int getMargin(int screenSize) {
        int margin;
        switch(screenSize) {
            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                margin = (int) context.getResources().getDimension(R.dimen.ic_clear_margin_large);
                break;
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
            default:
                margin = (int) context.getResources().getDimension(R.dimen.ic_clear_margin_normal);
                break;
        }
        return margin;
    }
}

