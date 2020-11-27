package com.example.credpass;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import de.hdodenhof.circleimageview.CircleImageView;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import android.view.autofill.AutofillManager;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.credpass.DTO.UserPassViewModel;
import com.example.credpass.Entity.UserPassDataBase;
import com.example.credpass.Util.IImagePickerLister;
import com.example.credpass.Util.ImagePickerEnum;
import com.example.credpass.database.AppDatabase;
import com.example.credpass.DTO.UIDataDTO;
import com.example.credpass.database.AppExecutors;
import com.example.credpass.firebase.FireBaseAndLocalQuery;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity  {
    private static int RESULT_LOAD_IMAGE = 1;
    private static final int CAMERA_ACTION_PICK_REQUEST_CODE = 610;
    private static final int PICK_IMAGE_GALLERY_REQUEST_CODE = 609;
    private AutofillManager mAutofillManager;
    public static final int CAMERA_STORAGE_REQUEST_CODE = 611;
    public static final int ONLY_CAMERA_REQUEST_CODE = 612;
    public static final int ONLY_STORAGE_REQUEST_CODE = 613;
    private TextView userName;
    private AppDatabase mDb;
    private UserPassViewModel userDataViewModel;
    CircleImageView profilePic;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userName=(TextView) findViewById(R.id.mainName);
        Map<String,String> localDataMap= FireBaseAndLocalQuery.getProfileData(this);
        userName.setText(localDataMap.get(FireBaseAndLocalQuery.sUsers));
        mAutofillManager = getSystemService(AutofillManager.class);
        if(!mAutofillManager.hasEnabledAutofillServices()){
//            Toast.makeText(this, "AutoFill permission missing", Toast.LENGTH_SHORT).show();
            new MaterialDialog.Builder(this)
                    .content(R.string.autofill_permission)
                    .positiveText("Proceed")
                    .negativeText("Cancel")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            Intent intent = new Intent(Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE);
                            intent.setData(Uri.parse("package:com.example.credpass"));
                            startActivityForResult(intent, 1);
                        }
                    })
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            finishAndRemoveTask();
                        }
                    }).show();

        }

        //Setting up the profile section :: Adding a click listener to change profile pic button & setting other details
        profilePic=(CircleImageView) findViewById(R.id.profile_image);
        Bitmap profilePicture=FireBaseAndLocalQuery.getBitmap(getApplicationContext().getPackageName());
        if(profilePicture!=null){
            profilePic.setImageBitmap(profilePicture);
        }else{
            profilePic.setImageResource(R.drawable.profile_pic);
        }
        CircleImageView changeProfilePic = (CircleImageView) findViewById(R.id.edit_profiePic);
        changeProfilePic.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent inProfile = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(inProfile);
            }
        });
        //Setting behaviour for Bottom Sheet Behaviour
        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        getSupportActionBar().setElevation(0);
        LinearLayout contentLayout = coordinatorLayout.findViewById(R.id.contentLayout);
        BottomSheetBehavior<LinearLayout> sheetBehaviour = BottomSheetBehavior.from(contentLayout);
        sheetBehaviour.setFitToContents(false);
        sheetBehaviour.setHideable(false);
        sheetBehaviour.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
        sheetBehaviour.setHalfExpandedRatio((float) 0.85);


        mDb=AppDatabase.getAppDatabase(getApplicationContext());


        userDataViewModel= ViewModelProviders.of(this).get(UserPassViewModel.class);
       userDataViewModel.getAllUIdata().observe(this, new Observer<List<UIDataDTO>>() {
            @Override
            public void onChanged(@Nullable final List<UIDataDTO> uiDataDTOS) {
                Log.d("data from Liv",uiDataDTOS.toString());
                CustomListAdapter adapter = new CustomListAdapter(MainActivity.this, uiDataDTOS);
                ListView listView = (ListView) findViewById(R.id.cred_list);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        UIDataDTO selectedItem = (UIDataDTO) parent.getItemAtPosition(position);
                        Intent editIntent = new Intent(MainActivity.this, EditCredActivity.class);
                        editIntent.putExtra("user_data", selectedItem.getData());
                        editIntent.putExtra("user_pass", selectedItem.getPassword());
                        editIntent.putExtra("app_name", selectedItem.appName);
                        editIntent.putExtra("data_skey",selectedItem.skey);
                        startActivity(editIntent);
                    }
                });
                listView.setAdapter(adapter);
            }
        });




    }






    private void toast(String text){
        Toast.makeText(MainActivity.this,text, Toast.LENGTH_SHORT).show();
    }


    public void showImagePickerDialog(@NonNull Context callingClassContext, IImagePickerLister imagePickerLister) {
        new MaterialDialog.Builder(callingClassContext)
                .items(R.array.imagePicker)
                .canceledOnTouchOutside(true)
                .itemsCallback((dialog, itemView, position, text) -> {
                    if (position == 0)
                        imagePickerLister.onOptionSelected(ImagePickerEnum.FROM_GALLERY);
                    else if (position == 1)
                        imagePickerLister.onOptionSelected(ImagePickerEnum.FROM_CAMERA);
                    dialog.dismiss();
                }).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean checkSelfPermissions(@NonNull Activity activity) {
        if (activity.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_STORAGE_REQUEST_CODE);
            return false;
        } else if (activity.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, ONLY_CAMERA_REQUEST_CODE);
            return false;
        } else if (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, ONLY_STORAGE_REQUEST_CODE);
            return false;
        }
        return true;
    }




    @Override
    public void onResume() {
        super.onResume();
        if(FireBaseAndLocalQuery.isStateChanged(this)){
            Map<String,String> localDataMap= FireBaseAndLocalQuery.getProfileData(this);
            userName.setText(localDataMap.get(FireBaseAndLocalQuery.sUsers));
            FireBaseAndLocalQuery.setStateChanged(this,false);
            Bitmap profilePicture=FireBaseAndLocalQuery.getBitmap(getApplicationContext().getPackageName());
            if(profilePic!=null){
                profilePic.setImageBitmap(profilePicture);
            }else{
                profilePic.setImageResource(R.drawable.profile_pic);
            }
        }

    }


}