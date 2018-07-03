package com.square.renov.swipevoicechat.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.square.renov.swipevoicechat.R;

public class VoicePlayerView extends TintedImageButton {

    private ObjectAnimator animator;

    public void setJustPlay(boolean justPlay) {
        this.justPlay = justPlay;
    }

    public interface VoiceRecordListener {
        void onRecord();
        void onStopRecord();
        void onPlay();
        void onStopPlay();
    }

    public static final int STATE_PREPARE = 0;
    public static final int STATE_RECORD = 1;
    public static final int STATE_PLAY = 2;
    public static final int STATE_STOP = 3;
    private int mState;

    private VoiceRecordListener voiceRecordListener;
    private Paint mBackgroundPaint;
    private float mBackgroundProgressWidth;
    private Paint mProgressPaint;
    private float mProgressWidth;
    private Paint mInCirclePaint;

    private float mProgressOffset;
    private Path drawPath;
    private RectF arcBounds;

    private int progressBgColor;
    private int progressColor;

    private int drawableRecord;
    private int drawablePlay;
    private int drawableStop;

    private boolean justPlay = false;

    public VoicePlayerView(Context context) {
        super(context);
        init(context, null);
    }

    public VoicePlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public VoicePlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init(Context context, AttributeSet attrs) {
//        progressBgColor = ResourcesCompat.getColor(getResources(), R.color.story_progress_bg, null);
//        progressColor = ResourcesCompat.getColor(getResources(), R.color.story_progress, null);
//
//        mBackgroundPaint = new Paint();
//        mBackgroundPaint.setAntiAlias(true);
//        mBackgroundPaint.setStyle(Paint.Style.STROKE);
//        mBackgroundPaint.setColor(progressColor);
//
//        mProgressPaint = new Paint();
//        mProgressPaint.setAntiAlias(true);
//        mProgressPaint.setStyle(Paint.Style.STROKE);
//        mProgressPaint.setStrokeWidth(px1dp);

        setMainColor(R.color.main_color);
        setBackgroundProgress(progressBgColor,10);
        setProgress(progressColor, 10);
        setInCircle();
        setRecordImage(R.drawable.ic_mic);
        setPlayImage(R.drawable.ic_play);
        setStopImage(R.drawable.ic_stop);

        drawPath = new Path();

//        setOnTouchListener((v, event) -> {
//            if(event.getAction() == MotionEvent.ACTION_DOWN){
//                Log.d("VoicePlayerView", "Action Down");
//                if(mState == STATE_PREPARE){
//                    mState = STATE_RECORD;
//
//                    if (voiceRecordListener != null) {
//                        voiceRecordListener.onRecord();
//                    }
//
//                    setImageResource(R.drawable.ic_stop);
//                } else if(mState == STATE_STOP){
//                    mState = STATE_PLAY;
//                    setImageResource(R.drawable.ic_stop);
//                    if (voiceRecordListener != null) {
//                        voiceRecordListener.onPlay();
//                    }
//                }
//            } else if(event.getAction() == MotionEvent.ACTION_UP){
//                Log.d("VoicePlayerView", "Action Up");
//                if(mState == STATE_RECORD){
//                    mState = STATE_STOP;
//                    setImageResource(R.drawable.ic_play);
//                    stopRecordProgress();
//                } else if(mState == STATE_PLAY){
//                    mState = STATE_STOP;
//                    setImageResource(R.drawable.ic_play);
//                    stopPlayProgress();
//                }
//            }
//
//            return false;
//        });

//        setOnLongClickListener(v -> {
//            switch (mState) {
//                case STATE_PREPARE:
//                    mState = STATE_RECORD;
//
//                    if (voiceRecordListener != null) {
//                        voiceRecordListener.onRecord();
//                    }
//
//                    setImageResource(R.drawable.ic_stop);
//
//                    break;
//                case STATE_RECORD:
//                    mState = STATE_STOP;
//                    setImageResource(R.drawable.ic_play);
//                    stopRecordProgress();
//                    break;
//                case STATE_STOP:
//                    mState = STATE_PLAY;
//                    setImageResource(R.drawable.ic_stop);
//                    if (voiceRecordListener != null) {
//                        voiceRecordListener.onPlay();
//                    }
//                    break;
//                case STATE_PLAY:
//                    mState = STATE_STOP;
//                    setImageResource(R.drawable.ic_play);
//                    stopPlayProgress();
//                    break;
//
//
//            }
//            return true;
//        });

        setOnClickListener(view -> {

            switch (mState) {
                case STATE_PREPARE:
                    mState = STATE_RECORD;

                    if (voiceRecordListener != null) {
                        voiceRecordListener.onRecord();
                        startRecordTime = System.currentTimeMillis();
                    }

                    setImageResource(R.drawable.ic_stop);

                    break;
                case STATE_RECORD:
                    endRecordTime = System.currentTimeMillis();
                    if(endRecordTime - startRecordTime < 3000){
                        Toast.makeText(getContext(), "3초 이상 녹음해주세요", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    mState = STATE_STOP;
                    setImageResource(R.drawable.ic_play);
                    stopRecordProgress();
                    break;
                case STATE_STOP:
                    mState = STATE_PLAY;
                    setImageResource(R.drawable.ic_stop);
                    if (voiceRecordListener != null) {
                        voiceRecordListener.onPlay();
                    }
                    break;
                case STATE_PLAY:
                    mState = STATE_STOP;
                    setImageResource(R.drawable.ic_play);
                    stopPlayProgress();
                    break;


            }
        });

        if(!justPlay)
            resetVoiceRecord();
        else
            prepareVoicePlay();
    }

    private void setMainColor(int mainColor) {
        setColorFilter(ResourcesCompat.getColor(getResources(), mainColor, null));
        progressBgColor = ResourcesCompat.getColor(getResources(), R.color.story_progress_bg, null);
        progressColor = ResourcesCompat.getColor(getResources(), mainColor, null);

    }

    public void setRecordImage(int ico_record) {
        this.drawableRecord = ico_record;
    }

    public void setPlayImage(int ico_play){
        this.drawablePlay = ico_play;
    }

    public void setStopImage(int ico_stop){
        this.drawableStop = ico_stop;
    }


    public void setBackgroundProgress(int colorId, float width){
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setAntiAlias(true);
        mBackgroundPaint.setStyle(Paint.Style.STROKE);
        mBackgroundPaint.setColor(colorId);
        mBackgroundProgressWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, width, getResources().getDisplayMetrics());
        mBackgroundPaint.setStrokeWidth(mBackgroundProgressWidth);
    }

    public void setProgress(int colorId, float width){
        mProgressPaint = new Paint();
        mProgressPaint.setAntiAlias(true);
        mProgressPaint.setStyle(Paint.Style.STROKE);
        mProgressPaint.setColor(colorId);
        mProgressWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, width, getResources().getDisplayMetrics());
        mProgressPaint.setStrokeWidth(mProgressWidth);
    }

