package com.example.minhajlib.elaaj1;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ResultCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Arrays;

import static android.os.Build.VERSION_CODES.M;
import static com.firebase.ui.auth.ResultCodes.OK;

public class SignInActivity extends AppCompatActivity {

    //declare constant
    private final int RC_SIGN_IN = 100;

    Button btnGoogle, btnFacebook, btnPhone;
    FirebaseAuth auth;

    Constants mConstants;
    MainActivity mMainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        auth = FirebaseAuth.getInstance();
        mConstants = Constants.getInstance();
        mMainActivity = new MainActivity();


        btnGoogle = (Button) findViewById(R.id.btn_google);
        btnFacebook = (Button) findViewById(R.id.btn_facebook);
        btnPhone = (Button) findViewById(R.id.btn_phone);

        btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleSignIn();
            }
        });
        btnFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                facebookSignIn();
            }
        });
        btnPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneSignIn();
            }
        });
    }

    private void googleSignIn() {
        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                .setIsSmartLockEnabled(true)
                .setTheme(R.style.AppTheme)
                .build(), RC_SIGN_IN);
    }

    private void facebookSignIn() {
        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build()))
                .setIsSmartLockEnabled(false)
                .setTheme(R.style.AppTheme)
                .build(), RC_SIGN_IN);
    }

    private void phoneSignIn() {
        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build()))
                .setIsSmartLockEnabled(true)
                .setTheme(R.style.AppTheme)
                .build(), RC_SIGN_IN);
    }

    //get result of signInIntent; handle sign in flow
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("xyz", "inside sign in onactivity result");
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == OK) {
                Log.d("xyz", "sign in result ok");
                authenticateWithFirebase(response);     //after sign in success authenticate this user with firebase
                //finish();
            } else {
                //is response is not ok; user cancel sign in flow
                if (response == null) {
                    mConstants.displayToast(this, getString(R.string.msg_sign_stop), Toast.LENGTH_SHORT);
                } else if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    mConstants.displayToast(this, getString(R.string.msg_no_internet), Toast.LENGTH_LONG);
                } else if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    mConstants.displayToast(this, getString(R.string.msg_some_error), Toast.LENGTH_LONG);
                }
            }
        }
    }//end onActivityResult

    //authenticate with firebase
    private void authenticateWithFirebase(IdpResponse response) {
        String provider = response.getProviderType();
        String token = response.getIdpToken();
        //Log.d("xyz", " token: " + token);
        //Log.d("xyz", "provider: " + provider);
        AuthCredential authCredential = null;

        if (provider.equals("facebook.com")) {
            authCredential = FacebookAuthProvider.getCredential(token);
        } else if (provider.equals("google.com")) {
            authCredential = GoogleAuthProvider.getCredential(token, null);
        } else {
            //authCredential = PhoneAuthProvider.getCredential(token, null);
            mConstants.displayToast(SignInActivity.this, getString(R.string.msg_signin_success), Toast.LENGTH_SHORT);
            //new MainActivity().mReplaceDonorsList();
            startMainActivity();
            return;
        }
        auth.signInWithCredential(authCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    mConstants.displayToast(SignInActivity.this, getString(R.string.msg_signin_success), Toast.LENGTH_SHORT);
                    startMainActivity();
                } else {
                    mConstants.displayToast(SignInActivity.this, getString(R.string.msg_signin_failed), Toast.LENGTH_SHORT);
                }
            }
        });
    }

    private void startMainActivity() {
        setResult(OK);
        finish();
    }
}