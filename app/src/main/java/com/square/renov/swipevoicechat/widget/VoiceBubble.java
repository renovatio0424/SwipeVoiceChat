package com.square.renov.swipevoicechat.widget;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Build;
import android.speech.tts.Voice;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TableRow;
import android.widget.TextView;

import com.square.renov.swipevoicechat.Activity.ChatActivity;
import com.square.renov.swipevoicechat.Activity.RecordActivity;
import com.square.renov.swipevoicechat.R;
import com.square.renov.swipevoicechat.Util.Utils;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class VoiceBubble extends TableRow {
    private static final String TAG = VoiceBubble.class.getSimpleName();
    @BindView(R.id.chat_bubble)
    ConstraintLayout chatBubble;
    @BindView(R.id.button_play)
    ImageButton playButton;
    @BindView(R.id.progressbar)
    SeekBar progressBar;
    @BindView(R.id.play_time)
    TextView tvPlayTime;

    String voiceFileUrl;
    int playTime;

    public void setPlayTime(int playTime) {
        this.playTime = playTime;
    }

    public interface VoicePlayListener {
        void onPlay();

        void onResume();

        void onPause();

        void onStopPlay();
    }

    private VoicePlayListener voicePlayListener;

    public void setVoicePlayListener(VoicePlayListener voicePlayListener) {
        this.voicePlayListener = voicePlayListener;
    }

    public static final int STATE_PLAY = 1;
    public static final int STATE_STOP = 2;
    private int mState = 2;

    public int getVoiceBubbleState() {
        return mState;
    }

    Unbinder unbinder;
    Context context;

    BubbleTimerTask timerTask;
    Timer timer;
    int startTime = 0;

    public VoiceBubble(Context context) {
        super(context);
        this.context = context;
        initView();
    }

    public void setVoiceFileUrl(String voiceFileUrl) {
        this.voiceFileUrl = voiceFileUrl;

        tvPlayTime.setText(Utils.getPlayTimeFormat(playTime));
        progressBar.setMax(playTime / 1000 * 1000);

        Log.e(TAG, "playTime: " + playTime);
        Log.e(TAG, "tv: " + Utils.getPlayTimeFormat(playTime));
    }

    public VoiceBubble(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
        getAttrs(attrs);
    }

    private void initView() {
        String infService = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(infService);
        View view = layoutInflater.inflate(R.layout.bubble_voice, this, false);
        unbinder = ButterKnife.bind(this, view);
        addView(view);

        playButton.setOnClickListener(v -> {
            switch (mState) {
                case STATE_PLAY:
                    mState = STATE_STOP;
                    //뷰 초기화
                    playButton.setImageResource(R.drawable.ic_play);

                    this.post(() -> {
                        tvPlayTime.setText(Utils.getPlayTimeFormat(playTime));
                    });
                    if (voicePlayListener != null) {
                        voicePlayListener.onPause();
                        if (timer != null)
                            timerTask.pause();
                    }

                    break;

                case STATE_STOP:
                    mState = STATE_PLAY;
                    playButton.setImageResource(R.drawable.ic_pause);
                    if (voicePlayListener != null) {
                        if (timerTask != null && timerTask.isPause()) {
                            timerTask.resume();
                            voicePlayListener.onResume();
                        } else {
                            voicePlayListener.onPlay();
                        }
                    }
                    if (timer == null) {
                        timerTask = new BubbleTimerTask(playTime);
                        timer = new Timer();
                        timer.schedule(timerTask, 0, 100);
                    }
                    break;
            }

        });
    }
    private void getAttrs(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.VoiceBubble);
        setTypeArray(typedArray);
    }

    private void setTypeArray(TypedArray typedArray) {
        int background_res_ID = typedArray.getResourceId(R.styleable.VoiceBubble_bg, R.drawable.my_bubble_line);
        chatBubble.setBackgroundResource(background_res_ID);
        int progress_color = typedArray.getResourceId(R.styleable.VoiceBubble_progress_tint, R.color.other_progress_color);
        int progress_background_color = typedArray.getResourceId(R.styleable.VoiceBubble_progress_background_tint, R.color.other_progress_background_color);
        int image_tint_color = typedArray.getResourceId(R.styleable.VoiceBubble_play_button_tint, R.color.other_play_button_tint);
        int text_color = typedArray.getResourceId(R.styleable.VoiceBubble_time_color, R.color.other_time_color);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            progressBar.setProgressTintList(ColorStateList.valueOf(getResources().getColor(progress_color)));
            progressBar.setProgressBackgroundTintList(ColorStateList.valueOf(getResources().getColor(progress_background_color)));
            playButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(image_tint_color)));
        }
        playButton.setMinimumWidth(Utils.dpToPx(33));
        playButton.setMinimumHeight(Utils.dpToPx(33));
        typedArray.recycle();
    }


public class BubbleTimerTask extends TimerTask {
    long maxRecordTime = 30 * 1000;
    long recordTime;
    long time = 0L;
    boolean isPause = false;

    public BubbleTimerTask(@Nullable long myRecordTime) {
        this.recordTime = myRecordTime;
    }


    /**
     * record time == 0 -> 아직 녹음을 안한 상태에는 녹음 타이머
     * record time != 0 -> 녹음을 한상태
     * time >= recordTime -> 재생이 끈난 상태
     * else -> 재생중
     */
    @Override
    public void run() {
        if (isPause) {
            return;
        }

        if (startTime >= playTime) {
            timer.cancel();
            startTime = 0;
            playButton.post(() -> playButton.setImageResource(R.drawable.ic_reload_white));
            tvPlayTime.post(() -> tvPlayTime.setText(Utils.getPlayTimeFormat(playTime)));
            progressBar.setProgress(0);
            timer = null;
            mState = STATE_STOP;
            return;
        }

        Log.e(TAG, "start time: " + startTime);
        Log.e(TAG, "play time: " + playTime);

        progressBar.post(() -> {
            progressBar.setProgress(startTime);
        });

        tvPlayTime.post(() -> {
            tvPlayTime.setText(Utils.getPlayTimeFormat(startTime));
        });

        startTime += 100;
    }

    public void pause() {
        isPause = true;
    }

    public void resume() {
        isPause = false;
    }

    public boolean isPause() {
        return isPause;
    }
}


}
