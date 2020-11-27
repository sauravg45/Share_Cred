package com.example.credpass.screen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.credpass.MainActivity;
import com.example.credpass.R;
import com.example.credpass.firebase.FireBaseAndLocalQuery;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mukesh.OnOtpCompletionListener;
import com.mukesh.OtpView;

import java.util.Map;

public class OtpVerification extends AppCompatActivity {
    private OtpView otpEt;
    private MaterialButton submitBu;
    private String verificationCode;
    private TextView guidelineText;
    Context mcontext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);
        otpEt = findViewById(R.id.otpEt);
        submitBu = findViewById(R.id.submitButton);
        guidelineText = findViewById(R.id.otp_guideline);
        mcontext=this;
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
                            Intent intent = getIntent();
                            FireBaseAndLocalQuery.savePhoneNo(mcontext,intent.getExtras().getString("phoneNo"));
                            FirebaseAuth auth = FirebaseAuth.getInstance();
                            String userId = auth.getCurrentUser().getUid();
                            checkAndLoadPrevData(mcontext,userId,getApplicationContext().getPackageName());
//                            startActivity(new Intent(OtpVerification.this, MainActivity.class));
//                            finish();
                        } else {
                            Toast.makeText(OtpVerification.this,"Incorrect OTP",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void checkAndLoadPrevData(Context mcontext, String uid, String packageName){

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference fDatabase=database.getReference();
        DatabaseReference fdbRef= fDatabase.child(FireBaseAndLocalQuery.sUsers).child(uid);
        fdbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue()==null){
                    return;
                }else{
                    Map<String,String> dataValue=(Map<String, String>) snapshot.getValue();
                    SharedPreferences sharedpreferences = mcontext.getSharedPreferences(FireBaseAndLocalQuery.MyPREFERENCES, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedpreferences.edit();

                    if(dataValue.containsKey(FireBaseAndLocalQuery.picUrl)){
                        Bitmap imgBitMap=FireBaseAndLocalQuery.getBitmapFromURL(dataValue.get(FireBaseAndLocalQuery.picUrl));
                        FireBaseAndLocalQuery.storeImage(imgBitMap ,packageName);
                    }
                    if(dataValue.containsKey(FireBaseAndLocalQuery.sPhone)){
                        editor.putString(FireBaseAndLocalQuery.sPhone,dataValue.get(FireBaseAndLocalQuery.sPhone));
                    }
                    if(dataValue.containsKey(FireBaseAndLocalQuery.sUsers)){
                        editor.putString(FireBaseAndLocalQuery.sUsers,dataValue.get(FireBaseAndLocalQuery.sUsers));
                    }
                    editor.commit();
                    startActivity(new Intent(OtpVerification.this, MainActivity.class));
                    finish();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                return;
            }
        });
    }

}