    public void setInCircle(){
        mInCirclePaint = new Paint();
        mInCirclePaint.setAntiAlias(true);
        mInCirclePaint.setStyle(Paint.Style.FILL);
        mInCirclePaint.setColor(ResourcesCompat.getColor(getResources(), R.color.white, null));
        mInCirclePaint.setStrokeWidth(mProgressWidth);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        arcBounds = new RectF(0, 0, w, h);
        arcBounds.inset(mProgressWidth,mProgressWidth);
//        arcBounds.inset(px1dp, px1dp);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        float centerX = (float) arcBounds.centerX();
        float centerY = (float) arcBounds.centerY();

        if (mState == STATE_PREPARE) {
//            canvas.drawCircle(centerX + px1dp/2, centerY + px1dp/2, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 72.5f, getResources().getDisplayMetrics()), mBackgroundPaint);
//            canvas.drawCircle(centerX, centerY, arcBounds.centerX() - px1dp, mBackgroundPaint);
//            canvas.drawCircle(centerX, centerY, arcBounds.centerX() - mBackgroundProgressWidth, mInCirclePaint);
//            canvas.drawCircle(centerX, centerY, arcBounds.centerX() - mBackgroundProgressWidth, mBackgroundPaint);
//            return;
        }

        canvas.drawCircle(centerX, centerY, arcBounds.centerX() - mBackgroundProgressWidth, mInCirclePaint);
        canvas.drawCircle(centerX, centerY, arcBounds.centerX() - mBackgroundProgressWidth, mBackgroundPaint);

        if (mProgressOffset > 0) {
            PointF startPoint = pointOnCircle(width / 2 - mProgressWidth, 0.f, centerX, centerY);
//            PointF startPoint = pointOnCircle(arcBounds.centerX() - mBackgroundProgressWidth, 0.f, centerX, centerY);

            drawPath.reset();
            drawPath.arcTo(arcBounds, -90.f, 360.f * mProgressOffset);
            drawPath.moveTo(startPoint.x, startPoint.y);
            drawPath.close();

            mProgressPaint.setColor(progressColor);
            canvas.drawPath(drawPath, mProgressPaint);
        }

        super.onDraw(canvas);
    }

