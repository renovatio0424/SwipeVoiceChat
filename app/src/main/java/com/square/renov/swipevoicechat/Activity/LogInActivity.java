package com.square.renov.swipevoicechat.Activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.igaworks.IgawCommon;
import com.igaworks.adbrix.IgawAdbrix;
import com.onesignal.OneSignal;
import com.square.renov.swipevoicechat.Handler.BackPressCloseHandler;
import com.square.renov.swipevoicechat.Model.Result;
import com.square.renov.swipevoicechat.Model.User;
import com.square.renov.swipevoicechat.Network.NetRetrofit;
import com.square.renov.swipevoicechat.Network.ApiService;
import com.square.renov.swipevoicechat.R;
import com.square.renov.swipevoicechat.Util.AdbrixUtil;
import com.square.renov.swipevoicechat.Util.SharedPrefHelper;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.internal.CallbackManagerImpl;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.square.renov.swipevoicechat.Util.Utils;

import java.io.IOException;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LogInActivity extends Activity {

    private static final int GOOGLE_SIGN_IN = 1000;
    private static final String TAG = LogInActivity.class.getSimpleName();
    private static final int FACEBOOK_SIGN_IN = 64206;
    GoogleSignInClient mGoogleSignInClient;
    GoogleSignInAccount account;

    public String SNSTYPE_GOOGLE = "GOOGLE";
    public String SNSTYPE_FACEBOOK = "FACEBOOK";

    private FirebaseAnalytics firebaseAnalytics;

    @BindView(R.id.facebook_sign_up_button)
    Button facebookSignUpButton;

    CallbackManager callbackManager;

    @BindView(R.id.sign_in_google)
    SignInButton signInGoogle;
    @BindView(R.id.google_sign_up_button)
    Button googleSignUpButton;

    ApiService Service = NetRetrofit.getInstance(this).getService();


    boolean isOurUser = false;
    private BackPressCloseHandler backPressCloseHandler = new BackPressCloseHandler(this);

    Unbinder unbinder;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        onNewIntent(LogInActivity.this.getIntent());
        unbinder = ButterKnife.bind(this);

        initFirebaseAnalystics();
        AdbrixUtil.setFirstTimeExperience(this, SharedPrefHelper.LOGIN);

        initGoogleSignIn();
//        TODO : 로그인 유저 정보 받아서 넘기기
    }

    private void goToTutorial() {
        Intent intent = new Intent(this, TutorialActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        IgawCommon.registerReferrer(LogInActivity.this);
    }

    private void initAdbrix() {
        if (SharedPrefHelper.getInstance(this).hadExperiencedSignUpStep(SharedPrefHelper.LOGIN)) {
            SharedPrefHelper.getInstance(this).setSharedPreferences(SharedPrefHelper.SIGN_UP_STEP, SharedPrefHelper.LOGIN);
            IgawAdbrix.firstTimeExperience(getString(R.string.login));
        }
    }

    private void initFirebaseAnalystics() {
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "test id");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "test name");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "image");
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }


    /**
     * <public_profile>
     * id
     * first_name
     * last_name
     * middle_name
     * name
     * name_format
     * picture
     * short_name
     * <p>
     * <user_status>
     * email
     * user_age_range
     * user_birthday
     * user_friends
     * user_gender
     * user_hometown
     * user_link
     * user_location
     **/


    private void initFacebookSignIn() {
        callbackManager = CallbackManager.Factory.create();
//        signInFacebook.setReadPermissions(Arrays.asList("public_profile ", "user_status"));
        LoginManager.getInstance().logInWithReadPermissions(LogInActivity.this,
                Arrays.asList("public_profile", "email"));
    }

    private Callback<User> returnCallback(String snsType, String Token) {
        return new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                // Signed in successfully, show authenticated UI.
                if (response.isSuccessful()) {
//                    hellovoiceauth token set
                    Log.d(TAG, "header: " + response.headers());
                    String token = response.headers().get("HelloVoiceAuth");
//                    Log.d(TAG, "token: " + token);

                    User myInfo = response.body();

                    Gson gson = new Gson();
                    SharedPrefHelper.getInstance(LogInActivity.this).setSharedPreferences(SharedPrefHelper.ACCESS_TOKEN, token);
                    SharedPrefHelper.getInstance(LogInActivity.this).setSharedPreferences(SharedPrefHelper.SNS_TYPE, snsType);
                    SharedPrefHelper.getInstance(LogInActivity.this).setSharedPreferences(SharedPrefHelper.USER_INFO, gson.toJson(myInfo));

                    Log.d(TAG, "gender: " + myInfo.getGender() +
                            "\nlat: " + myInfo.getLat() +
                            "\nlng: " + myInfo.getLng() +
                            "\nprofileImageUrl: " + myInfo.getProfileImageUrl() +
                            "\nbirth: " + myInfo.getBirth());

                    Log.e(TAG, "user.tostring(): " + gson.toJson(myInfo));

                    if (myInfo.getProfileImageUrl() == null || "".equals(myInfo.getProfileImageUrl()))
                        moveToProfile(myInfo);
                    else {
                        moveToMain();
                        OneSignal.sendTag("userId", String.valueOf(myInfo.getId()));
                    }
                } else {
                    switch (response.code()) {
                        case 400://회원 가입해야 하는 유저

                            Result result = null;
                            try {
                                result = Utils.parseError(response);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            if (!"회원 가입을 먼저 진행해주세요.".equals(result.getMessage())) {
                                Toast.makeText(LogInActivity.this, result.getMessage(), Toast.LENGTH_SHORT).show();
                                break;
                            }

                            if (snsType.equals(SNSTYPE_FACEBOOK)) {
                                Profile profile = Profile.getCurrentProfile();

                                moveToSignUp(snsType, Token, profile.getName(), profile.getProfilePictureUri(200, 300).toString());
                                Log.e("facebook", "first name: " + profile.getFirstName());
                                Log.e("facebook", "Last name: " + profile.getLastName());
                                Log.e("facebook", "Middle name: " + profile.getMiddleName());
                                Log.e("facebook", "name: " + profile.getName());
                                Log.e("facebook", "picture uri: " + profile.getProfilePictureUri(200, 300).toString());
                            } else if (snsType.equals(SNSTYPE_GOOGLE)) {
                                String googleToken = account.getIdToken();
                                String userName = account.getDisplayName();
                                Uri personPhoto = account.getPhotoUrl();

                                Log.e("retrofit", "google token: " + googleToken);
                                Log.e("google", "User info");
                                Log.e("google", "name: " + userName);
                                Log.e("google", "personPhoto: " + personPhoto.getPath());
                                moveToSignUp(snsType, Token, userName, personPhoto.toString());
                            }
                            break;
                        default:
                            try {
                                Utils.toastError(getApplicationContext(), response);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                }


            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(LogInActivity.this, "fail: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
    }


    private void initGoogleSignIn() {
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);
        account = GoogleSignIn.getLastSignedInAccount(this);
        // Set the dimensions of the sign-in button.
        signInGoogle.setSize(SignInButton.SIZE_WIDE);
        signInGoogle.setOnClickListener(v -> signIn());
    }

    @OnClick(R.id.google_sign_up_button)
    public void onClickGoogleSignUpButton() {
//        signInGoogle.performClick();
        signIn();
    }

    @OnClick(R.id.facebook_sign_up_button)
    public void onClickFacebookSignUpButton() {
        //TODO UI TEST
//        moveToSignUp(null,null,null,null);
//        return;

        callbackManager = CallbackManager.Factory.create();
//        signInFacebook.setReadPermissions(Arrays.asList("public_profile ", "user_status"));
        LoginManager.getInstance().logInWithReadPermissions(LogInActivity.this,
                Arrays.asList("public_profile", "email"));
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                String facebookToken = loginResult.getAccessToken().getToken();
                Log.e("facebook", "facebook token: " + facebookToken);
                Call<User> request = Service.login(facebookToken, "FACEBOOK");
                request.enqueue(returnCallback(SNSTYPE_FACEBOOK, facebookToken));
                Log.d("facebook", "login result: " + loginResult.getAccessToken().getToken() + ",\n" + loginResult.getAccessToken().getUserId());
            }

            @Override
            public void onCancel() {
                Toast.makeText(LogInActivity.this, "onCancel", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(LogInActivity.this, "onError", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(TAG, "onActivityResult(): " + requestCode + ", " + resultCode);
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == GOOGLE_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        } else if (requestCode == FACEBOOK_SIGN_IN) {
            CallbackManagerImpl.RequestCodeOffset.Share.toRequestCode();
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Call<ArrayList<JsonObject>> res = NetRetrofit.getInstance().getService().getListRepos(id);
     * res.enqueue(new Callback<ArrayList<JsonObject>>() {
     *
     * @Override public void onResponse(Call<ArrayList<JsonObject>> call, Response<ArrayList<JsonObject>> response) {
     * Log.d("Retrofit", response.toString());
     * if (response.body() != null)
     * textView.setText(response.body().toString());
     * }
     * @Override public void onFailure(Call<ArrayList<JsonObject>> call, Throwable t) {
     * Log.e("Err", t.getMessage());
     * }
     * });
     */

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            account = completedTask.getResult(ApiException.class);

            String googleToken = account.getIdToken();

            Log.e("retrofit", "google token: " + googleToken);
            Call<User> request = Service.login(googleToken, SNSTYPE_GOOGLE);

            request.enqueue(returnCallback(SNSTYPE_GOOGLE, googleToken));

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            // https://developers.google.com/android/reference/com/google/android/gms/auth/api/signin/GoogleSignInStatusCodes
            int errorCode = e.getStatusCode();
            String errorMessage = "";
            switch (errorCode) {
                case GoogleSignInStatusCodes.NETWORK_ERROR:
                    errorMessage = "인터넷 연결을 확인해주세요";
                    break;
                default:
                    errorMessage = GoogleSignInStatusCodes.getStatusCodeString(e.getStatusCode());
                    break;
            }
            Toast.makeText(this, errorMessage + ": " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
            Log.w(TAG, "signInResult:failed code=" + errorCode);

        }
    }

    private void moveToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("duration", 1000);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void moveToProfile(User user) {

        String type = SharedPrefHelper.getInstance(LogInActivity.this).getSharedPreferences(SharedPrefHelper.SNS_TYPE, null);
        Intent intent = new Intent(this, ProfileActivity.class);

        if ("GOOGLE".equals(type)) {
//            intent.putExtra("profile", account.getPhotoUrl().toString());
            Log.e(TAG, "google profile: " + account.getPhotoUrl().toString());
        } else if ("FACEBOOK".equals(type)) {
            intent.putExtra("profile", Profile.getCurrentProfile().getProfilePictureUri(200, 300).toString());
            Log.e(TAG, "facebook profile: " + Profile.getCurrentProfile().getProfilePictureUri(200, 300).toString());
        }

        intent.putExtra("user", user);
        startActivity(intent);
    }

    private void moveToSignUp(String SnsType, String token, String name, String profileImagePath) {
        Log.e(TAG, "sns Type: " + SnsType + "\ntoken: " + token);
        Log.e(TAG, "name: " + name);
        Log.e(TAG, "profile: " + profileImagePath);

        Intent intent = new Intent(this, SignUpActivity.class);
        intent.putExtra("type", SnsType);
        intent.putExtra("token", token);
        intent.putExtra("name", name);
        intent.putExtra("profile", profileImagePath);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
//        profileTracker.stopTracking();
    }

    @Override
    public void onBackPressed() {
        backPressCloseHandler.onBackPressed();
    }

    @OnClick(R.id.tv_terms_location)
    public void onClickTermsLocation() {
        Intent intent = new Intent(this, WebActivity.class);
        intent.putExtra("name", "location");
        startActivity(intent);
    }

    @OnClick(R.id.tv_terms_policy)
    public void onClickTermsPolicy() {
        Intent intent = new Intent(this, WebActivity.class);
        intent.putExtra("name", "policy");
        startActivity(intent);
    }

    @OnClick(R.id.tv_terms_privacy)
    public void onClickTermsPrivacy() {
        Intent intent = new Intent(this, WebActivity.class);
        intent.putExtra("name", "privacy");
        startActivity(intent);
    }
}
