package com.gauge.tsnt.gaugeview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * Created by ting说你跳 on 2017/6/15.
 */

public class GaugeView extends View {
    public static String TAG = "GaugeView";

    private int mStartNum;
    private int mEndNum;
    private int mGapNum;
    private int mGapDistance;
    private int mTextSize;
    private String mTextColor;
    private int mLineWidth;
    private String mLineColor;

    private int mHeight;
    private int mWidth;
    private int mCountOfLines;
    private int mLastX;
    private int mLastY;
    private int mMaxX;
    private int mLastMiddleNum;
    private boolean scrollToNearstNum;

    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;
    private OnGaugeScrollChangeListener mOnGaugeScrollChangeListener;

    public GaugeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public GaugeView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.GaugeView);
        mStartNum = typedArray.getInt(R.styleable.GaugeView_startNum, 100);
        mEndNum = typedArray.getInt(R.styleable.GaugeView_endNum, 1000);
        mGapNum = typedArray.getInt(R.styleable.GaugeView_gapNum, 20);
        mGapDistance = (int) typedArray.getDimension(R.styleable.GaugeView_gapDistance, 40);
        mTextSize = (int) typedArray.getDimension(R.styleable.GaugeView_textSize, 36);
        mTextColor = typedArray.getString(R.styleable.GaugeView_textColor);
        if (mTextColor == null) {
            mTextColor = "#333333";
        }
        mLineWidth = (int) typedArray.getDimension(R.styleable.GaugeView_lineWidth, 1);
        mLineColor = typedArray.getString(R.styleable.GaugeView_lineColor);
        if (mLineColor == null) {
            mLineColor = "#666666";
        }

        mCountOfLines = (mEndNum - mStartNum) / mGapNum + 1;
        mMaxX = (mCountOfLines - 1) * mGapDistance;

        if (mScroller == null) {
            mScroller = new Scroller(getContext());
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    public interface OnGaugeScrollChangeListener {
        /**
         * @param currentMiddleNum the num of the current middle location
         */
        void onGaugeScrollChange(int currentMiddleNum);
    }

    public void setOnGaugeScrollChangeListener(OnGaugeScrollChangeListener onGaugeScrollChangeListener) {
        mOnGaugeScrollChangeListener = onGaugeScrollChangeListener;
    }

    private void smoothScrollBy(int deltaX) {
        mScroller.startScroll(getScrollX(), 0, deltaX, 0, 1000);
        invalidate();
    }

    private void scrollToNearstNum() {
        scrollToNearstNum = false;
        int goalLocation = 200 + (getScrollX() + mGapDistance / 2) / mGapDistance * mGapNum;
        int goalX = (goalLocation - 200) / mGapNum * mGapDistance;
        scrollTo(goalX, 0);
    }

    private void updateMiddleNum() {
        int currentMiddleNum = mStartNum + getScrollX() / mGapDistance * mGapNum;
        if (currentMiddleNum != mLastMiddleNum) {
            mLastMiddleNum = currentMiddleNum;
            if (mOnGaugeScrollChangeListener != null) {
                mOnGaugeScrollChangeListener.onGaugeScrollChange(currentMiddleNum);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        mHeight = getLayoutParams().height;
        mWidth = getLayoutParams().width;
        if (mHeight == ViewGroup.LayoutParams.MATCH_PARENT || mHeight == ViewGroup.LayoutParams.WRAP_CONTENT) {
            mHeight = heightSpecSize;
        }
        if (mWidth == ViewGroup.LayoutParams.MATCH_PARENT || mWidth == ViewGroup.LayoutParams.WRAP_CONTENT) {
            mWidth = widthSpecSize;
        }
        mTextSize = Math.min((int) (0.4 * mHeight), mTextSize);
        setMeasuredDimension(widthMeasureSpec, mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint linePaint = new Paint();
        linePaint.setStrokeWidth(mLineWidth);
        linePaint.setColor(Color.parseColor(mLineColor));

        Paint textPaint = new Paint();
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(mTextSize);
        textPaint.setColor(Color.parseColor(mTextColor));

        float[] pts = new float[mCountOfLines * 4];
        for (int i = 0; i < mCountOfLines; i++) {
            pts[i * 4] = mWidth / 2 + mGapDistance * i;
            pts[i * 4 + 1] = mHeight;
            pts[i * 4 + 2] = mWidth / 2 + mGapDistance * i;
            if (i % 5 == 0) {
                pts[i * 4 + 3] = (float) (0.5 * mHeight);
                canvas.drawText(mStartNum + 20 * i + "", mWidth / 2 + mGapDistance * i, (float) (0.25 * mHeight + 0.5 * mTextSize), textPaint);
            } else {
                pts[i * 4 + 3] = (float) (0.7 * mHeight);
            }
        }
        canvas.drawLines(pts, linePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mVelocityTracker.addMovement(event);
        int x = (int) event.getX();
        int y = (int) event.getY();
        int goalX;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX = x - mLastX;
                int deltaY = y - mLastY;
                if (Math.abs(deltaX) > Math.abs(deltaY)) {
                    goalX = (getScrollX() + mLastX - x);
                    goalX = Math.max(0, Math.min(goalX, mMaxX));
                    scrollTo(goalX, 0);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mVelocityTracker.computeCurrentVelocity(200, 5000);
                float xVelocity = mVelocityTracker.getXVelocity();
                scrollToNearstNum = true;
                if (Math.abs(xVelocity) > 10) {
                    goalX = getScrollX() - (int) xVelocity;
                    goalX = Math.max(0, Math.min(goalX, mMaxX));
                    smoothScrollBy(goalX - getScrollX());
                } else {
                    invalidate();
                }
                mVelocityTracker.clear();
                break;
        }
        mLastX = x;
        mLastY = y;
        return true;
    }

    @Override
    public void computeScroll() {
        updateMiddleNum();
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), 0);
            postInvalidate();
        } else if (scrollToNearstNum) {
            scrollToNearstNum();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        mVelocityTracker.recycle();
        super.onDetachedFromWindow();
    }
}