    /**
     * 3시 방향이 0도, 12시 방향이 90도
     */
    private PointF pointOnCircle(float radius, float angleInDegrees, float centerX, float centerY) {
        // Convert from degrees to radians via multiplication by PI/180
        float x = (float)(radius * Math.cos(angleInDegrees * Math.PI / 180F) + centerX);
        float y = (float)(radius * Math.sin(angleInDegrees * Math.PI / 180F) + centerY);

        return new PointF(x, y);
    }

    public void setProgress(float progress) {
        if (progress > 1.f) {
            progress -= 1.f;
        }
        this.mProgressOffset = progress;
        postInvalidateOnAnimation();
    }

    public void setVoiceRecordListener(VoiceRecordListener voiceRecordListener) {
        this.voiceRecordListener = voiceRecordListener;
    }

    long startRecordTime, endRecordTime;

    public void startRecordProgress(long duration) {


        animator = ObjectAnimator.ofFloat(this, "progress", 0.f, 1.f);
        animator.setDuration(duration);
        animator.setInterpolator(new LinearInterpolator());
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mState = STATE_STOP;
                setImageResource(drawablePlay);
                if (voiceRecordListener != null) {
                    voiceRecordListener.onStopRecord();
                }

                postInvalidateOnAnimation();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                setProgress(0.f);
            }
        });

        animator.start();
    }

    public void startVoicePlayProgress(long duration) {
        animator = ObjectAnimator.ofFloat(this, "progress", 0.f, 1.f);
        animator.setDuration(duration);
        animator.setInterpolator(new LinearInterpolator());
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mState = STATE_STOP;
                setImageResource(drawablePlay);
                if (voiceRecordListener != null) {
                    voiceRecordListener.onStopPlay();
                }

                postInvalidateOnAnimation();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                setProgress(0.f);
            }
        });

        animator.start();
    }

    public void stopRecordProgress() {
        setProgress(0.f);
        if (animator != null) {
            animator.cancel();
        }
    }

    public void stopPlayProgress() {
        setProgress(0.f);
        if (animator != null) {
            animator.cancel();
        }
    }

    public void resetVoiceRecord() {
        mState = STATE_PREPARE;
//        setImageDrawable(getResources().getDrawable(drawableRecord));
        setImageResource(drawableRecord);
    }

    public void prepareVoicePlay() {
        mState = STATE_STOP;
        setImageResource(drawablePlay);
    }

    public void prepareVoiceStop() {
        mState = STATE_STOP;
        setImageResource(drawableStop);
    }

    public int getState(){
        return this.mState;
    }
}
