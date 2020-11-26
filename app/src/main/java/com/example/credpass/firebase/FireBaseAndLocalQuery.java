package com.example.credpass.firebase;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.ArrayMap;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Map;

public class FireBaseAndLocalQuery {
    static FirebaseDatabase database = FirebaseDatabase.getInstance();
    public static final String MyPREFERENCES = "MyPrefs" ;
    public  static String sPhone="sPhone";
    public  static String sUsers="User";
    public static String intialName="Hi there";
    public static String picUrl="PIC_URL";
    public static String TAG="FIREBASEANDLOCALQUERY";
    static private StorageReference mStorageRef;


   static SharedPreferences sharedpreferences;

    static DatabaseReference fDatabase=database.getReference();
    static FirebaseAuth auth = FirebaseAuth.getInstance();
    static String userId = auth.getCurrentUser().getUid();

    static public void setProfile(String name,String phone,Context context){
        Map dataMap=new ArrayMap();
        dataMap.put(sUsers,name);
        dataMap.put(sPhone,phone);
        fDatabase.child(sUsers).child(userId).setValue(dataMap);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(sUsers,name);
        editor.putString(sPhone,phone);
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
        dataMap.put(sUsers,sharedpreferences.getString(sUsers,intialName));
        dataMap.put(sPhone,sharedpreferences.getString(sPhone,""));
        return dataMap;
    }

    static public void savePhoneNo(Context context,String phoneNumber){
        fDatabase.child(sUsers).child(userId).child(sPhone).setValue(phoneNumber);
        sharedpreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(sPhone,phoneNumber);
        editor.commit();
    }


    static public void saveUserByKeyValue(String key,String Value){

    }

    static public void saveToFirebaseStorage(Uri file){
        mStorageRef = FirebaseStorage.getInstance().getReference();
        //Uri file = Uri.fromFile(new File("path/to/images/rivers.jpg"));
        StorageReference ref = mStorageRef.child("users_images/"+userId);

        ref.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        getDownloadUrl(ref.getDownloadUrl());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...
                    }
                });
    }

    private static void getDownloadUrl(Task<Uri> downloadUrl) {
       String url=downloadUrl.getResult().toString();
       saveUserByKeyValue(picUrl,url);
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            java.net.URL url = new java.net.URL(src);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static void storeImage(Bitmap image,String packageName) {
        File pictureFile = getOutputMediaFile(packageName);
        if (pictureFile == null) {
            Log.d(TAG,
                    "Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
    }

    private static   File getOutputMediaFile(String packageName){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + packageName
                + "/Files");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }
        // Create a media file name

        File mediaFile;
        String mImageName=userId+".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }

    public Bitmap getBitmap(String packageName) {
        String path=Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + packageName
                + "/Files/"
                +userId+".jpg";
        Bitmap bitmap=null;
        try {
            File f= new File(path);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            bitmap = BitmapFactory.decodeStream(new FileInputStream(f), null, options);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap ;
    }

}
