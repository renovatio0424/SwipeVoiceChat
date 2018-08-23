package com.square.renov.swipevoicechat.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.DefaultRetryPolicy;
import com.square.renov.swipevoicechat.Event.RefreshEvent;
import com.square.renov.swipevoicechat.Model.VoiceCard;
import com.square.renov.swipevoicechat.Model.VoiceChatRoom;
import com.square.renov.swipevoicechat.Network.NetRetrofit;
import com.square.renov.swipevoicechat.Network.network.RequestManager;
import com.square.renov.swipevoicechat.Network.network.VolleyMultipartRequest;
import com.square.renov.swipevoicechat.R;
import com.square.renov.swipevoicechat.Util.DialogUtils;
import com.square.renov.swipevoicechat.Util.ImageUtil;
import com.square.renov.swipevoicechat.Util.SharedPrefHelper;
import com.square.renov.swipevoicechat.Util.Utils;
import com.square.renov.swipevoicechat.widget.VoicePlayerManager;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;

public class RecordActivity extends AppCompatActivity {

    private static final String TAG = RecordActivity.class.getSimpleName();
    @BindView(R.id.tv_record_title)
    TextView recordTitle;
    @BindView(R.id.ib_record_btn)
    ImageButton recordButton;
    @BindView(R.id.ib_re_record_btn)
    ImageButton reRecordButton;
    @BindView(R.id.ib_send_btn)
    ImageButton sendButton;
    @BindView(R.id.tv_record_time)
    TextView tvRecordTime;
    @BindView(R.id.progressbar)
    SeekBar mProgressbar;
    @BindView(R.id.tv_start_time)
    TextView tvStartTime;
    @BindView(R.id.tv_end_time)
    TextView tvEndTime;
    @BindView(R.id.tv_record_state)
    TextView recordState;
    @BindView(R.id.tv_record_example)
    TextView recordExample;
    @BindView(R.id.iv_volume)
    ImageView ivVolume;
    @BindView(R.id.iv_volume_shadow)
    ImageView ivShadow;
    int chatId;
    Unbinder unbinder;

    int playTime;


    public interface VoiceRecordListener {
        void onRecord();

        void onStopRecord();

        void onPlay();

        void onStopPlay();

        void onPause();

        void onResume();
    }

    private VoiceRecordListener voiceRecordListener = new VoiceRecordListener() {
        @Override
        public void onRecord() {
            VoicePlayerManager.getInstance().voiceRecord(getApplicationContext());
        }

        @Override
        public void onStopRecord() {
            VoicePlayerManager.getInstance().voiceRecordStop();
            playTime = VoicePlayerManager.getInstance().voicePlay(getApplicationContext());
            VoicePlayerManager.getInstance().voicePlayStop();
        }

        @Override
        public void onPlay() {
            VoicePlayerManager.getInstance().voicePlay(getApplicationContext());
        }

        @Override
        public void onStopPlay() {
            VoicePlayerManager.getInstance().voicePlayStop();
        }

        @Override
        public void onPause() {
            VoicePlayerManager.getInstance().voicePlayPause();
        }

        @Override
        public void onResume() {
            VoicePlayerManager.getInstance().voicePlayResume();
        }
    };

    int CURRENT_STATE = 0;
    final int STATE_PREPARE = 0;
    final int STATE_RECORD = 1;
    final int STATE_PLAY = 2;
    final int STATE_STOP = 3;

    long startRecordTime, endRecordTime;
    private boolean reply;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        unbinder = ButterKnife.bind(this);

        if (getIntent().hasExtra("chatId")) {
            chatId = getIntent().getIntExtra("chatId", -1);
            if (chatId == -1)
                recordTitle.setText("새 이야기");
            else
                recordTitle.setText("답장하기");
        }

        reply = false;

