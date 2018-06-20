package com.example.renov.swipevoicechat.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.example.renov.swipevoicechat.Model.Result;
import com.example.renov.swipevoicechat.Model.User;
import com.example.renov.swipevoicechat.Network.NetRetrofit;
import com.example.renov.swipevoicechat.Network.RetrofitService;
import com.example.renov.swipevoicechat.R;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.internal.CallbackManagerImpl;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LogInActivity extends AppCompatActivity {

    private static final int GOOGLE_SIGN_IN = 1000;
    private static final String TAG = LogInActivity.class.getSimpleName();
    private static final int FACEBOOK_SIGN_IN = 64206;
    GoogleSignInClient mGoogleSignInClient;

    public String SNSTYPE_GOOGLE = "GOOGLE";
    public String SNSTYPE_FACEBOOK = "FACEBOOK";

    @BindView(R.id.facebook_sign_up_button)
    Button facebookSignUpButton;

    CallbackManager callbackManager;

    @BindView(R.id.sign_in_google)
    SignInButton signInGoogle;
    @BindView(R.id.google_sign_up_button)
    Button googleSignUpButton;

    RetrofitService Service = NetRetrofit.getInstance().getService();

    boolean isOurUser = false;

    Unbinder unbinder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);

        unbinder = ButterKnife.bind(this);

        initGoogleSignIn();
        initFacebookSignIn();

        if (isLoggedUser())
            moveToMain();
    }

    private boolean isLoggedUser() {
//        TODO: 로그인시 토큰값 발행 -> 어플 재실행시 토큰값 확인후 바로 로그인
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        boolean isFacebookLoggedIn = accessToken != null && !accessToken.isExpired();
        boolean isGoogleLoggedIn = account != null;
        if (isFacebookLoggedIn && isOurUser | isGoogleLoggedIn && isOurUser) {
            return true;
        } else {
            return false;
        }
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

    private Callback<Result> returnCallback(String snsType, String Token) {
        return new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                // Signed in successfully, show authenticated UI.
                if (response.isSuccessful()) {
                    moveToMain();
                } else {
                    switch (response.code()) {
                        case 400://회원 가입해야 하는 유저
                            String errorBody = null;
                            try {
                                errorBody = response.errorBody().string();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            Log.d(TAG, "header:" + response.headers() + "\nbody: " + errorBody);
                            String mJsonString = errorBody;
                            JsonParser parser = new JsonParser();
                            JsonElement mJson = parser.parse(mJsonString);
                            Gson gson = new Gson();
                            Result result = gson.fromJson(mJson, Result.class);

                            if ("회원 가입을 먼저 진행해주세요.".equals(result.getMessage()))
                                moveToSignUp(snsType, Token);
                            break;

                        default:
                            try {
                                Toast.makeText(LogInActivity.this, "error body: " + response.errorBody().string(), Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "error body: " + response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                }


            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                Toast.makeText(LogInActivity.this, "fail: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
    }


    private void initGoogleSignIn() {
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
        callbackManager = CallbackManager.Factory.create();
//        signInFacebook.setReadPermissions(Arrays.asList("public_profile ", "user_status"));
        LoginManager.getInstance().logInWithReadPermissions(LogInActivity.this,
                Arrays.asList("public_profile", "email"));
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                String facebookToken = loginResult.getAccessToken().getToken();
                Log.e("facebook", "facebook token: " + facebookToken);
                Call<Result> response = Service.login(facebookToken, "FACEBOOK");
                response.enqueue(returnCallback(SNSTYPE_FACEBOOK, facebookToken));
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
        Toast.makeText(this, "onActivityResult(): " + requestCode + ", " + resultCode, Toast.LENGTH_SHORT).show();
        Log.e(TAG,"onActivityResult(): " + requestCode + ", " + resultCode);
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
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            String googleToken = account.getIdToken();

            Log.e("retrofit", "google token: " + googleToken);

            Call<Result> response = Service.login(googleToken, "GOOGLE");

            response.enqueue(returnCallback(SNSTYPE_GOOGLE, googleToken));

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            // https://developers.google.com/android/reference/com/google/android/gms/auth/api/signin/GoogleSignInStatusCodes
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());

            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void moveToMain() {
        LoginManager.getInstance().logOut();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void moveToSignUp(String SnsType, String token) {
        Log.e(TAG, "sns Type: " + SnsType + "\ntoken: " + token);

        Intent intent = new Intent(this, SignUpActivity.class);
        intent.putExtra("type", SnsType);
        intent.putExtra("token", token);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
//        profileTracker.stopTracking();
    }
}
