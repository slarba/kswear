package com.mlt.kingsongwear;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;

import java.util.Date;

/**
 * TODO: document your custom view class.
 */
public class SpeedometerView extends View {

    private TextPaint mTextPaint;
    private float mTextWidth;
    private float mTextHeight;
    private Paint mArcPaint;
    private TextPaint speedPaint;
    private TextPaint titlePaint;
    private int mBatteryLevel;
    private float mSpeed;
    private TextPaint clockPaint;
    private Date mClock = new Date();
    private Paint mSeparatorLinePaint;
    private TextPaint paramPaint;
    private int mTemperature;
    private float mCurrent;
    private float mVoltage;
    private Paint mPowerArcPaint;

    public SpeedometerView(Context context) {
        super(context);
        init(null, 0);
    }

    public SpeedometerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public SpeedometerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        mBatteryLevel = 80;
        mSpeed = 35.4f;
        mTemperature = 34;
        mCurrent = 5.2f;
        mVoltage = 62.3f;

        mArcPaint = new Paint();
        mArcPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setStrokeWidth(10f);
        mArcPaint.setPathEffect(new DashPathEffect(new float[] { 10,3 }, 0));
        mArcPaint.setAntiAlias(true);
        mArcPaint.setColor(getResources().getColor(R.color.red));

        mPowerArcPaint = new Paint();
        mPowerArcPaint.setStyle(Paint.Style.STROKE);
        mPowerArcPaint.setStrokeWidth(10f);
        mPowerArcPaint.setPathEffect(new DashPathEffect(new float[] { 10,3 }, 0));
        mPowerArcPaint.setAntiAlias(true);
        mPowerArcPaint.setColor(getResources().getColor(R.color.blue));

        speedPaint = createFont(60f);
        titlePaint = createFont(20f);
        titlePaint.setColor(getResources().getColor(R.color.green));
        clockPaint = createFont(40f);

        paramPaint = createFont(30f);

        mSeparatorLinePaint = new Paint();
        mSeparatorLinePaint.setAntiAlias(true);
        mSeparatorLinePaint.setColor(getResources().getColor(R.color.grey));
        mSeparatorLinePaint.setStrokeWidth(2f);
        mSeparatorLinePaint.setStyle(Paint.Style.STROKE);
    }

    private TextPaint createFont(float textSize) {
        TextPaint paint = new TextPaint();
        paint.setAntiAlias(true);
        paint.setTextSize(textSize);
        paint.setColor(getResources().getColor(R.color.white));
        return paint;
    }

    public void refresh() {
        invalidate();
        requestLayout();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // TODO: consider storing these as member variables to reduce
        // allocations per draw cycle.
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int contentWidth = getWidth() - paddingLeft - paddingRight;
        int contentHeight = getHeight() - paddingTop - paddingBottom;

        // Draw the text.
        drawBatteryIndicator(canvas, mBatteryLevel);
        drawSpeed(canvas, mSpeed);
        drawClock(canvas, mClock);

        canvas.drawLine(160f, 100f, 160f, 240f, mSeparatorLinePaint);

        drawBatteryPercentage(canvas, mBatteryLevel);
        drawTemperature(canvas, mTemperature);
        drawCurrent(canvas, mCurrent);
        drawVoltage(canvas, mVoltage);
        drawPower(canvas, mVoltage*mCurrent);
        drawPowerIndicator(canvas, (int)((mVoltage*mCurrent)/15f));
    }

    private void drawBatteryPercentage(Canvas canvas, int mBatteryLevel) {
        canvas.drawText(String.format("%d", mBatteryLevel), 190f, 130f, paramPaint);
        canvas.drawText("%", 245f, 130f, titlePaint);
    }

    private void drawTemperature(Canvas canvas, int temp) {
        canvas.drawText(String.format("%d", temp), 80f, 130f, paramPaint);
        canvas.drawText("\u2103", 120f, 130f, titlePaint);
    }

    private void drawCurrent(Canvas canvas, float amps) {
        canvas.drawText(String.format("%.1f", amps), 57f, 165f, paramPaint);
        canvas.drawText("A", 120f, 165f, titlePaint);
    }

    private void drawVoltage(Canvas canvas, float volts) {
        canvas.drawText(String.format("%.1f", volts), 52f, 200f, paramPaint);
        canvas.drawText("V", 120f, 200f, titlePaint);
    }

    private void drawPower(Canvas canvas, float power) {
        canvas.drawText(String.format("%d", (int)power), 50f, 235f, paramPaint);
        canvas.drawText("W", 120f, 235f, titlePaint);
    }

    private void drawBatteryIndicator(Canvas canvas, int batteryPercentage) {
        canvas.drawArc(new RectF(5f, 5f, 315f, 315f), 90f, Math.max(-180f, -batteryPercentage*1.8f), false, mArcPaint);
    }

    private void drawPowerIndicator(Canvas canvas, int percentage) {
        canvas.drawArc(new RectF(5f, 5f, 315f, 315f), -180f, Math.min(90f,percentage*0.9f), false, mPowerArcPaint);
    }

    private void drawSpeed(Canvas canvas, float speed) {
        canvas.drawText(String.format("%.1f", speed), 90f, 80f, speedPaint);
        canvas.drawText("km/h", 210f, 80f, titlePaint);
    }

    private void drawClock(Canvas canvas, Date clock) {
        canvas.drawText(DateFormat.format("HH:mm", clock).toString(), 110f, 290f, clockPaint);
    }

    public void setVoltage(float voltage) {
        this.mVoltage = voltage;
    }

    public void setSpeed(float speed) {
        this.mSpeed = speed;
    }

    public void setCurrent(float current) {
        this.mCurrent = current;
    }

    public void setBatteryLevel(int batteryLevel) {
        this.mBatteryLevel = batteryLevel;
    }

    public void setTemp(float temp) {
        this.mTemperature = (int)temp;
    }

    public void setClock(Date clock) {
        this.mClock = clock;
    }
}