        tvRecordTime.setText("00:00 / 00:00");
        recordExample.setText(Html.fromHtml(recordExample.getText().toString()));
        setProgressbar();
    }

    private void setProgressbar() {
        mProgressbar.setOnTouchListener((v, event) -> true);
    }


    /**
     * 녹음 프로세스
     * PREPARE -> RECORD -> STOP -> PLAY -> STOP -> PREPARE
     */
    MyTimerTask mtask;
    Timer recordTimer;
    Timer playTimer;

    public void stopTimer() {
        if (mtask != null) {
            mtask.cancel();
            mtask = null;
        }

        if (recordTimer != null) {
            recordTimer.cancel();
            recordTimer.purge();
            recordTimer = null;
        }

        if (playTimer != null) {
            playTimer.cancel();
            playTimer.purge();
            playTimer = null;
        }
    }

    @OnClick(R.id.ib_record_btn)
    public void onClickRecord() {
        switch (CURRENT_STATE) {
            case STATE_PREPARE:
                CURRENT_STATE = STATE_RECORD;
                if (voiceRecordListener != null) {
                    voiceRecordListener.onRecord();
                    startRecordTime = System.currentTimeMillis();
                }
                //TODO START RECORD VIEW
                setRecordView();

                mtask = new MyTimerTask(0L);
                recordTimer = new Timer();
                recordTimer.schedule(mtask, 0, 10);
                break;

            case STATE_RECORD:
                endRecordTime = System.currentTimeMillis();
                if (endRecordTime - startRecordTime < 3000) {
                    Toast.makeText(this, "3초 이상 녹음해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                CURRENT_STATE = STATE_STOP;

                if (voiceRecordListener != null) {
                    voiceRecordListener.onStopRecord();
                }
                //TODO STOP RECORD VIEW
                setRecordView();
                stopTimer();
                break;

            case STATE_STOP:
                CURRENT_STATE = STATE_PLAY;

                startRecordTime = 0L;

                //TODO START PLAY VIEW
                setRecordView();

                if (mtask == null) {
                    if (voiceRecordListener != null) {
                        voiceRecordListener.onPlay();
                    }
                    mtask = new MyTimerTask(playTime);
                    playTimer = new Timer();
                    playTimer.schedule(mtask, 0, 10);
                } else if (mtask.isPause()) {
                    if (voiceRecordListener != null) {
                        voiceRecordListener.onResume();
                    }
                    mtask.resume();
                }

                break;

            case STATE_PLAY:
                CURRENT_STATE = STATE_STOP;
                if (voiceRecordListener != null) {
                    voiceRecordListener.onPause();
                }
                //TODO STOP PLAY VIEW
                setRecordView();
                if (mtask != null)
                    mtask.pause();
//                stopTimer();
                break;

            default:
                Toast.makeText(this, "녹음 할 수 없습니다.", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @OnClick(R.id.ib_re_record_btn)
    public void onClickPlay() {
        switch (CURRENT_STATE) {
            case STATE_STOP:
                CURRENT_STATE = STATE_PREPARE;
                setRecordView();
                tvRecordTime.setText("00:00 / 30:00");
                sendButton.setImageResource(R.drawable.send_inactivated);
                break;

            default:
                Toast.makeText(this, "녹음을 먼저 해주세요", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @OnClick(R.id.ib_send_btn)
    public void onClickSend() {
        //재생 정지일때
        if (CURRENT_STATE != STATE_STOP) {
            Toast.makeText(this, "아직 보낼 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

//

        if (chatId == -1) {
            getUploadVoiceInfoAndUploadVoice();
        } else {
            //TODO : 루나 차감 미구현으로 주석
            MaterialDialog reportDialog = new MaterialDialog.Builder(this)
                    .customView(R.layout.dialog_code, false)
                    .show();

            DialogUtils.initDialogView(reportDialog, this);

            TextView tvTitle = (TextView) reportDialog.findViewById(R.id.tv_title);
            TextView tvContent = (TextView) reportDialog.findViewById(R.id.tv_content);
            TextView tvConfirm = (TextView) reportDialog.findViewById(R.id.tv_send_code);
            TextView tvCancel = (TextView) reportDialog.findViewById(R.id.tv_cancel);
            EditText etInviteCode = (EditText) reportDialog.findViewById(R.id.et_code);

            etInviteCode.setVisibility(View.GONE);

            tvTitle.setText("답장을 보내시겠습니까?");
            tvContent.setText(Html.fromHtml(getString(R.string.dialog_reply_content)));
            tvConfirm.setText("확인");
            tvConfirm.setEnabled(true);
            tvConfirm.setBackgroundResource(R.drawable.button_text_background);
            tvConfirm.setTextColor(getResources().getColorStateList(R.color.button_text_color));
            tvConfirm.setOnClickListener(v -> {
                reportDialog.dismiss();
                if (Utils.haveEnoughReplyLuna(this, 2))
                    getUploadVoiceInfoAndUploadVoice();
            });

            tvCancel.setText("취소");
            tvCancel.setOnClickListener(v -> {
                reportDialog.dismiss();
            });
        }
    }

    @OnClick(R.id.ib_close)
    public void onClickClose() {
        finish();
    }

    private void getUploadVoiceInfoAndUploadVoice() {
        File voiceFile = new File(VoicePlayerManager.getInstance().getFileName());

        if (voiceFile == null) {
            Toast.makeText(this, "녹음을 먼저 해주세요!", Toast.LENGTH_SHORT).show();
            return;
        }

        int fileSize = (int) voiceFile.length();

        Call<Map> call = NetRetrofit.getInstance(getApplicationContext()).getService().getUploadMetaData("voice", fileSize);
        call.enqueue(new Callback<Map>() {
            @Override
            public void onResponse(Call<Map> call, retrofit2.Response<Map> response) {

                if (response.isSuccessful()) {
                    uploadVoice(response.body(), voiceFile);
//                        fileName = fileName.replace(".jpg", "");


//                        MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", fileName, RequestBody.create(MediaType.parse("image/*"), imageFile));
//                        MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", fileName, RequestBody.create(MediaType.parse("image/*"), imageFile));
//                        response.body().put("file", imageFile);

//                        Call<String> request = FileUploadRetrofit.getInstance(getApplicationContext()).getService().upload(key, response.body(), filePart);
//                        request.enqueue(new Callback<String>() {
//                            @Override
//                            public void onResponse(Call<String> call, Response<String> response) {
//                                if(response.isSuccessful()){
//                                    Log.d(TAG,"body: " + response.body());
//                                } else {
//                                    try {
//                                        Log.d(TAG, "error code: " + response.code() + " error body: " + response.errorBody().string());
//                                    } catch (IOException e) {
//                                        e.printStackTrace();
//                                    }
//                                }
//                            }
//
//                            @Override
//                            public void onFailure(Call<String> call, Throwable t) {
//
//                            }
//                        });
                } else {
                    try {
                        Utils.toastError(getApplicationContext(), response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<Map> call, Throwable t) {

            }
        });
    }

    public void uploadVoice(Map updateInfo, File temptFile) {
        final String uploadImagePath = "https://" + updateInfo.get("Host") + "/" + updateInfo.get("key");

        String filePath = ImageUtil.getFilePathFromUri(getTempUri(temptFile), getApplicationContext());
        if (filePath == null || "".equals(filePath)) {
            return;
        }

        MaterialDialog progressDialog = new MaterialDialog.Builder(RecordActivity.this)
                .content("이야기를 전송중 입니다 . . .")
                .progress(true, 0)
                .show();


        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest("https://hellovoicebucket.s3.amazonaws.com",
                response -> {
                    Log.d(TAG, "onResponse : " + response);

                    progressDialog.dismiss();

                    //TODO START VOICE @PARAM URL = uploadImagepath
                    if (chatId != -1) {
                        int duration = 0;
                        try {
                            duration = VoicePlayerManager.getInstance().getPlayTime(uploadImagePath, getApplicationContext());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Call<VoiceChatRoom> replyRequest = NetRetrofit.getInstance(getApplicationContext()).getService().makeVoiceChatRoom(chatId, uploadImagePath, duration);
                        replyRequest.enqueue(new Callback<VoiceChatRoom>() {
                            @Override
                            public void onResponse(Call<VoiceChatRoom> call, retrofit2.Response<VoiceChatRoom> response) {
                                if (response.isSuccessful()) {
                                    Toast.makeText(RecordActivity.this, "답장을 성공적으로 보냈습니다!", Toast.LENGTH_SHORT).show();
                                    SharedPrefHelper.getInstance(getApplicationContext()).setSharedPreferences(SharedPrefHelper.CHAT_ROOM_DATA_UPDATE_TIME, 0L);
                                    SharedPrefHelper.getInstance(getApplicationContext()).setSharedPreferences(SharedPrefHelper.NEW_CHAT_ROOM_ID, response.body().getId());
                                    EventBus.getDefault().post(new RefreshEvent(RefreshEvent.Action.STATUS_CHANGE, response.body().getId()));
                                    reply = true;

                                    //루나 갱신
                                    Utils.refreshMyInfo(getApplicationContext());

                                    Intent returnIntent = getIntent();
                                    returnIntent.putExtra("chatRoomId", response.body().getId());
                                    setResult(RESULT_OK, returnIntent);
                                    finish();
                                } else {
                                    try {
                                        Utils.toastError(getApplicationContext(), response);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<VoiceChatRoom> call, Throwable t) {
                                t.printStackTrace();
                            }
                        });
                    } else {
                        int duration = 0;
                        try {
                            duration = VoicePlayerManager.getInstance().getPlayTime(uploadImagePath, getApplicationContext());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Call<VoiceCard> newVoiceRequest = NetRetrofit.getInstance(getApplicationContext()).getService().sendNewVoice(uploadImagePath, duration);
                        newVoiceRequest.enqueue(new Callback<VoiceCard>() {
                            @Override
                            public void onResponse(Call<VoiceCard> call, retrofit2.Response<VoiceCard> response) {
                                if (response.isSuccessful()) {
                                    Toast.makeText(RecordActivity.this, "새 이야기를 성공적으로 보냈습니다!", Toast.LENGTH_SHORT).show();
                                    Intent returnIntent = getIntent();
                                    setResult(RESULT_OK, returnIntent);
                                    finish();
                                } else {
                                    try {
                                        Utils.toastError(getApplicationContext(), response);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<VoiceCard> call, Throwable t) {
                                t.printStackTrace();
                            }
                        });
                    }
                },
                error -> {
                    Log.e(TAG, "onErrorResponse : " + error.getMessage());
                    Toast.makeText(RecordActivity.this, "다시 보내주세요", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                });

        updateInfo.remove("Host");
        updateInfo.remove("uploadImagePath");
        multipartRequest.addStringParams(updateInfo);
        multipartRequest.addAttachment(VolleyMultipartRequest.MEDIA_TYPE_JPEG, "file", new File(filePath));
        multipartRequest.buildRequest();
        multipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestManager.addRequest(multipartRequest, "ProfileMultipart");
    }

    private Uri getTempUri(File temptFile) {
        Uri uri = null;
        try {
            uri = Uri.fromFile(temptFile);
        } catch (Exception e) {
            Log.w(TAG, "getTempUri fail : " + e.getMessage());
        }
        return uri;
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTimer();

        if (CURRENT_STATE == STATE_RECORD) {
            CURRENT_STATE = STATE_PREPARE;
            voiceRecordListener.onStopRecord();
            setRecordView();
        }

        if (CURRENT_STATE == STATE_PLAY) {
            recordButton.performClick();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();

        if (!reply) {
            Log.d(TAG, "card reverse");
            Intent returnIntent = getIntent();
            setResult(RESULT_CANCELED, returnIntent);
        }
    }

    public void setRecordView() {
        switch (CURRENT_STATE) {
            case STATE_PREPARE:
                tvRecordTime.setVisibility(View.VISIBLE);
                recordButton.setImageResource(R.drawable.record);
                recordExample.setVisibility(View.VISIBLE);
                recordExample.setText(Html.fromHtml(getString(R.string.ex_record)));
                recordState.setText("On Air");
                mProgressbar.setProgress(0);
                mProgressbar.setVisibility(View.INVISIBLE);
                tvStartTime.setVisibility(View.INVISIBLE);
                tvEndTime.setVisibility(View.INVISIBLE);
                ivVolume.setVisibility(View.VISIBLE);
                ivShadow.setVisibility(View.VISIBLE);
                break;
            case STATE_RECORD:
                tvRecordTime.setVisibility(View.VISIBLE);
                recordButton.setImageResource(R.drawable.recording);
                recordExample.setVisibility(View.VISIBLE);
                break;
            case STATE_STOP:
                if (startRecordTime != 0L) {
                    //녹음 정지일 경우
                    recordButton.setImageResource(R.drawable.button_recorded);
                    recordExample.setVisibility(View.VISIBLE);
                    recordExample.setText(R.string.ex_save);
                    recordState.setVisibility(View.VISIBLE);
                    recordState.setText("Saved");
                    recordState.setTextColor(getResources().getColor(R.color.green));
                    ivVolume.setVisibility(View.INVISIBLE);
                    ivShadow.setVisibility(View.INVISIBLE);
                    mProgressbar.setVisibility(View.VISIBLE);
                    mProgressbar.setProgress(0);
                    tvStartTime.setVisibility(View.VISIBLE);
                    tvEndTime.setVisibility(View.VISIBLE);
                    tvEndTime.setText(Utils.setRecordTimer(playTime));
                    sendButton.setImageResource(R.drawable.send_activated);
                } else {
                    //재생 정지일 경우
                    recordButton.setImageResource(R.drawable.button_play_white);
                    recordExample.setVisibility(View.INVISIBLE);
                    recordState.setVisibility(View.INVISIBLE);
                }
                tvRecordTime.setVisibility(View.INVISIBLE);
                break;
            case STATE_PLAY:
                tvRecordTime.setVisibility(View.INVISIBLE);
                recordButton.setImageResource(R.drawable.pause);
                recordExample.setVisibility(View.INVISIBLE);
                recordState.setVisibility(View.INVISIBLE);
                mProgressbar.setVisibility(View.VISIBLE);
                tvStartTime.setVisibility(View.VISIBLE);
                tvEndTime.setVisibility(View.VISIBLE);
                ivVolume.setVisibility(View.INVISIBLE);
                ivShadow.setVisibility(View.INVISIBLE);
                break;
        }
    }

    public class MyTimerTask extends TimerTask {
        long maxRecordTime = 30 * 1000;
        long recordTime;
        long time = 0L;
        boolean isPause = false;

        public MyTimerTask(@Nullable long myRecordTime) {
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
            runOnUiThread(() -> {
                if (isPause) {
                    return;
                }

                time += 10L;
                if (recordTime == 0) {
                    tvRecordTime.setText(Utils.setRecordTimer(time) + " / " + Utils.setRecordTimer(maxRecordTime));
                    Log.e(TAG, "progress: " + (int) (time / maxRecordTime));

                    if (time >= maxRecordTime) {
                        voiceRecordListener.onStopRecord();
                        stopTimer();
                    }
//                    mProgressbar.setProgress((int)(time*100/maxRecordTime));
                } else {
                    if (time >= recordTime) {
                        CURRENT_STATE = STATE_STOP;
                        recordButton.setImageResource(R.drawable.play_white);
                        tvRecordTime.setText(Utils.setRecordTimer(recordTime) + " / " + Utils.setRecordTimer(recordTime));
                        tvStartTime.setText(Utils.setRecordTimer(recordTime));
                        tvEndTime.setText(Utils.setRecordTimer(recordTime));
                        mProgressbar.setProgress(100);
                        if (voiceRecordListener != null) {
                            voiceRecordListener.onStopPlay();
                        }
                        stopTimer();
                        return;
                    }
                    tvRecordTime.setText(Utils.setRecordTimer(time) + " / " + Utils.setRecordTimer(recordTime));
                    tvStartTime.setText(Utils.setRecordTimer(time));
                    tvEndTime.setText(Utils.setRecordTimer(recordTime));
                    Log.e(TAG, "progress: " + (int) (time * 100 / recordTime));
                    mProgressbar.setProgress((int) (time * 100 / recordTime));
                }
            });
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
