package com.windy.example.mycustomviewdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by windog on 2016/7/13.
 */
public class CircleDotLoadingView extends View {
    private int state = STATE_INIT; //开始,加载中,加载成功,加载失败
    private static final int STATE_INIT = 1; //初始化
    private static final int STATE_ING = 2; //加载中
    private int MIN_SIZE;     //默认的宽高
    private float mCircleStrokeWidth;  //圆圈的厚度
    private int mCircleRadius; //圆圈的半径
    private float mDotRadius;    //圆点的半径
    private int mColor;    //主颜色
    private Interpolator mInterpolator; //圆点旋转的插值器
    /*下面是内部变量*/
    Paint circlePaint = new Paint(); //画笔
    Paint dotPaint = new Paint(); //画笔
    private int cx; //圆心坐标
    private int cy;
    private float dotX; //小圆点的x坐标
    private float dotY; //小圆点的y坐标
    private int linearAngle = 0; //线性角度变化值:0-360
    //定时器
    Timer timer = new Timer();
    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            refreshDotPosition();
        }
    };
    public CircleDotLoadingView(Context context) {
        super(context);
        init();
    }
    public CircleDotLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public CircleDotLoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        /*下面的值可以通过自定义属性获得*/
        mCircleStrokeWidth = getContext().getResources().getDimension(R.dimen.circle_stroke_width); //圆圈的厚度和感叹号的厚度
        mColor = getContext().getResources().getColor(R.color.loading_dot);
        MIN_SIZE = DensityUtil.dip2px(getContext(), 72); //这里的dip2px方法就是简单的将72dp转换为本机对应的px,可以去网上随便搜一个
        mDotRadius = getContext().getResources().getDimension(R.dimen.dot_size);
        mInterpolator = new AccelerateDecelerateInterpolator();
        circlePaint.setColor(mColor);
        circlePaint.setAntiAlias(true);
        circlePaint.setStrokeWidth(mCircleStrokeWidth);
        circlePaint.setStyle(Paint.Style.STROKE);
        dotPaint.setAntiAlias(true);
        dotPaint.setColor(mColor);
        dotPaint.setStyle(Paint.Style.FILL);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(MIN_SIZE, MIN_SIZE);
        } else if (widthMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(MIN_SIZE, heightSize);
        } else if (heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSize, MIN_SIZE);
        }
    }
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        /*注意,绘制的坐标是以当前View的左上角为圆点的,而不是当前View的坐标*/
        //圆心坐标计算
        cx = (getWidth() + getPaddingLeft() - getPaddingRight()) / 2;
        cy = (getHeight() + getPaddingTop() + -getPaddingBottom()) / 2;
        //圆圈的半径计算
        int radiusH = (getWidth() - getPaddingRight() - getPaddingLeft()) / 2 - (int) mDotRadius;
        int radiusV = (getHeight() - getPaddingBottom() - getPaddingTop()) / 2 - (int) mDotRadius;
        mCircleRadius = Math.min(radiusV, radiusH);
        //初始化小圆点位置坐标
        dotX = cx;
        dotY = getPaddingTop() + mDotRadius * 2;
    }
    @Override
    protected void onDraw(Canvas canvas) {
        //画圆圈
        canvas.drawCircle(cx, cy, mCircleRadius, circlePaint);
        //画小圆
        if (state == STATE_ING) {
            canvas.drawCircle(dotX, dotY, mDotRadius, dotPaint);
        }
    }
    public void startLoading() {
        timer.schedule(timerTask, 10, 3); //延时10ms,每3ms触发一次
        state = STATE_ING;
    }
    public void stopLoading() {
        timer.cancel();
    }
    /**
     * 刷新dot位置
     */
    private void refreshDotPosition() {
        final float input = (linearAngle % 360.0F);
        float f = mInterpolator.getInterpolation(input / 360.0F);
        double realAngle = f * 2 * Math.PI; //真实的角度
        dotX = cx + (float) (mCircleRadius * Math.sin(realAngle));
        dotY = cy - (float) (mCircleRadius * Math.cos(realAngle));
        postInvalidate();
        linearAngle = linearAngle + 1;
    }
}

