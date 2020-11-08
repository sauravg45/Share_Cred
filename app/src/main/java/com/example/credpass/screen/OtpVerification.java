package com.example.credpass.screen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.credpass.MainActivity;
import com.example.credpass.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.mukesh.OnOtpCompletionListener;
import com.mukesh.OtpView;

public class OtpVerification extends AppCompatActivity {
    private OtpView otpEt;
    private MaterialButton submitBu;
    private String verificationCode;
    private TextView guidelineText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);
        otpEt = findViewById(R.id.otpEt);
        submitBu = findViewById(R.id.submitButton);
        guidelineText = findViewById(R.id.otp_guideline);

        Intent intent = getIntent();
        verificationCode = intent.getExtras().getString("verificationCode");
        String otpGuidline = getString(R.string.otp_verification_guildeline) + " " + intent.getExtras().getString("phoneNo");
        guidelineText.setText(otpGuidline);
        otpEt.setOtpCompletionListener(new OnOtpCompletionListener() {
            @Override
            public void onOtpCompleted(String otp) {
                Toast.makeText(OtpVerification.this, otp, Toast.LENGTH_SHORT).show();
            }
        });

        submitBu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String otp = otpEt.getText().toString();
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCode, otp);
                SigninWithPhone(credential);
            }
        });

    }

    private void SigninWithPhone(PhoneAuthCredential credential) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(OtpVerification.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(OtpVerification.this,"Incorrect OTP",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}