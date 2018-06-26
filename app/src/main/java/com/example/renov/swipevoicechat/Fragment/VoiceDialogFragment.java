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

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.renov.swipevoicechat.Network.NetRetrofit;
import com.example.renov.swipevoicechat.Network.network.HttpNetworkError;
import com.example.renov.swipevoicechat.Network.network.HttpRequestVO;
import com.example.renov.swipevoicechat.Network.network.HttpResponseCallback;
import com.example.renov.swipevoicechat.Network.network.ProgressHandler;
import com.example.renov.swipevoicechat.Network.network.RequestManager;
import com.example.renov.swipevoicechat.Network.network.VolleyMultipartRequest;
import com.example.renov.swipevoicechat.R;
import com.example.renov.swipevoicechat.Util.ImageUtil;
import com.example.renov.swipevoicechat.widget.VoicePlayerManager;
import com.example.renov.swipevoicechat.widget.VoicePlayerView;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;

public class VoiceDialogFragment extends DialogFragment {

    public static final String TAG = "VoiceDialogFragment";
    private boolean isReply;
    private ResultReceiver receiver;
    private String voiceUrl;
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
        getUploadVoiceInfoAndUploadVoice();
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
            voiceUrl = args.getString("voiceUrl");
            receiver = args.getParcelable(EXTRA_RECEIVER);
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

    private void getUploadVoiceInfoAndUploadVoice() {
        File voiceFile = new File(VoicePlayerManager.getInstance().getFileName());
        int fileSize = (int) voiceFile.length();

        Call<Map> call = NetRetrofit.getInstance(getContext()).getService().getUploadMetaData("voice", fileSize);
        call.enqueue(new Callback<Map>() {
            @Override
            public void onResponse(Call<Map> call, retrofit2.Response<Map> response) {
                try {
                    Log.d(TAG, "response raw: " + response.raw());
                    Log.d(TAG, "response headers: " + response.headers());
                    Log.d(TAG, "response body: " + response.body());

                    if (response.errorBody() != null)
                        Log.d(TAG, "response error body: " + response.errorBody().string());

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
                        Log.e(TAG, "error code: " + response.code() + " error body: " + response.errorBody());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Map> call, Throwable t) {

            }
        });
    }

    public void uploadVoice(Map updateInfo, File temptFile) {
        final String uploadImagePath = "https://" + updateInfo.get("Host") + "/" + updateInfo.get("key");

        String filePath = ImageUtil.getFilePathFromUri(getTempUri(temptFile), getContext());
        if (filePath == null || "".equals(filePath)) {
            return;
        }

        final ProgressHandler progressHandler = new ProgressHandler(getActivity(), false);

        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest("https://hellovoicebucket.s3.amazonaws.com",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "onResponse : " + response);

                        progressHandler.onCancel();

                        Bundle data = new Bundle();
                        data.putString(VoiceDialogFragment.EXTRA_RESULT_DATA, uploadImagePath);
                        receiver.send(1000, data);

                        dismiss();

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "onErrorResponse : " + error.getMessage());

                        progressHandler.onCancel();
                    }
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

//        multipartRequest.setFixedStreamingMode(true);


        progressHandler.onStart();
        RequestManager.addRequest(multipartRequest, "ProfileMultipart");
    }

    private Uri getTempUri(File temptFile) {
        Uri uri = null;
        try {
            uri = Uri.fromFile(temptFile);
        } catch (Exception e) {
            Log.w(TAG,"getTempUri fail : " + e.getMessage());
        }
        return uri;
    }
}

