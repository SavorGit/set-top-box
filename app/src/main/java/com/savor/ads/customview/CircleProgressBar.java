package com.savor.ads.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.savor.ads.R;

/**
 * 圆环进度条
 */
public class CircleProgressBar extends View {
    private static final int MODE_ADD = 1;
    private static final int MODE_REDUCE = 2;
    private Paint mPaint;

    private boolean mIsIndeterminate;
    private int mIndeterminateDuration;
    private int mBackgroundColor;
    private int mFillColor;
    private int mStrokeWidth;

    /** 是否持续绘制（动画效果）*/
    private boolean doAnimation;

    /** 绘制频率*/
    private final int DRAW_PERIOD = 25;
    /** 起始角度*/
    private float mStartDegree = -90;
    /** 当前转过的角度*/
    private float mSweepDegree;

    private int mDrawMode;

    public CircleProgressBar(Context context) {
        super(context);
        init(null, 0);
    }

    public CircleProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public CircleProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.CircleProgressBar, defStyle, 0);

        mIsIndeterminate = a.getBoolean(R.styleable.CircleProgressBar_indeterminate, false);
        mIndeterminateDuration = a.getInt(
                R.styleable.CircleProgressBar_indeterminateDuration,
                1000);
        mBackgroundColor = a.getColor(
                R.styleable.CircleProgressBar_backgroundColor,
                0x34000000);
        mFillColor = a.getColor(
                R.styleable.CircleProgressBar_fillColor,
                0x0000ff);
        mStrokeWidth = a.getDimensionPixelSize(R.styleable.CircleProgressBar_strokeWidth, 10);

        a.recycle();

        mPaint = new Paint(Paint.DITHER_FLAG | Paint.ANTI_ALIAS_FLAG);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        mDrawMode = MODE_ADD;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // allocations per draw cycle.
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int contentWidth = getWidth() - paddingLeft - paddingRight;
        int contentHeight = getHeight() - paddingTop - paddingBottom;

        // 画底环
        mPaint.setColor(mBackgroundColor);
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(contentWidth / 2f, contentHeight / 2f, contentWidth / 2f - mStrokeWidth, mPaint);

        // 画填充环
        if (mIsIndeterminate && doAnimation) {
            if (MODE_ADD == mDrawMode) {
                mSweepDegree += (360f / mIndeterminateDuration) * DRAW_PERIOD;
                mStartDegree = -90;
                if (mSweepDegree > 360) {
                    mSweepDegree = 360;
                    mDrawMode = MODE_REDUCE;
//                    mStartDegree = (mStartDegree + (360f / mIndeterminateDuration) * DRAW_PERIOD) % 360;
//                    mSweepDegree = 270 - mStartDegree;
                }
            } else {
                mStartDegree = (mStartDegree + (360f / mIndeterminateDuration) * DRAW_PERIOD) % 360;
                mSweepDegree = 270 - mStartDegree;
                if (mStartDegree > 270) {
                    mStartDegree = -90;
                    mSweepDegree = 0;
                    mDrawMode = MODE_ADD;
//                    mSweepDegree += (360f / mIndeterminateDuration) * DRAW_PERIOD;
                }
            }

            mPaint.setColor(mFillColor);
            canvas.drawArc(new RectF(mStrokeWidth, mStrokeWidth, contentWidth - mStrokeWidth, contentHeight - mStrokeWidth),
                    mStartDegree, mSweepDegree, false, mPaint);

            if (MODE_ADD == mDrawMode && mSweepDegree == 0) {
                postInvalidateDelayed(6 * DRAW_PERIOD);
            } else {
                postInvalidateDelayed(DRAW_PERIOD);
            }
        }
    }

    public void setProgress(float progress) {
        if (!mIsIndeterminate) {
            mSweepDegree = progress * 360 / 100;

        }
    }

    public float getProgress() {
        float progress = -1;
        if (!mIsIndeterminate) {
            progress = mSweepDegree * 100 / 360;
        }
        return progress;
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (VISIBLE == visibility) {
            mSweepDegree = 0;
            doAnimation = true;
        } else {
            doAnimation = false;
        }
    }
}
