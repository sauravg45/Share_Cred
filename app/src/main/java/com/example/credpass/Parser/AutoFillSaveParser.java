package com.example.credpass.Parser;

import android.app.Application;
import android.app.assist.AssistStructure;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.ArrayMap;
import android.util.Base64;
import android.view.View;

import androidx.room.Room;

import com.example.credpass.AppDatabase;
import com.example.credpass.Entity.UserPassDataBase;
import com.example.credpass.Util.HintsStringUtil;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

public class AutoFillSaveParser extends Application {

    public List<UserPassDataBase> listUserData;
    private String packageName;
    public static Context mcontext;
    public String mtimestsmp;

    public void structureParser(AssistStructure structure,Context context){
        mcontext=context;
        packageName=structure.getActivityComponent().getPackageName();
        Long tsLong = System.currentTimeMillis()/1000;
        mtimestsmp=tsLong.toString();
        ArrayMap<String,String> tagValueMap=new ArrayMap<>();
        traverseStructure(structure,tagValueMap);
        if (tagValueMap!=null&&tagValueMap.size()!=0){
            String password=tagValueMap.get(View.AUTOFILL_HINT_PASSWORD);
            saveToDataBase(tagValueMap,password,null);
        }

    }

    public void traverseStructure(AssistStructure structure,ArrayMap<String,String> tagValueMap) {
        int nodes = structure.getWindowNodeCount();

        for (int i = 0; i < nodes; i++) {
            AssistStructure.WindowNode windowNode = structure.getWindowNodeAt(i);
            AssistStructure.ViewNode viewNode = windowNode.getRootViewNode();
            traverseNode(viewNode,tagValueMap);
        }

    }

    public void traverseNode(AssistStructure.ViewNode viewNode,ArrayMap<String, String> tagValueMap) {
        if(viewNode.getAutofillHints() != null && viewNode.getAutofillHints().length > 0) {
            //Mai
            String data=viewNode.getText().toString();
            if(data==null){
                return;
            }
            if(!tagValueMap.containsKey(viewNode.getAutofillHints()[0])){
            tagValueMap.put(viewNode.getAutofillHints()[0],data);}
        } else {

            //Get Hint By heuristic
            String hint= HintsStringUtil.getHintByHeuristic(viewNode);

            if (hint!=null){
                String data=viewNode.getText().toString();
                if(data==null){
                    return;
                }
                if(!tagValueMap.containsKey(hint)){
                tagValueMap.put(hint,data);}
            }
        }

        for(int i = 0; i < viewNode.getChildCount(); i++) {
            AssistStructure.ViewNode childNode = viewNode.getChildAt(i);
            traverseNode(childNode,tagValueMap);
        }
    }

    public String getImageFromDrawable(){
        Drawable drawable=null;
        try
        {

            drawable = mcontext.getPackageManager().getApplicationIcon(packageName);

        }
        catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }

        String img = null;
        if(drawable instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] arr = stream.toByteArray();
            img = Base64.encodeToString(arr, Base64.URL_SAFE);
            return img;
        }
        return null;
    }

    public static String getAppNameFromPkgName(Context context, String Packagename) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo info = packageManager.getApplicationInfo(Packagename, PackageManager.GET_META_DATA);
            String appName = (String) packageManager.getApplicationLabel(info);
            return appName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

  private void saveToDataBase(ArrayMap<String,String> tagValueMap,String password,String identifier){
      AppDatabase appDatabase=Room.databaseBuilder(mcontext,AppDatabase.class,"user-db").allowMainThreadQueries().build();
   for(Map.Entry<String,String> tagValue:tagValueMap.entrySet()){
       if(!(tagValue.getKey().equalsIgnoreCase(View.AUTOFILL_HINT_PASSWORD)&&(tagValueMap.size()>1))){
           UserPassDataBase userPassDataBase=new UserPassDataBase();
           userPassDataBase.setPassword(password);
           userPassDataBase.setAppName(getAppNameFromPkgName(mcontext,packageName));
           userPassDataBase.setIcon(getImageFromDrawable());
           userPassDataBase.setdata(tagValue.getValue());
           userPassDataBase.setAppName(packageName);
           userPassDataBase.setTag(tagValue.getKey());
           userPassDataBase.setTimestamp(mtimestsmp);
           userPassDataBase.setIdentifier(identifier);
           if(tagValueMap.size()==1&&tagValueMap.containsKey(View.AUTOFILL_HINT_PASSWORD)){
               userPassDataBase.setIsIdPass(HintsStringUtil.passwordOnly);
           }else if(tagValueMap.size()==1&&!tagValueMap.containsKey(View.AUTOFILL_HINT_PASSWORD)){
               userPassDataBase.setIsIdPass(HintsStringUtil.IdOnly);
           }else if(tagValueMap.size()>1&&tagValueMap.containsKey(View.AUTOFILL_HINT_PASSWORD)){
               userPassDataBase.setIsIdPass(HintsStringUtil.passwordIdBoth);
           }
           appDatabase.userPassDataDao().saveUserPassData(userPassDataBase);
       }
   }

  }


  }


