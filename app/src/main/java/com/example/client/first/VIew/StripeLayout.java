package com.example.client.first.VIew;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.example.client.first.R;

import java.util.ArrayList;

/**
 * Created by Lyn on 15/10/22.
 */
public class StripeLayout extends LinearLayout {

    private  static int INVALIDATE_DURATION = 50;
    private static int MID_RADIUS = 40;
    private static int MIN_EXCESS_LENGTH = 100;
    private static int CLICK_TRRIGER_DELAY = 600;
    boolean mIsPressed;
    int mTargetWidth;
    int mCenterX,mCenterY;
    int mRevealRadius,mRevealRadiusGap = 10;
    int left,right,top,bottom;
    boolean mIsQuickClick;
    long currentTime = 0l;

    DispatchUpTouchEventRunnable mDispatchUpTouchEventRunnable = new DispatchUpTouchEventRunnable();
    Paint mPaint = new Paint();
    View mTouchTarget;

    public Stripe(Context context) {
        super(context);
        mPaint.setColor(context.getResources().getColor(R.color.transparent));

    }

    public Stripe(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint.setColor(context.getResources().getColor(R.color.transparent));
    }

    public Stripe(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint.setColor(context.getResources().getColor(R.color.transparent));
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();
        int action = event.getAction();

        if (action == MotionEvent.ACTION_DOWN) {
            mIsPressed = true;
            currentTime = System.currentTimeMillis();
            View touchTarget = getTouchTarget(this, x, y);
            if (touchTarget != null && touchTarget.isClickable() && touchTarget.isEnabled()) {
                mTouchTarget = touchTarget;
                initParametersForChild(event, touchTarget);
                postInvalidateDelayed(INVALIDATE_DURATION);
            }
        } else if (action == MotionEvent.ACTION_UP) {
            mIsPressed = false;
            mIsQuickClick = System.currentTimeMillis() - currentTime < 50;
            mDispatchUpTouchEventRunnable.setEvent(event);
            postDelayed(mDispatchUpTouchEventRunnable, CLICK_TRRIGER_DELAY);
            return true;
        } else if (action == MotionEvent.ACTION_CANCEL) {
            mIsPressed = false;
        }

        return super.dispatchTouchEvent(event);
    }

    private void initParametersForChild(MotionEvent event, View touchTarget) {
        mTargetWidth = touchTarget.getMeasuredWidth();
        mCenterX = (int) event.getRawX();
        mCenterY = (int) event.getRawY();
        mRevealRadius = 10;

        int[] parentLocation = new int[2];
        this.getLocationInWindow(parentLocation);
        mCenterX -= parentLocation[0];
        mCenterY -= parentLocation[1];
        //获得目标view信息
        int[] location = new int[2];
        mTouchTarget.getLocationOnScreen(location);
        left = location[0] -parentLocation[0];
        top = location[1] - parentLocation[1];
        right = left + mTouchTarget.getMeasuredWidth();
        bottom = top + mTouchTarget.getMeasuredHeight();
    }

    //寻找child
    private View getTouchTarget(View view, int x, int y) {
        View target = null;
        ArrayList<View> TouchableViews = view.getTouchables();
        for (View child : TouchableViews) {
            if (isTouchPointInView(child, x, y)) {
                target = child;
                break;
            }
        }

        return target;
    }

    private boolean isTouchPointInView(View view, int x, int y) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int left = location[0];
        int top = location[1];
        int right = left + view.getMeasuredWidth();
        int bottom = top + view.getMeasuredHeight();
        if (view.isClickable() && y >= top && y <= bottom
                && x >= left && x <= right) {
            return true;
        }
        return false;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mTargetWidth <= 0 || mTouchTarget == null) {
            return;
        }
        //速度判断
        if(mIsQuickClick){
            mRevealRadius += mRevealRadiusGap * 5;
        }
        else if (mRevealRadius > MID_RADIUS) {
            mRevealRadius += mRevealRadiusGap * 4;
        } else {
            mRevealRadius += mRevealRadiusGap;
        }


        canvas.save();
        canvas.clipRect(left, top, right, bottom);
        canvas.drawCircle(mCenterX, mCenterY, mRevealRadius, mPaint);
        canvas.restore();

        if(isStopDrawCircle() && !mIsPressed){
            Drawable currentColor = mTouchTarget.getBackground();
            if (currentColor instanceof ColorDrawable){
                initCanvas(canvas, ((ColorDrawable) currentColor).getColor());
            }
            return;
        }
        postInvalidateDelayed(INVALIDATE_DURATION, left, top, right, bottom);
    }

    private void initCanvas(Canvas canvas,int color){
        canvas.save();
        canvas.clipRect(left, top, right, bottom);
        canvas.drawColor(color);
        canvas.restore();
    }

    //边界检查
    public boolean isStopDrawCircle() {
        return  mCenterX + mRevealRadius > right + MIN_EXCESS_LENGTH &&
                mCenterX - mRevealRadius < left - MIN_EXCESS_LENGTH &&
                mCenterY + mRevealRadius > bottom + MIN_EXCESS_LENGTH &&
                mCenterY - mRevealRadius < top - MIN_EXCESS_LENGTH;
    }

    class DispatchUpTouchEventRunnable implements Runnable {
        public MotionEvent event;

        public void setEvent(MotionEvent event) {
            this.event = event;
        }

        @Override
        public void run() {
            if (mTouchTarget == null || !mTouchTarget.isEnabled()) {
                return;
            }
            //强制消费
            if (isTouchPointInView(mTouchTarget, (int)event.getRawX(), (int)event.getRawY())) {
                mTouchTarget.dispatchTouchEvent(event);
            }
        }
    }
}
