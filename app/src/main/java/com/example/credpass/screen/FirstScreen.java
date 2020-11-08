package com.example.credpass.screen;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;

import com.example.credpass.MainActivity;
import com.example.credpass.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FirstScreen extends AppCompatActivity {

    FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        String unicode = "\u2764\uFE0F";
        String madeWithLoveString = "Made with " + Html.fromHtml(unicode, Html.FROM_HTML_MODE_LEGACY) + " in India";
        TextView txtView = (TextView) findViewById(R.id.first_screen_text);
        txtView.setText(madeWithLoveString);
        auth = FirebaseAuth.getInstance();
        FirebaseUser user=auth.getCurrentUser();

        if(user!=null){
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }else{
            startActivity(new Intent(this, Login.class));
            finish();
        }
    }
}