package com.example.credpass;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class EditCredActivity extends AppCompatActivity {
    private String userData;
    private String userPassword;
    private String appName;
    private boolean showPass = false;
    CheckBox showHidePassWord;
    TextInputEditText etPassword;
    MaterialButton btnEdit;
    MaterialButton btnCancel;
    MaterialButton btnSave;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_cred);
        Intent i = getIntent();
        userData = i.getExtras().getString("user_data");
        userPassword = i.getExtras().getString("user_pass");
        appName = i.getExtras().getString("app_name");
        TextInputEditText etApp = (TextInputEditText) findViewById(R.id.ec_appName);
        etApp.setText(appName);
        TextInputEditText etUser = (TextInputEditText) findViewById(R.id.ec_cred_data);
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
                } else if(btnEdit.getTag() == "saveMode"){
                    etApp.setEnabled(false);
                    etUser.setEnabled(false);
                    etPassword.setEnabled(false);
                    btnEdit.setText("Edit");
                    btnEdit.setTag("editMode");
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

    
}