package com.example.credpass.screen;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.credpass.DTO.OnGetDataListener;
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
    public static final int CAMERA_STORAGE_REQUEST_CODE = 611;
    public static final int ONLY_CAMERA_REQUEST_CODE = 612;
    public static final int ONLY_STORAGE_REQUEST_CODE = 613;
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
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference fDatabase=database.getReference();
                            DatabaseReference fdbRef= fDatabase.child(FireBaseAndLocalQuery.sUsers).child(userId);
                           // setUser(fdbRef,getApplicationContext().getPackageName());
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
                        if(checkSelfPermissions(OtpVerification.this)) {
                            Bitmap imgBitMap = FireBaseAndLocalQuery.getBitmapFromURL(dataValue.get(FireBaseAndLocalQuery.picUrl));
                            FireBaseAndLocalQuery.storeImage(imgBitMap, packageName);
                        }
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

    @Override
    public void onBackPressed() {
        Intent setIntent = new Intent(OtpVerification.this, Login.class);
        startActivity(setIntent);
        finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public  boolean checkSelfPermissions(@NonNull Activity activity) {
        if (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, ONLY_STORAGE_REQUEST_CODE);
            return false;
        }
        return true;
    }

   /* public void readData(DatabaseReference ref, final OnGetDataListener listener) {
        listener.onStart();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listener.onSuccess(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onFailure();
            }
        });

    }

    public void setUser(DatabaseReference ref, String packageName) {
        readData(ref, new OnGetDataListener() {
            @Override
            public void onSuccess(DataSnapshot snapshot) {
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
                }

            }
            @Override
            public void onStart() {
                startActivity(new Intent(OtpVerification.this, MainActivity.class));
                finish();
            }

            @Override
            public void onFailure() {

            }
        });
    }*/


    public  boolean isReadStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
               // Log.v(TAG,"Permission is granted1");
                return true;
            } else {

              //  Log.v(TAG,"Permission is revoked1");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 3);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
          //  Log.v(TAG,"Permission is granted1");
            return true;
        }
    }

    public  boolean isWriteStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                //Log.v(TAG,"Permission is granted2");
                return true;
            } else {

               // Log.v(TAG,"Permission is revoked2");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
           // Log.v(TAG,"Permission is granted2");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 2:

                if(grantResults[0]== PackageManager.PERMISSION_GRANTED){

                    //resume tasks needing this permission

                }else{
                    //progress.dismiss();
                }
                break;

            case 3:

                if(grantResults[0]== PackageManager.PERMISSION_GRANTED){

                    //resume tasks needing this permission

                }else{
                   // progress.dismiss();
                }
                break;
        }
    }

}