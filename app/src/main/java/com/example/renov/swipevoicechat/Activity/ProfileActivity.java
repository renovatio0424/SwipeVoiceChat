package com.example.renov.swipevoicechat.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.example.renov.swipevoicechat.Model.User;
import com.example.renov.swipevoicechat.Network.NetRetrofit;
import com.example.renov.swipevoicechat.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Retrofit;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = ProfileActivity.class.getSimpleName();
    @BindView(R.id.iv_crop)
    ImageView ivCrop;
    @BindView(R.id.btn_complete)
    TextView btnComplete;
    @BindView(R.id.btn_profile_register)
    Button btnProfileRegister;

    Unbinder unbinder;

    Uri mCropImageUri;
    private int CAMERA_CODE = 1;
    private int GALLERY_CODE = 2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile);

        unbinder = ButterKnife.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @OnClick(R.id.iv_back)
    public void onClickBack() {
        finish();
    }

    @OnClick(R.id.btn_complete)
    public void onClickComplete() {
//        TODO: 프로필 사진 업로드
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @SuppressLint("NewApi")
    @OnClick(R.id.iv_crop)
    public void onClickCrop() {
        if (CropImage.isExplicitCameraPermissionRequired(this)) {
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE);
            return;
        }

        //        else
//            new MaterialDialog.Builder(ProfileActivity.this)
//                    .title("프로필 등록하기")
//                    .items(R.array.profile)
//                    .itemsCallbackSingleChoice(0, (dialog, itemView, which, text) -> {
//                        switch (which) {
//                            case 0:
//                                selectCamera();
//                                break;
//                            case 1:
//                                selectGallery();
//                                break;
//                        }
//                        return true;
//                    })
//                    .positiveText("확인")
//                    .negativeText("취소")
//                    .show();


//        권한 요청
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMinCropResultSize(200, 300)
                .setFixAspectRatio(true)
                .setAspectRatio(3, 4)
                .setAllowFlipping(false)
                .setAllowRotation(false)
                .start(this);
    }

    @OnClick(R.id.btn_profile_register)
    public void onClickButtonProfileRegister() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMinCropResultSize(200, 300)
                .setFixAspectRatio(true)
                .setAspectRatio(3, 4)
                .setAllowFlipping(false)
                .setAllowRotation(false)
                .start(this);
    }

    private void selectCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        Uri outputFileUri = Uri.fromFile(new File(this.getExternalCacheDir().getPath(), "pickImageResult.jpeg"));
        File newFile = new File(getApplicationContext().getExternalCacheDir(), "pickImageResult.jpeg");

        Uri outputFileUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", newFile);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        startActivityForResult(intent, CAMERA_CODE);
    }

    private void selectGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_CODE);
        CropImage.startPickImageActivity(this);
    }

    @Override
    @SuppressLint("NewApi")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // handle result of pick image chooser
        if (requestCode == CAMERA_CODE && resultCode == Activity.RESULT_OK || requestCode == GALLERY_CODE && resultCode == Activity.RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(this, data);
            Log.d(TAG, "image uri: " + imageUri.toString());
            // For API >= 23 we need to check specifically that we have permissions to read external storage.
            if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {
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
//                Uri resultUri = mCropImageUri;
                ivCrop.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectGallery();
            } else {
                Toast.makeText(this, "Cancelling, required permissions are not granted", Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
            if (mCropImageUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // required permissions granted, start crop image activity
                selectCamera();
            } else {
                Toast.makeText(this, "Cancelling, required permissions are not granted", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startCropImageActivity(Uri imageUri) {
        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMinCropResultSize(200, 300)
                .setFixAspectRatio(true)
                .setAspectRatio(3, 4)
                .setAllowFlipping(false)
                .setAllowRotation(false)
                .start(this);
    }
}
