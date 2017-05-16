package edu.txstate.mobile.tracs.gestures;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.view.VelocityTrackerCompat;
import android.util.Log;
import android.util.LongSparseArray;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.widget.ListView;

import edu.txstate.mobile.tracs.MainActivity;
import edu.txstate.mobile.tracs.R;
import edu.txstate.mobile.tracs.adapters.NotificationsAdapter;
import edu.txstate.mobile.tracs.notifications.tracs.TracsNotification;
import edu.txstate.mobile.tracs.util.async.StatusUpdate;

public class NotificationTouchListener implements View.OnTouchListener {

    private static final String TAG = "NotifTouchListener";
    private VelocityTracker velocityTracker;
    private float downPressX, downPressY;
    private int swipeSlop = -1;
    private Context context;
    private Activity activity;
    private boolean itemPressed = false;
    private boolean swiping = false;
    private boolean isOnClick = false;
    private LongSparseArray<Integer> itemIdTopMap = new LongSparseArray<>();
    private float currentVelocityX;

    private static final int SWIPE_DURATION = 250;
    private static final int MOVE_DURATION = 150;

    public NotificationTouchListener(Context context) {
        //For convenience I am saving the context twice.
        this.context = context;
        this.activity = (Activity) context;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int index = event.getActionIndex();
        int action = event.getActionMasked();
        int pointerId = event.getPointerId(index);

        //Swipe slop is the number of pixels you can go until it's considered a real
        //scroll event, varies by device.
        if (swipeSlop < 0) {
            swipeSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        }

        //Process the event
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (itemPressed) {
                    return false;
                }
                itemPressed = true;

                downPressX = event.getX();
                downPressY = event.getY();
                isOnClick = true;
                if (velocityTracker == null) {
                    velocityTracker = VelocityTracker.obtain();
                } else {
                    velocityTracker.clear();
                }
                velocityTracker.addMovement(event);
                break;
            case MotionEvent.ACTION_CANCEL:
                v.setAlpha(1);
                v.setTranslationX(0);
                itemPressed = false;
                break;
            case MotionEvent.ACTION_MOVE: {
                float xPos = event.getX() + v.getTranslationX();
                float deltaX = xPos - downPressX;
                float deltaXAbs = Math.abs(deltaX);

                boolean barelyMoved = Math.abs(downPressX - event.getX()) > 2;
                if (isOnClick && barelyMoved) {
                    isOnClick = false;
                }

                if (!swiping) {
                    if (deltaXAbs > swipeSlop) {
                        swiping = true;
                        ListView listView = (ListView) this.activity.findViewById(R.id.notifications_list);
                        listView.requestDisallowInterceptTouchEvent(true);
                    }
                }

                if (swiping) {
                    v.setTranslationX(xPos - downPressX);
                    v.setAlpha(1 - deltaXAbs / v.getWidth());
                }
                velocityTracker.addMovement(event);
                velocityTracker.computeCurrentVelocity(1000);
                currentVelocityX = VelocityTrackerCompat.getXVelocity(velocityTracker, pointerId);
            }
            break;
            case MotionEvent.ACTION_UP: {
                if (isOnClick) {
                    Log.i(TAG, "Clicked");
                    onClick(v);
                }

                if (swiping) {
                    float xPos = event.getX() + v.getTranslationX();
                    float deltaX = xPos - downPressX;
                    float deltaXAbs = Math.abs(deltaX);
                    float endX, endAlpha;
                    final boolean remove;

                    float fractionCovered = deltaXAbs / v.getWidth();
                    if (deltaXAbs > v.getWidth() / 4) {
                        endX = deltaX < 0 ? -v.getWidth() : v.getWidth();
                        endAlpha = 0;
                        remove = true;
                    } else {
                        fractionCovered = 1 - fractionCovered;
                        endX = 0;
                        endAlpha = 1;
                        remove = false;
                    }

                    long duration = (int) ((1 - fractionCovered) * SWIPE_DURATION);
                    ListView listView = (ListView) this.activity.findViewById(R.id.notifications_list);
                    listView.setEnabled(false);
                    v.animate().setDuration(duration)
                            .alpha(endAlpha).translationX(endX)
                            .withEndAction(() -> {
                                v.setAlpha(1);
                                v.setTranslationX(0);
                                if (remove) {
                                    animateRemoval(listView, v);
                                } else {
                                    swiping = false;
                                    listView.setEnabled(true);
                                }
                            });
                }
                itemPressed = false;
            }
            break;
            default:
                return false;
        }
        return true;
    }

    private void onClick(View view) {
        ListView listView = (ListView) this.activity.findViewById(R.id.notifications_list);
        int position = listView.getPositionForView(view);
        TracsNotification notification = (TracsNotification) listView.getAdapter().getItem(position);
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("url", notification.getUrl());
        context.startActivity(intent);
        new StatusUpdate().updateRead(notification);
    }

    private void animateRemoval(final ListView listView, View viewToRemove) {
        int firstVisiblePosition = listView.getFirstVisiblePosition();
        NotificationsAdapter adapter = (NotificationsAdapter) listView.getAdapter();
        for (int i = 0; i < listView.getChildCount(); ++i) {
            View child = listView.getChildAt(i);
            if (child != viewToRemove) {
                int position = firstVisiblePosition + i;
                long itemId = adapter.getItemId(position);
                itemIdTopMap.put(itemId, child.getTop());
            }
        }

        int position = listView.getPositionForView(viewToRemove);
        adapter.remove(adapter.getItem(position));

        final ViewTreeObserver observer = listView.getViewTreeObserver();
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                observer.removeOnPreDrawListener(this);
                boolean firstAnimation = true;
                int firstVisiblePosition = listView.getFirstVisiblePosition();
                for (int i = 0; i < listView.getChildCount(); ++i) {
                    final View child = listView.getChildAt(i);
                    int position = firstVisiblePosition + i;
                    long itemId = adapter.getItemId(position);
                    Integer startTop = itemIdTopMap.get(itemId);
                    int top = child.getTop();
                    if (startTop != null) {
                        if (startTop != top) {
                            int delta = startTop - top;
                            child.setTranslationY(delta);
                            Log.wtf(TAG, "Velocity: " + currentVelocityX);
                            child.animate().setDuration(Math.round(MOVE_DURATION * (1 / Math.abs(currentVelocityX)))).translationY(0);
                            if (firstAnimation) {
                                child.animate().withEndAction(() -> {
                                    swiping = false;
                                    listView.setEnabled(true);
                                });
                                firstAnimation = false;
                            }
                        }
                    } else {
                        int childHeight = child.getHeight() + listView.getDividerHeight();
                        startTop = top + (i > 0 ? childHeight : -childHeight);
                        int delta = startTop - top;
                        child.setTranslationY(delta);
                        Log.wtf(TAG, "Velocity: " + currentVelocityX);
                        child.animate().setDuration(MOVE_DURATION).translationY(0);
                        if (firstAnimation) {
                            child.animate().withEndAction(() -> {
                                swiping = false;
                                listView.setEnabled(true);
                            });
                            firstAnimation = false;
                        }
                    }
                }
                itemIdTopMap.clear();
                return true;
            }
        });
    }
}