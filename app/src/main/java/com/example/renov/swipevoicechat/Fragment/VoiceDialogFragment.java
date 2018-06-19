package com.example.renov.swipevoicechat.Fragment;

import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.example.renov.swipevoicechat.R;
import com.example.renov.swipevoicechat.widget.VoicePlayerManager;
import com.example.renov.swipevoicechat.widget.VoicePlayerView;

import java.io.File;
import java.util.Map;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class VoiceDialogFragment extends DialogFragment {

    public static final String TAG = "VoiceDialogFragment";
    private boolean isReply;
    private ResultReceiver receiver;
    private String voiceUrl;
    private boolean isProfile;
    private boolean isPromotion;
    private int dialogCase;
    private int needBuzziCount;
    public static final String EXTRA_RECEIVER = "extra.receiver";
    public static final String EXTRA_RESULT_DATA = "extra.result.data";

    private String title;
    private String description;
    private String example;

    @BindView(R.id.tv_title)
    TextView mTextTitle;
    @BindView(R.id.tv_desc)
    TextView mTextDesc;
    @BindView(R.id.tv_example)
    TextView mTextExample;
    @BindView(R.id.btn_voice_record)
    VoicePlayerView mBtnVoiceRecord;
    @BindView(R.id.btn_send)
    Button mBtnSend;
    @BindView(R.id.btn_voice_reset)
    TextView mBtnVoiceReset;

    private Unbinder unbinder;

    @OnClick(R.id.btn_send)
    public void onClickBtnSend() {
//        getUploadVoiceInfoAndUploadVoice();
        this.dismiss();
    }

    @OnClick(R.id.btn_voice_reset)
    public void onClickBtnVoiceReset() {
        int currentState = mBtnVoiceRecord.getState();

        if(currentState == VoicePlayerView.STATE_STOP){
            mBtnVoiceRecord.resetVoiceRecord();
            mBtnVoiceReset.setText("녹음 하기");
        }
        else
            mBtnVoiceRecord.performClick();

//        mBtnVoiceRecord.performClick();
//        mBtnVoiceRecord.resetVoiceRecord();
//        mTextDesc.setVisibility(View.VISIBLE);
    }

    public static final int DIALOG_CASE_PROFILE_VOICE_PLAY = 1;
    public static final int DIALOG_CASE_PROFILE_VOICE_RECORD = 2;
    public static final int DIALOG_CASE_STORY_RECORD = 3;

    //    프로필 수정에서 음성 플레이,녹음 부분
    public static VoiceDialogFragment newInstance(String voiceUrl, ResultReceiver receiver) {
        VoiceDialogFragment fragment = new VoiceDialogFragment();

        Bundle args = new Bundle();
        args.putString("voiceUrl", voiceUrl);
        args.putParcelable(EXTRA_RECEIVER, receiver);
        args.putBoolean("isProfile", true);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.TransparentDialogFragment);

        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            isReply = args.getBoolean("isReply");
            isPromotion = args.getBoolean("isPromotion");
            voiceUrl = args.getString("voiceUrl");
            isProfile = args.getBoolean("isProfile");
            dialogCase = args.getInt("dialogCase", 0);
            receiver = args.getParcelable(EXTRA_RECEIVER);
            needBuzziCount = args.getInt("needBuzziCount");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_record2, container, false);

        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        mTextTitle.setText(title);
        mTextDesc.setText(description);
        mTextExample.setText(example);
        mBtnVoiceReset.setText("녹음 하기");

        mBtnVoiceRecord.setVoiceRecordListener(new VoicePlayerView.VoiceRecordListener() {
            @Override
            public void onRecord() {
//                LogUtil.d("VoiceDialogFragment] onRecord()");
//                PermissionManager.getInstance().requestPermission(
//                        getContext(),
//                        PermissionUtils.MICROPHONE,
//                        PermissionUtils.MICROPHONE_NAME,
//                        new PermissionListener() {
//                            @Override
//                            public void onGranted() {
//                                if (getContext() != null) {
//                                    VoicePlayerManager.getInstance().voiceRecord(getContext());
//                                    mBtnVoiceRecord.startRecordProgress(30000);
//                                }
//                            }
//
//                            @Override
//                            public void onDenied() {
//                                mBtnVoiceRecord.resetVoiceRecord();
//                            }
//                        }
//                );
                VoicePlayerManager.getInstance().voiceRecord(getContext());
                mBtnVoiceRecord.startRecordProgress(30000);
                mBtnVoiceReset.setText("녹음 중지");
            }

            @Override
            public void onStopRecord() {
                VoicePlayerManager.getInstance().voiceRecordStop();
                mBtnSend.setEnabled(true);
                mBtnVoiceReset.setText("다시 녹음");
            }

            @Override
            public void onPlay() {
                int duration = 0;
                if (voiceUrl == null) {
                    duration = VoicePlayerManager.getInstance().voicePlay(getContext());
                } else {
                    duration = VoicePlayerManager.getInstance().voicePlay(voiceUrl);
                }
                mBtnVoiceReset.setText("재생 중지");
                mBtnVoiceRecord.startVoicePlayProgress(duration);
            }

            @Override
            public void onStopPlay() {
                VoicePlayerManager.getInstance().voicePlayStop();
                mBtnVoiceReset.setText("녹음 하기");
            }
        });
    }


    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setExample(String example) {
        this.example = example;
    }

    public void setViewType(int ExampleCase) {
        switch (ExampleCase) {
            case 0:
                break;
            case 1:
                break;
            case 2:
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        unbinder.unbind();
    }

//    private void getUploadVoiceInfoAndUploadVoice() {
//        HttpRequestVO httpRequestVO = HttpUtil.getHttpRequestVO(Constants.URL_STORY_VOICE_UPLOAD, Map.class, null, getContext());
//        new RequestFactory().create(httpRequestVO, new HttpResponseCallback<Map>() {
//            @Override
//            public void onResponse(Map result) {
//                LogUtil.d("voice upload result: " + result.toString());
//
//                uploadVoice(result);
//            }
//
//            @Override
//            public void onError(HttpNetworkError error) {
//                Log.e("result", null, error);
//            }
//        }).execute();
//    }
//
//    public void uploadVoice(Map updateInfo) {
//        final String uploadImagePath = (String) updateInfo.get("uploadImagePath");
//
//        String filePath = ImageUtil.getFilePathFromUri(getTempUri(), getContext());
//        if (StringUtil.isEmpty(filePath)) {
//            return;
//        }
//
//        final ProgressHandler progressHandler = new ProgressHandler(getActivity(), false);
//
//        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Constants.IMAGE_SERVER_URL,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        Log.d(TAG, "onResponse : " + response);
//
//                        progressHandler.onCancel();
//
//                        Bundle data = new Bundle();
//                        data.putString(Constants.EXTRA_RESULT_DATA, uploadImagePath);
//                        receiver.send(1000, data);
//
//                        dismiss();
//
//                    }
//                },
//                new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Log.e(TAG, "onErrorResponse : " + error.getMessage());
//
//                        progressHandler.onCancel();
//                    }
//                });
//
//        updateInfo.remove("Host");
//        updateInfo.remove("uploadImagePath");
//        multipartRequest.addStringParams(updateInfo);
//        multipartRequest.addAttachment(VolleyMultipartRequest.MEDIA_TYPE_JPEG, "file", new File(filePath));
//        multipartRequest.buildRequest();
//        multipartRequest.setRetryPolicy(new DefaultRetryPolicy(
//                Constants.HTTP_CONNECTION_TIME_OUT,
//                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//
////        multipartRequest.setFixedStreamingMode(true);
//
//
//        progressHandler.onStart();
//        RequestManager.addRequest(multipartRequest, "ProfileMultipart");
//    }
//
//    private Uri getTempUri() {
//        Uri uri = null;
//        try {
//            uri = Uri.fromFile(new File(VoicePlayerManager.getInstance().getFileName()));
//        } catch (Exception e) {
//            LogUtil.w("getTempUri fail : " + e.getMessage());
//        }
//        return uri;
//    }
}

