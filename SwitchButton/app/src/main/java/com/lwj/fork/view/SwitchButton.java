package com.lwj.fork.view;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.lwj.fork.R;

/**
 * Created by lwj on 16/4/12.
 * Des:
 */
public class SwitchButton extends View {
    // 背景的颜色
    public Paint mPaint;
    // 滑块
    public Paint mMaskPaint;


    //关闭时默认背景颜色
    public static final int CLOSE_PAINT_COLOR = 0x667f7f7f;
    //打开时默认背景颜色
    public static final int OPEN_PAINT_COLOR = 0xFF33A63A;
    //打开时背景颜色
    private int openColor;
    //关闭时背景颜色
    private int closeColor;
    private int maskColor;

    // 滑块半径
    private int mRadius;
    private int roundCorner = dip2px(18);

    // 空隙距离2dp
    private int intervalWidth = dip2px(2);
    // 背景绘制区域
    RectF bgRectf = new RectF();
    // 滑块绘制区域
    RectF mMaskRectF = new RectF();
    // 手指按下的 x
    float preX = 0;
    float oldleftOffset = 0;
    //  圆形滑块距离左侧的
    private float leftOffset = 0f;

    /**
     * 滑块的最小偏移距离
     */
    private float mMinMaskOffset = 0;
    /**
     * 滑块的最大偏移距离
     */
    private float mMaxMaskOffset = 0;
    boolean mIsChecked;
    // 是否滑动
    private boolean isMove = false;
    // 手指点击距左端
    private float clickLeftOffset = 0;
    // 滑块绘制矩形的宽度
    float mMaskRectFWidth;
    // 滑块绘制矩形的 高度
    float mMaskRectFHeight;

    public SwitchButton(Context context) {
        this(context, null);
    }

