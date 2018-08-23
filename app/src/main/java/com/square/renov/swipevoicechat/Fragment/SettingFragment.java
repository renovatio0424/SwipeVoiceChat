package com.square.renov.swipevoicechat.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.DefaultRetryPolicy;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.square.renov.swipevoicechat.Activity.LogInActivity;
import com.square.renov.swipevoicechat.Activity.WebActivity;
import com.square.renov.swipevoicechat.Model.Result;
import com.square.renov.swipevoicechat.Model.User;
import com.square.renov.swipevoicechat.Network.NetRetrofit;
import com.square.renov.swipevoicechat.Network.network.ProgressHandler;
import com.square.renov.swipevoicechat.Network.network.RequestManager;
import com.square.renov.swipevoicechat.Network.network.VolleyMultipartRequest;
import com.square.renov.swipevoicechat.R;
import com.square.renov.swipevoicechat.Util.AgeUtil;
import com.square.renov.swipevoicechat.Util.DialogUtils;
import com.square.renov.swipevoicechat.Util.ImageUtil;
import com.square.renov.swipevoicechat.Util.SharedPrefHelper;
import com.onesignal.OneSignal;
import com.square.renov.swipevoicechat.Util.Utils;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;

public class SettingFragment extends Fragment {

    private static final String TAG = SettingFragment.class.getSimpleName();
    @BindView(R.id.iv_profile)
    ImageView profileImage;
    @BindView(R.id.iv_edit_profile)
    ImageView ivEditProfile;
    @BindView(R.id.iv_edit_name)
    ImageView ivEditName;
    @BindView(R.id.tv_name)
    TextView nameAgeInfo;
    @BindView(R.id.sw_reply_push)
    ImageView replyPush;
    @BindView(R.id.sw_basic_push)
    ImageView basicPush;

    User myInfo;
    public Unbinder unbinder;
    MultiTransformation multiTransformation = new MultiTransformation(new CircleCrop(),
            new FitCenter());

    private boolean isCheckedBasicPush, isCheckedReplyPush;

