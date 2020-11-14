package com.example.credpass.screen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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


    public static final String TAG="FirstScreen";
    FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        String redHeart = "\u2764\uFE0F";
        String madeWithLoveString = "Made with " + Html.fromHtml(redHeart, Html.FROM_HTML_MODE_LEGACY) + " in India";
        TextView txtView = (TextView) findViewById(R.id.first_screen_text);
        txtView.setText(madeWithLoveString);
        auth = FirebaseAuth.getInstance();
        FirebaseUser user=auth.getCurrentUser();
        int splashScreenTimeOut = 3;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(user!=null){
                    startActivity(new Intent(FirstScreen.this, MainActivity.class));
                    finish();
                }else{
                    startActivity(new Intent(FirstScreen.this, Login.class));
                    finish();
                }
            }
        }, splashScreenTimeOut*1000);


        
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
                Log.d("First Screen", msg);
                Toast.makeText(FirstScreen.this, msg, Toast.LENGTH_SHORT).show();
            }
        });

    }
  
}