    public SwitchButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwitchButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.SwitchButton);
        openColor = array.getColor(R.styleable.SwitchButton_open_color, OPEN_PAINT_COLOR);
        closeColor = array.getColor(R.styleable.SwitchButton_close_color, CLOSE_PAINT_COLOR);
        maskColor = array.getColor(R.styleable.SwitchButton_mask_color, Color.WHITE);
        mIsChecked = array.getBoolean(R.styleable.SwitchButton_checked, false);
        array.recycle();
        initPaint();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int mWidth = getMeasuredSize(widthMeasureSpec, dip2px(24));
        int mHeight = getMeasuredSize(heightMeasureSpec, dip2px(12));
        setMeasuredDimension(mWidth, mHeight);
        mRadius = getMeasuredHeight() / 2 - intervalWidth;
        bgRectf.set(0, 0, getMeasuredWidth(), getMeasuredHeight());
        mMaskRectFWidth = mRadius * 2;
        mMaskRectFHeight = mMaskRectFWidth;
        leftOffset = intervalWidth;
        mMaxMaskOffset = getMeasuredWidth() - intervalWidth - mRadius * 2;
        mMinMaskOffset = intervalWidth;
        if (mIsChecked) {
            leftOffset = mMaxMaskOffset;
        } else {
            mMaxMaskOffset = mMinMaskOffset;
        }
    }

    private int getMeasuredSize(int measureSpecValue, int defaultValue) {
        int specMode = MeasureSpec.getMode(measureSpecValue);
        int specSize = MeasureSpec.getSize(measureSpecValue);
        int defaultSize = dip2px(defaultValue);
        if (specMode == MeasureSpec.EXACTLY) {
            defaultSize = specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            defaultSize = Math.min(specSize, defaultSize);
        }
        return defaultSize;
    }

    public void initPaint() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        //  画笔样式
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        if (mIsChecked) {
            mPaint.setColor(openColor);
        } else {
            mPaint.setColor(closeColor);
        }
        // 更加清晰和平滑
        mPaint.setDither(true);

        mMaskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMaskPaint.setStrokeCap(Paint.Cap.ROUND);
        // 接合处 为 圆形
        mMaskPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setDither(true);
        mMaskPaint.setColor(maskColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBg(canvas);
        drawMask(canvas);
    }

    // 画整体背景
    public void drawBg(Canvas canvas) {
        canvas.drawRoundRect(bgRectf, roundCorner, roundCorner, mPaint);
    }

    // 画滑块
    public void drawMask(Canvas canvas) {

        float left = leftOffset;
        float top = intervalWidth;
        float right = mMaskRectFWidth + left;
        float bottom = mMaskRectFHeight + top;

        mMaskRectF.set(left, top, right, bottom);
        canvas.drawOval(mMaskRectF, mMaskPaint);
    }


    public void toggle() {
        boolean isSelect = isSelected();
        updateState(!isSelect);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //手指按下  获取
                preX = event.getRawX();

                clickLeftOffset = event.getX();
                isMove = false;
                //记录原先的 leftOffset
                oldleftOffset = mMaskRectF.left;
                break;
            case MotionEvent.ACTION_MOVE:
                //手指滑动...记录当前x轴坐标
                float culX = event.getRawX();
                //手指滑动距离(当前手指所在x轴坐标减去按下时x轴坐标)
                float dx = culX - preX;
                //当手指滑动5个像素时，我们才认为是真正滑动了
                if (Math.abs(dx) > 5) {
                    isMove = true;
                    //当前
                    leftOffset = (dx + oldleftOffset);
                    setVaildOffset();
                    float percent = leftOffset * 1.0f / mMaxMaskOffset;
                    changeBgColor(percent, closeColor, openColor);
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                //松开手指...
                boolean isShouldCheck = false;
                if (isMove) {//滑动
                    //计算滑块中间位置 是否超过控件的中心位置
                    int pointCenterX = (int) mMaskRectF.centerX();
                    isShouldCheck = pointCenterX >= bgRectf.centerX();
                } else {//点击
                    isShouldCheck = clickLeftOffset >= bgRectf.centerX();
                }
                updateState(isShouldCheck);
                exitAnim();
                isMove = false;
                break;
        }
        return true;
    }

    // 检测偏移是否越界
    public void setVaildOffset() {
        leftOffset = (leftOffset > mMaxMaskOffset ? mMaxMaskOffset : leftOffset);
        leftOffset = (leftOffset < mMinMaskOffset ? mMinMaskOffset : leftOffset);
    }

    // 执行动画
    public void exitAnim() {

        float start = mMaskRectF.left;
        float end = mIsChecked ? mMaxMaskOffset : mMinMaskOffset;
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(start, end);
        //400毫秒
        valueAnimator.setDuration(400);
        //加速再减速
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        //开启动画
        valueAnimator.start();

        //获取松开手指时，背景颜色
        final int startColor = mPaint.getColor();

        //增加动画执行监听 这里就可以每次给你返回执行进度和执行值
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //获取偏移量
                float offset = (Float) animation.getAnimatedValue();
                //当前动画执行进度，这个值是用来以后改变背景颜色的~
                float fraction = animation.getAnimatedFraction();

                if (mIsChecked)
                    //open状态，从当前颜色渐变到open时的颜色
                    changeBgColor(fraction, startColor, openColor);
                else
                    //close状态，从当前颜色渐变到close时的颜色
                    changeBgColor(fraction, startColor, closeColor);
                //赋值
                leftOffset = offset;

                invalidate();
            }

        });
    }

    public interface OnCheckedChangedListener {
        void onCheckedChange(boolean isCheck);
    }

    private OnCheckedChangedListener onCheckedChangedListener;

    public void setOnCheckedChangedListener(OnCheckedChangedListener onCheckedChangedListener) {
        this.onCheckedChangedListener = onCheckedChangedListener;
    }

    //变更当前状态
    private void updateState(boolean checked) {
        setSelected(checked);
        this.mIsChecked = checked;
        //状态监听接口
        if (onCheckedChangedListener != null)
            onCheckedChangedListener.onCheckedChange(this.mIsChecked);
    }

    //颜色插值器
    ArgbEvaluator argbEvaluator = new ArgbEvaluator();

    private void changeBgColor(float fraction, int startColor, int endColor) {
        mPaint.setColor((int) argbEvaluator.evaluate(fraction, startColor, endColor));
    }

    //此方法是将dp值转化为px值，方便适配
    private int dip2px(float dpValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
