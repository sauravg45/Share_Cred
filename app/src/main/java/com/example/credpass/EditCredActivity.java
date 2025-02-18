package com.example.credpass;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.credpass.database.AppDatabase;
import com.example.credpass.firebase.FireBaseAndLocalQuery;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class EditCredActivity extends AppCompatActivity {
    private String userData;
    private String userPassword;
    private String appName;
    Long skey;
    private boolean showPass = false;
    CheckBox showHidePassWord;
    TextInputEditText etPassword;
    MaterialButton btnEdit;
    MaterialButton btnCancel;
    TextInputEditText etUser;
    ImageView appIcon;
    MaterialButton btnSave;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_cred);

        //action bar back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Intent i = getIntent();
        userData = i.getExtras().getString("user_data");
        userPassword = i.getExtras().getString("user_pass");
        appName = i.getExtras().getString("app_name");
        skey=i.getExtras().getLong("data_skey");
        String icon=i.getExtras().getString("icon");
        appIcon=(ImageView) findViewById(R.id.appIconImsgeView);
        appIcon.setImageBitmap(FireBaseAndLocalQuery.stringToBitMap(icon));
        TextView etApp = (TextView) findViewById(R.id.ec_appName);
        etApp.setText(appName);
        etUser = (TextInputEditText) findViewById(R.id.ec_cred_data);
        etUser.setText(userData);
        etPassword = (TextInputEditText) findViewById(R.id.ec_cred_pass);
        etPassword.setText(userPassword);
        showHidePassWord = (CheckBox) findViewById(R.id.ec_showHidePswd);
        showHidePassWord.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    showPass = true;
                    etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else {
                    showPass = false;
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT |
                            InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
            }
        });
        btnEdit = (MaterialButton) findViewById(R.id.ec_edit);
        btnEdit.setTag("editMode");
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btnEdit.getTag() == "editMode"){
                    etApp.setEnabled(true);
                    etUser.setEnabled(true);
                    etPassword.setEnabled(true);
                    btnEdit.setText("Save");
                    btnEdit.setTag("saveMode");

                    //
                } else if(btnEdit.getTag() == "saveMode"){
                    etApp.setEnabled(false);
                    etUser.setEnabled(false);
                    etPassword.setEnabled(false);
                    btnEdit.setText("Edit");
                    btnEdit.setTag("editMode");
                    editUserPass();
                }
            }
        });
        btnCancel = (MaterialButton) findViewById(R.id.ec_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etApp.setEnabled(false);
                etUser.setEnabled(false);
                etPassword.setEnabled(false);
                Intent in = new Intent(EditCredActivity.this, MainActivity.class);
                startActivity(in);
                finish();
            }
        });
    }
    @Override
    public boolean onSupportNavigateUp() {

        finish();
        return true;
    }

    public void Check(View v){
        if(showHidePassWord.isChecked()){
            showPass = true;
            etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            showPass = false;
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT |
                    InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }

    }

    public void delete(Long skey){
        AppDatabase appDatabase= Room.databaseBuilder(getApplicationContext(), AppDatabase.class,"user-db").allowMainThreadQueries().build();
        appDatabase.userPassDataDao().deleteBySkey(skey);
    }

    public void editUserPass(){
        AppDatabase appDatabase= Room.databaseBuilder(getApplicationContext(),AppDatabase.class,"user-db").allowMainThreadQueries().build();
       // set data here meena for etUser and etPassword
        appDatabase.userPassDataDao().updateBySkey(skey,etUser.getText().toString(),etPassword.getText().toString());
        Intent in = new Intent(EditCredActivity.this, MainActivity.class);
        startActivity(in);
        finish();
    }
    
}