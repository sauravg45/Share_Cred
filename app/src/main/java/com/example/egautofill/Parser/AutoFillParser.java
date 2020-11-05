package com.example.egautofill.Parser;

import android.app.assist.AssistStructure;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.service.autofill.Dataset;
import android.service.autofill.FillResponse;
import android.service.autofill.SaveInfo;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillValue;
import android.widget.ImageView;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Room;

import com.example.egautofill.AppDatabase;
import com.example.egautofill.DTO.UIDataDTO;
import com.example.egautofill.R;
import com.example.egautofill.Util.HintsStringUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class AutoFillParser {

    String TAG="AutoFill Service";
    Context applicationContext;
    Context viewContext;
    public FillResponse structureParser(AssistStructure structure, Context applicationContext,Context viewContext){
        ArrayMap<String, AutofillId> fields=getAutoFillableFields(structure);
       this.viewContext=viewContext;
       this.applicationContext=applicationContext;
        if (fields.isEmpty()) {

            return null;
        }
        FillResponse response = createResponse(applicationContext, fields,structure);

        return response;
    }

    private FillResponse createResponse(Context context, ArrayMap<String, AutofillId> fields,AssistStructure structure) {
        String packageName = context.getPackageName();
        packageName=structure.getActivityComponent().getPackageName();
        FillResponse response=getDataFromDB(packageName,fields,context);
        return response;
    }

    private FillResponse getDataFromDB(String packageName, ArrayMap<String, AutofillId> fields,Context mcontext) {
        AppDatabase appDatabase= Room.databaseBuilder(mcontext,AppDatabase.class,"user-db").allowMainThreadQueries().build();
        String dataType=checkDataAsked(fields);
        List<UIDataDTO> dbData=new ArrayList<>();
        for(Map.Entry<String,AutofillId> field:fields.entrySet()){
            if(dataType==HintsStringUtil.passwordIdBoth){
                if(field.getKey()!=View.AUTOFILL_HINT_PASSWORD){
                    dbData.addAll(appDatabase.userPassDataDao().getByTagAndIsPassId(field.getKey(),dataType));

                }
            }else{
                dbData=appDatabase.userPassDataDao().getByTagAndIsPassId(field.getKey(),dataType);

            }
        }
        return genarateDataset(dbData,fields,dataType);

    }


    private FillResponse genarateDataset(List<UIDataDTO> dbData,ArrayMap<String, AutofillId> fields,String dataType) {
        FillResponse.Builder response = new FillResponse.Builder();
        String packageName = viewContext.getPackageName();
        for(UIDataDTO userPass:dbData){
            String value="";
            Dataset.Builder dataset = new Dataset.Builder();
           for (Map.Entry<String, AutofillId> field : fields.entrySet()){

               if(field.getKey()!=View.AUTOFILL_HINT_PASSWORD){
                  value=userPass.getData();
               }else{
                   value=userPass.getPassword();
               }
               RemoteViews presentation = newDatasetPresentation(packageName,value,userPass.getIcon());
               dataset.setValue(field.getValue(), AutofillValue.forText(value), presentation);
           }
            response.addDataset(dataset.build());


        }
        Collection<AutofillId> ids = fields.values();
        AutofillId[] requiredIds = new AutofillId[ids.size()];
        ids.toArray(requiredIds);
        response.setSaveInfo(
                // We're simple, so we're generic
                new SaveInfo.Builder(SaveInfo.SAVE_DATA_TYPE_PASSWORD|SaveInfo.SAVE_DATA_TYPE_USERNAME, requiredIds).build());
        return response.build();
    }

    private RemoteViews newDatasetPresentation(@NonNull String packageName,
                                              @NonNull CharSequence text,String iconString) {

        Drawable icon=null;
        try
        {

            icon = applicationContext.getPackageManager().getApplicationIcon(packageName);

        }
        catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }

        RemoteViews presentation =
                new RemoteViews(packageName, R.layout.multidataset_service_list_item);
        presentation.setTextViewText(R.id.text, text);
        Bitmap bitIcon=stringToBitMap(iconString);
        if(bitIcon!=null){
        presentation.setImageViewBitmap(R.id.icon,bitIcon);
        }else{
            presentation.setImageViewResource(R.id.icon, R.mipmap.ic_launcher);
        }
        return presentation;
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

    public static Bitmap drawableToBitmap (Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }


    private String checkDataAsked(ArrayMap<String, AutofillId> fields){
        if(fields.size()>1&&fields.containsKey(View.AUTOFILL_HINT_PASSWORD)){
            return HintsStringUtil.passwordIdBoth;
        }else if(fields.size()==1&&fields.containsKey(View.AUTOFILL_HINT_PASSWORD)){
            return HintsStringUtil.passwordOnly;
        }else if(fields.size()==1&&!fields.containsKey(View.AUTOFILL_HINT_PASSWORD)){
            return HintsStringUtil.IdOnly;
        }
        return null;
    }

    private ArrayMap<String, AutofillId> getAutoFillableFields(AssistStructure structure) {
        ArrayMap<String, AutofillId> fields = new ArrayMap<>();
        int nodes = structure.getWindowNodeCount();
        for (int i = 0; i < nodes; i++) {
            AssistStructure.ViewNode node = structure.getWindowNodeAt(i).getRootViewNode();
            addAutofillableFields(fields, node);
            Log.d("TAG",node.toString());
        }
        return fields;
    }

    private void addAutofillableFields(@NonNull Map<String, AutofillId> fields,
                                       @NonNull AssistStructure.ViewNode node) {
        String hint = getHint(node);
        if (hint != null) {
            AutofillId id = node.getAutofillId();
            if (!fields.containsKey(hint)) {
                Log.v(TAG, "Setting hint '" + hint + "' on " + id);
                fields.put(hint, id);
            } else {
                Log.v(TAG, "Ignoring hint '" + hint + "' on " + id
                        + " because it was already set");
            }
        }
        int childrenSize = node.getChildCount();
        for (int i = 0; i < childrenSize; i++) {
            addAutofillableFields(fields, node.getChildAt(i));
        }
    }

    @Nullable
    protected String getHint(@NonNull AssistStructure.ViewNode node) {

        // First try the explicit autofill hints...

        String[] hints = node.getAutofillHints();
        if (hints != null) {
            // We're simple, we only care abou t the first hint
            return hints[0].toLowerCase();
        }

        String hint= HintsStringUtil.getHintByHeuristic(node);
        return hint;
    }


}
