package com.example.credpass;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.room.Room;
import de.hdodenhof.circleimageview.CircleImageView;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.InputType;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import android.view.autofill.AutofillManager;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.credpass.Util.IImagePickerLister;
import com.example.credpass.Util.ImagePickerEnum;
import com.google.android.material.appbar.AppBarLayout;
import com.example.credpass.DTO.UIDataDTO;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.yalantis.ucrop.UCrop;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Toast;
import android.widget.Toolbar;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CustomListAdapter.customButtonListener, IImagePickerLister {
    private static int RESULT_LOAD_IMAGE = 1;
    private static final int CAMERA_ACTION_PICK_REQUEST_CODE = 610;
    private static final int PICK_IMAGE_GALLERY_REQUEST_CODE = 609;
    private AutofillManager mAutofillManager;
    public static final int CAMERA_STORAGE_REQUEST_CODE = 611;
    public static final int ONLY_CAMERA_REQUEST_CODE = 612;
    public static final int ONLY_STORAGE_REQUEST_CODE = 613;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        hasEnabledAutofillServices()
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
        CircleImageView changeProfilePic = (CircleImageView) findViewById(R.id.edit_profiePic);
        changeProfilePic.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if (checkSelfPermissions(MainActivity.this))
                        showImagePickerDialog(MainActivity.this, MainActivity.this);
                }
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

        //Create the an adapter & populate the list view
        AppDatabase appDatabase= Room.databaseBuilder(getApplicationContext(),AppDatabase.class,"user-db").allowMainThreadQueries().build();
        List<UIDataDTO> databases= appDatabase.userPassDataDao().getAll();
//        ArrayList<String> dataArray = prepareDataForListView(databases);
        CustomListAdapter adapter = new CustomListAdapter(MainActivity.this, databases);
        ListView listView = (ListView) findViewById(R.id.cred_list);
        adapter.setCustomButtonListener(MainActivity.this);
        listView.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CAMERA_ACTION_PICK_REQUEST_CODE && resultCode == RESULT_OK){
            Uri uri = Uri.parse(currentPhotoPath);
            openCropActivity(uri, uri);
        } else if(requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK){
                Uri uri = UCrop.getOutput(data);
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ImageView profileImageView = (ImageView) findViewById(R.id.profile_image);
                profileImageView.setImageBitmap(bitmap);
        } else if(requestCode == PICK_IMAGE_GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null){

//            File file = null;
            try {
                Uri sourceUri = data.getData();
                File file = getImageFile();
                Uri destinationUri = Uri.fromFile(file);
                openCropActivity(sourceUri, destinationUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    //    @Override
    @Override
    public void onButtonClickListener(int position, UIDataDTO data, CustomListAdapter.ViewHolder viewHolder){
        Object buttonTag = viewHolder.button.getTag();
        if(buttonTag == "hidden"){
            viewHolder.button.setImageResource(R.drawable.ic_visibility_off_black_24dp);
            viewHolder.pass.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            viewHolder.button.setTag("shown");
        } else if(buttonTag == "shown"){
            viewHolder.button.setImageResource(R.drawable.ic_remove_red_eye_24px);
            viewHolder.pass.setInputType( InputType.TYPE_CLASS_TEXT |
                    InputType.TYPE_TEXT_VARIATION_PASSWORD);
            viewHolder.button.setTag("hidden");
        }
//        Toast.makeText(MainActivity.this, "Value:" + value + "pos: " + position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onOptionSelected(ImagePickerEnum imagePickerEnum) {
        try {
            if (imagePickerEnum == ImagePickerEnum.FROM_CAMERA) {
                openCamera();
            }
            else if (imagePickerEnum == ImagePickerEnum.FROM_GALLERY){
                openImagesDocument();
            }
        } catch (IOException e){
            e.printStackTrace();
        }

    }

    private void openImagesDocument() {
        Intent pictureIntent = new Intent(Intent.ACTION_GET_CONTENT);
        pictureIntent.setType("image/*");
        pictureIntent.addCategory(Intent.CATEGORY_OPENABLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            String[] mimeTypes = new String[]{"image/jpeg", "image/png"};
            pictureIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        }
        startActivityForResult(Intent.createChooser(pictureIntent, "Select Picture"), PICK_IMAGE_GALLERY_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_STORAGE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                showImagePickerDialog(this, this);
            else if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_DENIED) {
                toast(this, "ImageCropper needs Storage access in order to store your profile picture.");
                finish();
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                toast(this, "ImageCropper needs Camera access in order to take profile picture.");
                finish();
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED && grantResults[1] == PackageManager.PERMISSION_DENIED) {
                toast(this, "ImageCropper needs Camera and Storage access in order to take profile picture.");
                finish();
            }
        } else if (requestCode == ONLY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                showImagePickerDialog(this, this);
            else {
                toast(this, "ImageCropper needs Camera access in order to take profile picture.");
                finish();
            }
        } else if (requestCode == ONLY_STORAGE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                showImagePickerDialog(this, this);
            else {
                toast(this, "ImageCropper needs Storage access in order to store your profile picture.");
                finish();
            }
        }
    }

    public void toast(Context context, String content) {
        Toast.makeText(context, content, Toast.LENGTH_LONG).show();
    }

    private void openCamera() throws IOException {
        Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = getImageFile(); // 1
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) // 2
            uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID.concat(".provider"), file);
        else
            uri = Uri.fromFile(file); // 3
        pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri); // 4
        startActivityForResult(pictureIntent, CAMERA_ACTION_PICK_REQUEST_CODE);
    }

    String currentPhotoPath = "";
    private File getImageFile() throws IOException {
        String imageFileName = "JPEG_" + System.currentTimeMillis() + "_";
        File storageDir = new File(
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DCIM
                ), "Camera"
        );
        System.out.println(storageDir.getAbsolutePath());
        if (storageDir.exists())
            System.out.println("File exists");
        else
            System.out.println("File not exists");
        File file = File.createTempFile(
                imageFileName, ".jpg", storageDir
        );
        currentPhotoPath = "file:" + file.getAbsolutePath();
        return file;
    }

    private void openCropActivity(Uri sourceUri, Uri destinationUri) {
        UCrop.Options options = new UCrop.Options();
        options.setCircleDimmedLayer(true);
        options.setCropFrameColor(ContextCompat.getColor(this, R.color.colorAccent));
        UCrop.of(sourceUri, destinationUri)
                .withMaxResultSize(100, 100)
                .withAspectRatio(5f, 5f)
                .start(this);
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


    static Bitmap stringToBitMap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.URL_SAFE);
            return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }
}