package com.square.renov.swipevoicechat.widget;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
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


    public int getAmplitude() {
        return this.mRecorder.getMaxAmplitude();
    }

    public MediaPlayer getmPlayer() {
        return mPlayer;
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
        if(mRecorder == null){
            mRecorder = new MediaRecorder();
            mFileName = context.getExternalCacheDir().getAbsolutePath();
            mFileName += VOICE_FILE_NAME;

            mRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mRecorder.setOutputFile(mFileName);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
//            mRecorder.setAudioEncoder(MediaRecorder.getAudioSourceMax());
            //TODO : 없었던 부분 -> IOS 호환성 문제 있을 경우 제거
            mRecorder.setAudioEncodingBitRate(96000);
            mRecorder.setAudioSamplingRate(44100);
        }

        try {
            mRecorder.prepare();
            mRecorder.start();
        } catch (Exception e) {
//            Log.e(LOG_TAG, "prepare() failed");
            Log.e(TAG, null, e);
        }


    }

    public void voiceRecordStop() {
        try {
            mRecorder.stop();
            mRecorder.reset();
            mRecorder.release();
            mRecorder = null;
        } catch (RuntimeException e){
            e.printStackTrace();
        }
//        mRecorder.release();

    }

    public int voicePlay(Context context) {
        mFileName = context.getExternalCacheDir().getAbsolutePath();
        mFileName += VOICE_FILE_NAME;

        return voicePlay(mFileName);
    }

    public int getPlayTime(String filePath, Context context) throws IOException {
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
        }
        mPlayer.reset();

        mPlayer.setDataSource(filePath);
        mPlayer.prepare();
        int time = mPlayer.getDuration();
        mPlayer.release();
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
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }

    public void voicePlayResume() {
        isPlay.set(true);
        mPlayer.start();
    }


    public void voicePlayPause() {
        isPlay.set(false);
        mPlayer.pause();
    }

    public boolean isPlay() {
        return isPlay.get();
    }
}
