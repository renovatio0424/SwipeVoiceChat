package com.square.renov.swipevoicechat.widget;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class VoicePlayerManager {
    private static final String TAG = "VoicePlayerManager";
    private static final String VOICE_FILE_NAME = "/audiorecordtest.m4a";

    private static volatile VoicePlayerManager instance;
    private String mFileName;
    private MediaPlayer mPlayer;
    private MediaRecorder mRecorder;

    private AtomicBoolean isPlay = new AtomicBoolean(false);

    private VoicePlayerManager() {
    }


    public int getAmplitude(){
        return this.mRecorder.getMaxAmplitude();
    }

    public static VoicePlayerManager getInstance() {
        if (instance == null) {
            synchronized (VoicePlayerManager.class) {
                if (instance == null) {
                    VoicePlayerManager temp = new VoicePlayerManager();
                    instance = temp;
                }
            }
        }

        return instance;
    }

    public String getFileName() {
        return mFileName;
    }

    public void voiceRecord(Context context) {
        mRecorder = new MediaRecorder();

        mFileName = context.getExternalCacheDir().getAbsolutePath();
        mFileName += VOICE_FILE_NAME;

        mRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        try {
            mRecorder.setOutputFile(mFileName);
            mRecorder.prepare();
        } catch (Exception e) {
//            Log.e(LOG_TAG, "prepare() failed");
            Log.e(TAG, null, e);
        }

        mRecorder.start();
    }

    public void voiceRecordStop() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }

    public int voicePlay(Context context) {
        mFileName = context.getExternalCacheDir().getAbsolutePath();
        mFileName += VOICE_FILE_NAME;

        return voicePlay(mFileName);
    }

    public int getPlayTime(String filePath) throws IOException {
        if(mPlayer == null){
            mPlayer = new MediaPlayer();
        }
        mPlayer.setDataSource(filePath);
        int time = mPlayer.getDuration();
        mPlayer = null;
        return time;
    }

    public int voicePlay(String filePath) {
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
        }

        if (isPlay.getAndSet(true)) {
            return -1;
        }

        mPlayer.setOnCompletionListener(mediaPlayer -> {
            mediaPlayer.reset();
            isPlay.set(false);
            Log.e(TAG, "onCompletion()");
        });

        try {
            mPlayer.reset();
//          string이 uri값일 경우
            mPlayer.setDataSource(filePath);

            mPlayer.prepare();
            Log.e(TAG, "onPrepared()");
            mPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "prepare() failed: ");
        }

        return mPlayer.getDuration();
    }

    public void voicePlayStop() {
        isPlay.set(false);

        mPlayer.stop();
        mPlayer.release();
        mPlayer = null;
    }
}
