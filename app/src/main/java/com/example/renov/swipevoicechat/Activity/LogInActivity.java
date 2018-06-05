package com.example.renov.swipevoicechat.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.example.renov.swipevoicechat.R;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class LogInActivity extends AppCompatActivity {

    private static final int GOOGLE_SIGN_IN = 1000;
    private static final String TAG = LogInActivity.class.getSimpleName();
    private static final int FACEBOOK_SIGN_IN = 64206;
    GoogleSignInClient mGoogleSignInClient;
    @BindView(R.id.sign_in_facebook)
    LoginButton signInFacebook;
    CallbackManager callbackManager;

    @BindView(R.id.sign_in_google)
    SignInButton signInGoogle;


    boolean isOurUser = false;

    Unbinder unbinder;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        unbinder = ButterKnife.bind(this);

        initGoogleSignIn();
        initFacebookSignIn();

        if(isLoggedUser())
            moveToMain();
        else
            moveToSignUp();
    }

    private boolean isLoggedUser() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        boolean isFacebookLoggedIn = accessToken != null && !accessToken.isExpired();
        boolean isGoogleLoggedIn = account != null;
        if(isFacebookLoggedIn && isOurUser | isGoogleLoggedIn && isOurUser){
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
     *
     * <user_status>
     * email
     user_age_range
     user_birthday
     user_friends
     user_gender
     user_hometown
     user_link
     user_location
     * */

    private void initFacebookSignIn() {


        callbackManager = CallbackManager.Factory.create();
//        signInFacebook.setReadPermissions(Arrays.asList("public_profile ", "user_status"));
        signInFacebook.setReadPermissions(Arrays.asList("public_profile "));
        signInFacebook.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                Toast.makeText(LogInActivity.this, "onSuccess", Toast.LENGTH_SHORT).show();

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

    private void moveToMain() {
        LoginManager.getInstance().logOut();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


    private void initGoogleSignIn() {

        // Set the dimensions of the sign-in button.

        signInGoogle.setSize(SignInButton.SIZE_WIDE);
        signInGoogle.setOnClickListener(v -> signIn());
        findViewById(R.id.button2).setOnClickListener(v -> {
//            TODO: 페북 로그아웃 확인
            LoginManager.getInstance().logOut();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Toast.makeText(this, "onActivityResult(): " + requestCode + ", " +resultCode, Toast.LENGTH_SHORT).show();
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == GOOGLE_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        } else if (requestCode == FACEBOOK_SIGN_IN){
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Signed in successfully, show authenticated UI.
            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }

    private void updateUI(GoogleSignInAccount account) {
        if(account != null && isOurUser){
//            TODO: 서버에서 해당 유저가 가입했는지 확인
            Log.d("google login", "profile image: " + account.getPhotoUrl());
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else {
            moveToSignUp();
        }
    }

    private void moveToSignUp(){
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
//        profileTracker.stopTracking();
    }
}
