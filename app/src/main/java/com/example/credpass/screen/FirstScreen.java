package com.example.credpass.screen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.KeyguardManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.credpass.MainActivity;
import com.example.credpass.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

public class FirstScreen extends AppCompatActivity {


    public static final String TAG = "FirstScreen";
    FirebaseAuth auth;
    public static final int CODE_AUTHENTICATION_VERIFICATION = 29;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        KeyguardManager km = (KeyguardManager)getSystemService(KEYGUARD_SERVICE);
        if(km.isKeyguardSecure()) {

            Intent i = km.createConfirmDeviceCredentialIntent("Authentication required", "password");
            startActivityForResult(i, CODE_AUTHENTICATION_VERIFICATION);
        }
        else{
            Toast.makeText(this, "No any security setup done by user(pattern or password or pin or fingerprint", Toast.LENGTH_SHORT).show();
        }


        setContentView(R.layout.activity_first);

        String redHeart = "\u2764\uFE0F";
        String madeWithLoveString = "Made with " + Html.fromHtml(redHeart, Html.FROM_HTML_MODE_LEGACY) + " in India";
        TextView txtView = (TextView) findViewById(R.id.first_screen_text);
        txtView.setText(madeWithLoveString);
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                    return;
                }

                // Get new FCM registration token
                String token = task.getResult();


                // Log and toast
                String msg = getString(R.string.fcm_token, token);
             //   Toast.makeText(FirstScreen.this, msg, Toast.LENGTH_SHORT).show();
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK && requestCode==CODE_AUTHENTICATION_VERIFICATION)
        {
            int splashScreenTimeOut = 3;
            auth = FirebaseAuth.getInstance();
            FirebaseUser user = auth.getCurrentUser();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (user != null) {
                        startActivity(new Intent(FirstScreen.this, MainActivity.class));
                        finish();
                    } else {
                        startActivity(new Intent(FirstScreen.this, Login.class));
                        finish();
                    }
                }
            }, splashScreenTimeOut * 1000);
        }
        else
        {
            Toast.makeText(this, "Failure: Unable to verify user's identity", Toast.LENGTH_SHORT).show();
        }
    }

}