package com.example.egautofill;

import android.app.assist.AssistStructure;
import android.content.Context;
import android.content.IntentSender;
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
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillValue;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.example.egautofill.Parser.AutoFillParser;
import com.example.egautofill.Parser.AutoFillSaveParser;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class AutofillEgService extends AutofillService {


    private static final String TAG = "DebugService";

    private boolean mAuthenticateResponses;
    private boolean mAuthenticateDatasets;
    private int mNumberDatasets;
    Context context;
    @Override
    public void onConnected() {
        super.onConnected();
        context=getApplicationContext();
        // TODO(b/114236837): use its own preferences?
       // MyPreferences pref = MyPreferences.getInstance(getApplicationContext());
        mAuthenticateResponses = false;
        mAuthenticateDatasets = false;
        mNumberDatasets = 2;

        Log.d(TAG, "onConnected(): numberDatasets=" + mNumberDatasets
                + ", authResponses=" + mAuthenticateResponses
                + ", authDatasets=" + mAuthenticateDatasets);
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
        FillResponse mresponse=parser.structureParser(structure,context,this);
        callback.onSuccess(mresponse);
    }

    @Override
    public void onSaveRequest(SaveRequest request, SaveCallback callback) {
        List<FillContext> fillContexts = request.getFillContexts();
        List<AssistStructure> structures =
                fillContexts.stream().map(FillContext::getStructure).collect(toList());
        AssistStructure structure = fillContexts.get(fillContexts.size() - 1).getStructure();
        //ArrayMap<String, AutofillId> fields = getAutofillableFields(structure);
       // ClientParser parser = new ClientParser(structures);
        AutoFillSaveParser parser=new AutoFillSaveParser();
        parser.structureParser(structure,context);
        Log.d(TAG, "onSaveRequest()");
        callback.onSuccess();
    }


    /**
     * Displays a toast with the given message.
     */
    private void toast(@NonNull CharSequence message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
}
