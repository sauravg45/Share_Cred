package com.example.credpass.firebase;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.ArrayMap;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Map;

public class FireBaseAndLocalQuery {
    static FirebaseDatabase database = FirebaseDatabase.getInstance();
    public static final String MyPREFERENCES = "MyPrefs" ;
    private  static String sName="Name";
    private  static String sPhone="sPhone";
    private  static String sUsers="User";
    public static String userName="USER_NAME";
    public static String userPhone="USER_PHONE";
    public static String intialName="Hi there";
   static SharedPreferences sharedpreferences;

    static DatabaseReference fDatabase=database.getReference();
    static FirebaseAuth auth = FirebaseAuth.getInstance();
    static String userId = auth.getCurrentUser().getUid();

    static public void setProfile(String name,String phone,Context context){
        Map dataMap=new ArrayMap();
        dataMap.put(sName,name);
        dataMap.put(sPhone,phone);
        fDatabase.child(sUsers).child(userId).setValue(dataMap);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(userName,name);
        editor.putString(userPhone,phone);
        editor.commit();

    }

    static public void setFCMToken(String token,Context context){
        sharedpreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean("FCM_TOKEN",true);
        fDatabase.child(sUsers).child(userId).child("FCMToken").setValue(token);
        editor.commit();
    }

    static public Map<String,String> getProfileData(Context context){
        sharedpreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        Map<String,String> dataMap=new ArrayMap<>();
        dataMap.put(userName,sharedpreferences.getString(userName,intialName));
        return dataMap;
    }


}
