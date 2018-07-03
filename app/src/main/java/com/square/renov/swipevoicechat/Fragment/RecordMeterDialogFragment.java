package com.square.renov.swipevoicechat.Fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.square.renov.swipevoicechat.Model.VoiceCard;
import com.square.renov.swipevoicechat.Network.NetRetrofit;
import com.square.renov.swipevoicechat.Network.network.RequestManager;
import com.square.renov.swipevoicechat.Network.network.VolleyMultipartRequest;
import com.square.renov.swipevoicechat.R;
import com.square.renov.swipevoicechat.Util.ImageUtil;
import com.square.renov.swipevoicechat.widget.VoicePlayerManager;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;

public class RecordMeterDialogFragment extends DialogFragment {
    private static final String TAG = RecordMeterDialogFragment.class.getSimpleName();

    @BindViews({R.id.tv_meter_1,R.id.tv_meter_2, R.id.tv_meter_3, R.id.tv_meter_4, R.id.tv_meter_5, R.id.tv_meter_6})
    List<TextView> meterList;

    Context context;
    int chatId;
    TimerTask meterTask;
    Timer timer;
    VoicePlayerManager voicePlayerManager;
    Unbinder unbinder;

    int step1 = 1000, step2 = 2000, step3 = 3000, step4 = 4000, step5 = 5000, step6 = 600;

    public static RecordMeterDialogFragment newInstance(int chatId){
        RecordMeterDialogFragment recordMeterDialogFragment = new RecordMeterDialogFragment();

        Bundle bundle = new Bundle();
        bundle.putInt("chatId",chatId);
        recordMeterDialogFragment.setArguments(bundle);

        return recordMeterDialogFragment;
    }

    @Override
    public void onAttach(Context context) {
        this.context = context;
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();

        if(args != null) {
            this.chatId = args.getInt("chatId");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_reply, container);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setMeterView();
    }

    private void setMeterView() {
        voicePlayerManager = VoicePlayerManager.getInstance();
        voicePlayerManager.voiceRecord(getContext());
        meterTask = new TimerTask() {
            @Override
            public void run() {
                int volume = voicePlayerManager.getAmplitude();

                if(volume < step1){
                    getActivity().runOnUiThread(new MeterTask(0, meterList));
                } else if (step1 <= volume && volume < step2){
                    getActivity().runOnUiThread(new MeterTask(1, meterList));
                } else if (step2 <= volume && volume < step3){
                    getActivity().runOnUiThread(new MeterTask(2, meterList));
                } else if (step3 <= volume && volume < step4){
                    getActivity().runOnUiThread(new MeterTask(3, meterList));
                } else if (step4 <= volume && volume < step5){
                    getActivity().runOnUiThread(new MeterTask(4, meterList));
                } else if (step5 <= volume){
                    getActivity().runOnUiThread(new MeterTask(5, meterList));
                } else {
                    getActivity().runOnUiThread(new MeterTask(0, meterList));
                }
            }
        };

        timer = new Timer();
        timer.schedule(meterTask, 500, 500);

    }

    class MeterTask implements Runnable {
        List<TextView> meterList;
        int step;

        MeterTask(int step, List<TextView> meterList){
            this.step = step;
            this.meterList = meterList;
        }

        @Override
        public void run() {
            for(TextView tv : meterList){
                if(meterList.indexOf(tv) <= step)
                    tv.setVisibility(View.VISIBLE);
                else
                    tv.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        timer.cancel();
        voicePlayerManager.voiceRecordStop();
        getUploadVoiceInfoAndUploadVoice();

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
        final String uploadVoicePath = "https://" + updateInfo.get("Host") + "/" + updateInfo.get("key");

        String filePath = ImageUtil.getFilePathFromUri(getTempUri(temptFile), getContext());
        if (filePath == null || "".equals(filePath)) {
            return;
        }

        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest("https://hellovoicebucket.s3.amazonaws.com",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "onResponse : " + response);

                        Call<VoiceCard> request = NetRetrofit.getInstance(getContext()).getService().sendChatVoice(chatId, uploadVoicePath);
                        request.enqueue(new Callback<VoiceCard>() {
                            @Override
                            public void onResponse(Call<VoiceCard> call, retrofit2.Response<VoiceCard> response) {
                                if(response.isSuccessful()){
                                    Toast.makeText(context, "발송 되었습니다.", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<VoiceCard> call, Throwable t) {
                                t.printStackTrace();
                            }
                        });
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "onErrorResponse : " + error.getMessage());
                        Toast.makeText(getContext(), "음성 파일 업로드에 실패했습니다. 다시 녹음해주세요", Toast.LENGTH_SHORT).show();
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
