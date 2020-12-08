package com.example.credpass;

import android.app.assist.AssistStructure;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.CancellationSignal;
import android.service.autofill.AutofillService;
import android.service.autofill.Dataset;
import android.service.autofill.FillCallback;
import android.service.autofill.FillContext;
import android.service.autofill.FillRequest;
import android.service.autofill.FillResponse;
import android.service.autofill.SaveCallback;
import android.service.autofill.SaveInfo;
import android.service.autofill.SaveRequest;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillManager;
import android.view.autofill.AutofillValue;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.NonNull;


import com.example.credpass.DTO.AutofillParserDTO;
import com.example.credpass.DTO.UIDataDTO;
import com.example.credpass.Parser.AutoFillParser;
import com.example.credpass.Parser.AutoFillSaveParser;
import com.example.credpass.screen.SimpleAuthActivity;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.example.credpass.CustomListAdapter.stringToBitMap;
import static java.util.stream.Collectors.toList;

public class AutofillEgService extends AutofillService {


    private static final String TAG = "AutofillService";

    Context context;
    @Override
    public void onConnected() {
        super.onConnected();
        context=getApplicationContext();


    }

    /**
     * Helper method to get the {@link AssistStructure} associated with the latest request
     * in an autofill context.
     */
    @NonNull
    static AssistStructure getLatestAssistStructure(@NonNull FillRequest request) {
        List<FillContext> fillContexts = request.getFillContexts();
        return fillContexts.get(fillContexts.size() - 1).getStructure();
    }

    @Override
    public void onFillRequest(FillRequest request, CancellationSignal cancellationSignal,
                              FillCallback callback) {
        Log.d(TAG, "onFillRequest()");

        // Find autofillable fields
        AssistStructure structure = getLatestAssistStructure(request);
        AutoFillParser parser=new AutoFillParser();
        AutofillParserDTO autoFillData=parser.structureParser(structure,context,this);
            if(autoFillData!=null) {
                FillResponse mresponse = genarateDataset(autoFillData);
                callback.onSuccess(mresponse);
            }
    }


    private FillResponse genarateDataset( AutofillParserDTO autoFillData) {


        FillResponse.Builder response = new FillResponse.Builder();
        String packageName = this.getPackageName();
        if(autoFillData.getDbData()!=null && autoFillData.getFields()!=null){
            ArrayMap<String, AutofillId> fields=autoFillData.getFields();
            List<UIDataDTO> dbData=autoFillData.getDbData();
            for(UIDataDTO userPass:dbData){
                String value="";
                Dataset.Builder lockedDataset = new Dataset.Builder();
                Dataset unlockedDataset=unlockedDataset(userPass,fields);
                for (Map.Entry<String, AutofillId> field : fields.entrySet()){

                    if(field.getKey()!= View.AUTOFILL_HINT_PASSWORD){
                        value=userPass.getData();
                    }else{
                        value=userPass.getPassword();
                    }


                    RemoteViews presentation = newDatasetPresentation(packageName,value,userPass.getIcon());
                    IntentSender authentication =
                            SimpleAuthActivity.newIntentSenderForDataset(this, unlockedDataset);
                    lockedDataset.setValue(field.getValue(), null, presentation).setAuthentication(authentication);
                }

                response.addDataset(lockedDataset.build());
            }
        }

        if(autoFillData.getFields()!=null){
            ArrayMap<String, AutofillId> fields=autoFillData.getFields();
            Collection<AutofillId> ids = fields.values();
            AutofillId[] requiredIds = new AutofillId[ids.size()];
            ids.toArray(requiredIds);
            response.setSaveInfo(
                    // We're simple, so we're generic
                    new SaveInfo.Builder(SaveInfo.SAVE_DATA_TYPE_PASSWORD|SaveInfo.SAVE_DATA_TYPE_USERNAME, requiredIds).build());
        }

        //for notifying to save password

        return response.build();
    }


    private Dataset unlockedDataset(UIDataDTO userPass,ArrayMap<String, AutofillId> fields){
        String value="";
        String packageName = this.getPackageName();
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
        return dataset.build();
    }


    private RemoteViews newDatasetPresentation(@NonNull String packageName,
                                               @NonNull CharSequence text,String iconString) {

        Drawable icon=null;
        try
        {

            icon = context.getPackageManager().getApplicationIcon(packageName);

        }
        catch (PackageManager.NameNotFoundException e) {
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


    @Override
    public void onSaveRequest(SaveRequest request, SaveCallback callback) {
        List<FillContext> fillContexts = request.getFillContexts();
//       // List<AssistStructure> structures =
//                fillContexts.stream().map(FillContext::getStructure).collect(toList());
        AssistStructure structure = fillContexts.get(fillContexts.size() - 1).getStructure();
        AutoFillSaveParser parser=new AutoFillSaveParser();
        parser.structureParser(structure,context);
        Log.d(TAG, "onSaveRequest()");
        callback.onSuccess();
    }



}
