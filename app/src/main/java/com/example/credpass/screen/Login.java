package com.example.credpass.screen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.credpass.MainActivity;
import com.example.credpass.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import static android.icu.text.Normalizer.YES;

public class Login extends AppCompatActivity {

    Button getOtpBu;
//    Button submitBu;
    TextInputLayout phoneNoEt;
//    EditText otpEt;
    String phoneNumber, otp;
    FirebaseAuth auth;
    private String verificationCode;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        auth = FirebaseAuth.getInstance();
        attachElements();
        StartFirebaseLogin();
        getOtpBu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                phoneNumber=phoneNoEt.getEditText().getText().toString();
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        phoneNumber,                     // Phone number to verify
                        60,                           // Timeout duration
                        TimeUnit.SECONDS,                // Unit of timeout
                        Login.this,        // Activity (for callback binding)
                        mCallback);                      // OnVerificationStateChangedCallbacks
            }
        });

//        submitBu.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                otp=otpEt.getText().toString();
//                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCode, otp);
//                SigninWithPhone(credential);
//            }
//        });
    }

    private void attachElements(){
//        otpEt=findViewById(R.id.otpEt);
        phoneNoEt=findViewById(R.id.editTextPhone);
        getOtpBu=findViewById(R.id.getOtpButton);
//        submitBu=findViewById(R.id.submitButton);
    }

    private void StartFirebaseLogin() {


        mCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                toast("verified");
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {

                toast("Verification Failed ");

            }


            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                verificationCode = s;

                Intent i = new Intent(Login.this, OtpVerification.class);
                i.putExtra("verificationCode", s);
                i.putExtra("phoneNo", phoneNumber);
                startActivity(i);

            }
        };
    }

    public void SigninWithPhone(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(Login.this, MainActivity.class));
                            finish();
                        } else {
                            toast("Incorrect OTP");

                        }
                    }
                });
    }

    private void toast(String text){
        Toast.makeText(Login.this,text, Toast.LENGTH_SHORT).show();
    }

}