    public static SettingFragment newInstance(User user) {
        SettingFragment settingFragment = new SettingFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("user", user);
        settingFragment.setArguments(bundle);
        return settingFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myInfo = SharedPrefHelper.getInstance(getContext()).getUserInfo();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadMyNameAndProfileImage();

        checkRadioButton(replyPush, false);
        checkRadioButton(basicPush, false);

        OneSignal.getTags(tags -> {
            if (tags != null) {
                isCheckedReplyPush = tags.has("Reply");
                isCheckedBasicPush = tags.has("Basic");

                Log.d(TAG, "Onesignal TAG: " + (isCheckedReplyPush ? "reply y" : "reply n"));
                Log.d(TAG, "Onesignal TAG: " + (isCheckedBasicPush ? "basic y " : "basic n"));

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        checkRadioButton(replyPush, isCheckedReplyPush);
                        checkRadioButton(basicPush, isCheckedBasicPush);
                    });
                }
            } else {
                Log.d(TAG, "Onesignal TAG is null");
            }
        });

        replyPush.setOnClickListener(v -> {
            if (isCheckedReplyPush)
                OneSignal.deleteTag("Reply");
            else
                OneSignal.sendTag("Reply", "true");

            checkRadioButton(replyPush, !isCheckedReplyPush);
        });

        basicPush.setOnClickListener(v -> {
            if (isCheckedBasicPush)
                OneSignal.deleteTag("Basic");
            else
                OneSignal.sendTag("Basic", "true");
            checkRadioButton(basicPush, !isCheckedBasicPush);
        });
    }

    private void loadMyNameAndProfileImage() {
        if (myInfo == null || myInfo.getProfileImageUrl() == null || "".equals(myInfo.getProfileImageUrl()))
            Glide.with(getContext())
                    .load(R.drawable.com_facebook_profile_picture_blank_square)
                    .apply(RequestOptions.bitmapTransform(multiTransformation))
                    .into(profileImage);
        else
            Glide.with(getContext())
                    .load(myInfo.getProfileImageUrl())
                    .apply(RequestOptions.bitmapTransform(multiTransformation))
                    .into(profileImage);

        SpannableStringBuilder s = Utils.setNameAndAge(myInfo.getName(), AgeUtil.getAgeFromBirth(myInfo.getBirth()));
        s.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.age_black_color)), myInfo.getName().length(), s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        nameAgeInfo.post(() ->
                nameAgeInfo.setText(s)
        );

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }


    @OnClick(R.id.iv_profile)
    public void onClickProfile() {
        if (CropImage.isExplicitCameraPermissionRequired(getContext())) {
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE);
            return;
        }

        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMaxCropResultSize(960, 1020)
                .setMinCropResultSize(200, 300)
                .setAutoZoomEnabled(false)
                .setFixAspectRatio(true)
                .setAspectRatio(3, 4)
                .setAllowFlipping(false)
                .setAllowRotation(false)
                .start(getContext(), this);
        Toast.makeText(getActivity(), "click profile", Toast.LENGTH_SHORT).show();
    }

    private int CAMERA_CODE = 1;
    private int GALLERY_CODE = 2;
    Uri mCropImageUri;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(TAG, "on activity result");
        Toast.makeText(getActivity(), "on activity result", Toast.LENGTH_SHORT).show();
        // handle result of pick image chooser
        if (requestCode == CAMERA_CODE && resultCode == RESULT_OK || requestCode == GALLERY_CODE && resultCode == RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(getContext(), data);
            Log.d(TAG, "image uri: " + imageUri.toString());
            // For API >= 23 we need to check specifically that we have permissions to read external storage.
            if (CropImage.isReadExternalStoragePermissionsRequired(getContext(), imageUri)) {
                // request permissions and handle the result in onRequestPermissionsResult()
                mCropImageUri = imageUri;
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
            } else {
                // no permissions required or already granted, can start crop image activity
                startCropImageActivity(imageUri);
            }
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                mCropImageUri = resultUri;
                getActivity().runOnUiThread(() -> Glide.with(getActivity())
                        .load(mCropImageUri)
                        .apply(RequestOptions.bitmapTransform(multiTransformation))
                        .into(profileImage));
                ImageUpload(mCropImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(getActivity(), "Cancelling, required permissions are not granted", Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
            if (mCropImageUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // required permissions granted, start crop image activity
            } else {
                Toast.makeText(getActivity(), "Cancelling, required permissions are not granted", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startCropImageActivity(Uri imageUri) {
        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMinCropResultSize(200, 300)
                .setFixAspectRatio(true)
                .setAutoZoomEnabled(false)
                .setAspectRatio(3, 4)
                .setAllowFlipping(false)
                .setAllowRotation(false)
                .start(getActivity());
    }

    private void ImageUpload(Uri mCropImageUri) {
//        File imageFile = new File(mCropImageUri.getPath());
        Uri blurImageUri = ImageUtil.cropImageToBlurImage(mCropImageUri, getContext());
        File imageFile = new File(ImageUtil.getFilePathFromUri(blurImageUri, getContext()));
//        int fileSize = Integer.parseInt(String.valueOf(imageFile.length()/1024));
        int fileSize = (int) imageFile.length();
        Call<Map> call = NetRetrofit.getInstance(getContext()).getService().getUploadMetaData("image", fileSize);
        call.enqueue(new Callback<Map>() {
            @Override
            public void onResponse(Call<Map> call, Response<Map> response) {

                if (response.isSuccessful()) {
                    String key = (String) response.body().get("key");
                    String Host = (String) response.body().get("Host");

                    Log.d(TAG, "key: " + key);
                    Log.d(TAG, "Host: " + Host);

                    String fileName = key.replace("image/", "");
                    uploadImage(response.body(), imageFile);
                } else {
                    try {
                        Utils.toastError(getContext(), response);
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

    public void uploadImage(Map updateInfo, File imagefile) {
        final String uploadImagePath = "https://" + updateInfo.get("Host") + "/" + updateInfo.get("key");
        Log.d(TAG, uploadImagePath);

        String filePath = ImageUtil.getFilePathFromUri(getTempUri(), getContext());
        if (filePath == null || "".equals(filePath)) {
            return;
        }

        final ProgressHandler progressHandler = new ProgressHandler(getActivity(), false);

        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest("https://hellovoicebucket.s3.amazonaws.com",
                response -> {
                    Log.d(TAG, "onResponse : " + response);

                    progressHandler.onCancel();

                    ImageUtil.deleteImageByUri(getTempUri(), getContext());
                    getActivity().runOnUiThread(() -> Glide.with(getActivity())
                            .load(uploadImagePath)
                            .apply(RequestOptions.bitmapTransform(multiTransformation))
                            .into(profileImage));

                    updateUserInfo(uploadImagePath);
//                        리워드 보상 팝업 띄우기
//                        OneButtonAlertDialogFragment oneButtonAlertDialogFragment = OneButtonAlertDialogFragment.newInstance();
//                        Bundle bundle = new Bundle();
//                        bundle.putString("title", "매력 카드 평가를 통해 1버찌를 얻었습니다.");
//                        oneButtonAlertDialogFragment.setArguments(bundle);
//                        oneButtonAlertDialogFragment.show
//                        oneButtonAlertDialogFragment.show(getFragmentManager(), "oneButtonAlertDialogFragment");

//                        아직 프로필 승인이 안되었고, 보이스 등록이 안되었을 경우 경우에
//                        String userStatus = user.getStatus();
//                        int order = profileImageViewList.indexOf(selectedProfileImageView);
//
//                        LogUtil.d("user.getStatus(): " + user.getStatus());
//                        String status = SharedPrefHelper.getInstance(ProfileActivity.this).getSharedPreferences(SharedPrefHelper.USER_STATUS,null);
//                        LogUtil.d("sharedpref: " + status);
//
//
//                        if(order == 1 &&
//                                "P".equals(userStatus) &&
//                                (user.getVoice() == null | "".equals(user.getVoice()))){
//                            OneButtonAlertDialogFragment oneButtonAlertDialogFragment = OneButtonAlertDialogFragment.newInstance();
//                            Bundle bundle = new Bundle();
//                            bundle.putString(AlertDialogFragment.DIALOG_TITLE_NAME, "프로필 보이스 이벤트에 참여하세요!!");
//                            bundle.putString(AlertDialogFragment.DIALOG_DESCRIPTION_NAME, getString(R.string.profile_voice_reward));
//                            bundle.putString(AlertDialogFragment.DIALOG_CONFIRM_NAME, "확인");
//                            oneButtonAlertDialogFragment.setArguments(bundle);
//                            oneButtonAlertDialogFragment.show(getSupportFragmentManager(), "oneButtonAlertDialogFragment");
//                        }
                }, error -> {
            Log.d(TAG, "onErrorResponse : " + error.getMessage());

            //TODO : 프로그레스 스타트
            ImageUtil.deleteImageByUri(getTempUri(), getActivity());
            Toast.makeText(getActivity(), "error", Toast.LENGTH_SHORT).show();
        });

        updateInfo.remove("Host");
        updateInfo.remove("uploadImagePath");
        multipartRequest.addStringParams(updateInfo);
        multipartRequest.addAttachment(VolleyMultipartRequest.MEDIA_TYPE_JPEG, "file", imagefile);
        multipartRequest.buildRequest();
        multipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

//        multipartRequest.setFixedStreamingMode(true);

        //TODO : 프로그레스 종료
        RequestManager.addRequest(multipartRequest, "ProfileMultipart");
    }

    private Uri getTempUri() {
        Uri uri = null;
        try {
            uri = Uri.fromFile(ImageUtil.createTempImageFileForProfile());
        } catch (Exception e) {
            Log.w(TAG, "getTempUri fail : " + e.getMessage());
        }
        return uri;
    }

    private void updateUserInfo(String uploadImagePath) {
//        getMyInfo(uploadImagePath);
        User myInfo = SharedPrefHelper.getInstance(getContext()).getUserInfo();

        Log.e(TAG, "user birth: " + myInfo.getBirth());
        myInfo.setProfileImageUrl(uploadImagePath);

        Call<User> request = NetRetrofit.getInstance(getContext()).getService().updateUserInfo(myInfo);
        request.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    Gson gson = new Gson();
                    SharedPrefHelper.getInstance(getActivity()).setSharedPreferences(SharedPrefHelper.USER_INFO, gson.toJson(response.body()));
                } else {
                    try {
                        Utils.toastError(getContext(), response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }
        });
    }

    @OnClick(R.id.layout_logout)
    public void onClickLogOut() {
        //TODO: 로그아웃 기능 구현중
        MaterialDialog logoutDialog = new MaterialDialog.Builder(getActivity())
                .title("로그아웃 하시겠습니까?")
                .titleColorRes(R.color.black)
                .content("정말 로그아웃 하시겠습니까?")
                .contentColorRes(R.color.grey)
                .backgroundColorRes(R.color.white)
                .negativeText("취소")
                .negativeColorRes(R.color.grey)
                .positiveText("로그아웃")
                .positiveColorRes(R.color.main_color)
                .onPositive((dialog, which) -> {
                    Call<Result> response = NetRetrofit.getInstance(getContext()).getService().logout();
                    response.enqueue(new Callback<Result>() {
                        @Override
                        public void onResponse(Call<Result> call, Response<Result> response) {
                            if (response.isSuccessful()) {
                                SharedPrefHelper.getInstance(getContext()).removeAllSharedPreferences();
                                OneSignal.deleteTag("userId");
                                OneSignal.deleteTag("Basic");
                                OneSignal.deleteTag("Reply");
                                goToLogin();
                            }
                        }

                        @Override
                        public void onFailure(Call<Result> call, Throwable t) {
                            t.printStackTrace();
                        }
                    });
                })
                .show();
//
//        MaterialDialog logoutDialog = new MaterialDialog.Builder(getActivity())
//                .customView(R.layout.dialog_code, false)
//                .show();
//
//        TextView tvTitle = (TextView) logoutDialog.findViewById(R.id.tv_title);
//        TextView tvContent = (TextView) logoutDialog.findViewById(R.id.tv_content);
//        TextView tvSend = (TextView) logoutDialog.findViewById(R.id.tv_send_code);
//        TextView tvCancel = (TextView) logoutDialog.findViewById(R.id.tv_cancel);
//        EditText etReason = (EditText) logoutDialog.findViewById(R.id.et_code);
//
//        etReason.setVisibility(View.GONE);
//        tvTitle.setText("로그아웃 하시겠습니까?");
//        tvContent.setText("정말 로그아웃 하시겠습니까?");
//        tvCancel.setText("취소");
//        tvSend.setText("로그아웃");
//
//        tvCancel.setOnClickListener(v->{
//            logoutDialog.dismiss();
//        });
//        tvSend.setOnClickListener(v->{
//            Call<Result> response = NetRetrofit.getInstance(getContext()).getService().logout();
//                    response.enqueue(new Callback<Result>() {
//                        @Override
//                        public void onResponse(Call<Result> call, Response<Result> response) {
//                            if (response.isSuccessful()) {
//                                Toast.makeText(getContext(), response.body().getMessage(), Toast.LENGTH_SHORT).show();
//                                SharedPrefHelper.getInstance(getContext()).removeAllSharedPreferences();
//                                OneSignal.deleteTag("userId");
//                                OneSignal.deleteTag("Basic");
//                                OneSignal.deleteTag("Reply");
//                                goToLogin();
//                            }
//
//                            switch (response.code()) {
//                                default:
//                                    Toast.makeText(getContext(), "code: " + response.code() + "message: " + response.body(), Toast.LENGTH_SHORT).show();
//                                    break;
//                            }
//                        }
//
//                        @Override
//                        public void onFailure(Call<Result> call, Throwable t) {
//                            t.printStackTrace();
//                        }
//                    });
//        });

//        DialogUtils.initDialogView(logoutDialog, getActivity());
    }

    @OnClick(R.id.layout_terms)
    public void onClickTerms() {
        Intent intent = new Intent(getActivity(), WebActivity.class);
        intent.putExtra("name", "policy");
        startActivity(intent);
    }

    @SuppressLint("StringFormatMatches")
    @OnClick(R.id.layout_feedback)
    public void onClickFeedback() {
        User me = SharedPrefHelper.getInstance(getActivity()).getUserInfo();
        Uri uri = Uri.parse("mailto:help.sori@formationsquare.com");
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.do_qna));
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.email_content_qna, me.getId()));
        startActivity(Intent.createChooser(intent, getString(R.string.send_email)));
    }

    private void goToLogin() {
        Intent intent = new Intent(getContext(), LogInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @OnClick(R.id.layout_withdraw)
    public void onClickWithdraw() {
        initWithdrawDialog();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initWithdrawDialog() {
        MaterialDialog reportDialog = new MaterialDialog.Builder(getActivity())
                .customView(R.layout.dialog_code, false)
                .show();

//        DialogUtils.initDialogView(reportDialog, getActivity());

        TextView tvTitle = (TextView) reportDialog.findViewById(R.id.tv_title);
        TextView tvContent = (TextView) reportDialog.findViewById(R.id.tv_content);
        TextView tvSend = (TextView) reportDialog.findViewById(R.id.tv_send_code);
        TextView tvCancel = (TextView) reportDialog.findViewById(R.id.tv_cancel);
        EditText etReason = (EditText) reportDialog.findViewById(R.id.et_code);
        ConstraintLayout dialogLayout = (ConstraintLayout) reportDialog.findViewById(R.id.layout_dialog);

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(dialogLayout);
        constraintSet.constrainPercentWidth(R.id.et_code, 0.8f);
        constraintSet.applyTo(dialogLayout);

        tvTitle.setText("계정 탈퇴를 하시겠습니까?");
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        tvContent.setVisibility(View.GONE);

        etReason.setHint("계정 탈퇴를 하시는 이유를 알려주세요");
        etReason.setSingleLine(false);
        etReason.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
        etReason.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        etReason.setLines(5);
        etReason.setMaxLines(5);
        etReason.setVerticalScrollBarEnabled(true);
        etReason.setBackgroundResource(R.drawable.background_edit);
        etReason.setMovementMethod(ScrollingMovementMethod.getInstance());
        etReason.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
        etReason.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0)
                    enabledSendButton(tvSend, false);
                else
                    enabledSendButton(tvSend, true);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        tvSend.setText("확인");
        tvSend.setOnClickListener(v -> {
            Call<Result> request = NetRetrofit.getInstance(getActivity()).getService().withdrawUser(etReason.getText().toString());
            request.enqueue(new Callback<Result>() {
                @Override
                public void onResponse(Call<Result> call, Response<Result> response) {
                    if (response.isSuccessful()) {
                        reportDialog.dismiss();
                        SharedPrefHelper.getInstance(getContext()).removeAllSharedPreferences();
                        OneSignal.deleteTag("userId");
                        OneSignal.deleteTag("Basic");
                        OneSignal.deleteTag("Reply");
                        goToLogin();
                    } else {
                        try {
                            Utils.toastError(getContext(), response);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<Result> call, Throwable t) {
                    t.printStackTrace();
                }
            });
        });
        tvCancel.setOnClickListener(v -> {
            reportDialog.dismiss();
        });
    }


    @OnClick({R.id.iv_edit_name, R.id.tv_name})
    public void onClickEditName() {
        Toast.makeText(getContext(), "click edit name", Toast.LENGTH_SHORT).show();

        MaterialDialog reportDialog = new MaterialDialog.Builder(getActivity())
                .customView(R.layout.dialog_code, false)
                .show();

//        DialogUtils.initDialogView(reportDialog, getActivity());

        ((TextView) reportDialog.findViewById(R.id.tv_title)).setText("닉네임 변경하기");
        ((TextView) reportDialog.findViewById(R.id.tv_content)).setVisibility(View.GONE);
        TextView tvSend = (TextView) reportDialog.findViewById(R.id.tv_send_code);
        tvSend.setText("변경하기");
        TextView tvCancel = (TextView) reportDialog.findViewById(R.id.tv_cancel);

        EditText etInviteCode = (EditText) reportDialog.findViewById(R.id.et_code);

        etInviteCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0)
                    enabledSendButton(tvSend, false);
                else
                    enabledSendButton(tvSend, true);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        tvSend.setOnClickListener(v -> {
            String nickName = etInviteCode.getText().toString();
            User UpdateUserInfo = new User();
            UpdateUserInfo.setName(nickName);
            Call<User> request = NetRetrofit.getInstance(getContext()).getService().updateUserInfo(UpdateUserInfo);
            request.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "닉네임 변경에 성공하였습니다", Toast.LENGTH_SHORT).show();
                        getMyInfo();

                        reportDialog.dismiss();
                    } else {
                        try {
                            Utils.toastError(getContext(), response);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    t.printStackTrace();
                }
            });
        });

        tvCancel.setOnClickListener(v -> {
            reportDialog.dismiss();
        });

    }

    public void getMyInfo() {
        Call<User> request = NetRetrofit.getInstance(getActivity()).getService().checkCurrentUserInfo();
        request.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    myInfo = response.body();
                    Gson gson = new Gson();
                    String stringUserInfo = gson.toJson(myInfo);
                    SharedPrefHelper.getInstance(getActivity()).setSharedPreferences(SharedPrefHelper.USER_INFO, stringUserInfo);
                    loadMyNameAndProfileImage();
                    Log.d(TAG, "gender: " + myInfo.getGender() +
                            "\nlat: " + myInfo.getLat() +
                            "\nlng: " + myInfo.getLng() +
                            "\nprofileImageUrl: " + myInfo.getProfileImageUrl() +
                            "\nbirth: " + myInfo.getBirth() +
                            "\nname: " + myInfo.getName());
                } else {
                    try {
                        Utils.toastError(getActivity(), response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public void enabledSendButton(TextView sendButton, boolean enabled) {
        if (enabled) {
            sendButton.setBackgroundResource(R.drawable.background_round_main_color);
            sendButton.setTextColor(getResources().getColor(R.color.main_color));
        } else {
            sendButton.setBackgroundResource(R.drawable.background_grey_stroke);
            sendButton.setTextColor(getResources().getColor(R.color.grey));
        }

        sendButton.setEnabled(enabled);
    }

    public void checkRadioButton(ImageView radioView, boolean isChecked) {
        if (isChecked)
            radioView.setBackgroundResource(R.drawable.radio_on);
        else
            radioView.setBackgroundResource(R.drawable.radio_off);

        int id = radioView.getId();
        switch (id) {
            case R.id.sw_basic_push:
                isCheckedBasicPush = isChecked;
                break;
            case R.id.sw_reply_push:
                isCheckedReplyPush = isChecked;
                break;
        }
    }


}
