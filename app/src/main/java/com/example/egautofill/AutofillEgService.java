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

        ArrayMap<String, AutofillId> fields = getAutofillableFields(structure);
        Log.d(TAG, "autofillable fields:" + fields);

//        if (fields.isEmpty()) {
//            toast("No autofill hints found");
//            callback.onSuccess(null);
//            return;
//        }
//
//        // Create response...
//        FillResponse response = null;
//        if (mAuthenticateResponses) {
////            int size = fields.size();
////            String[] hints = new String[size];
////            AutofillId[] ids = new AutofillId[size];
////            for (int i = 0; i < size; i++) {
////                hints[i] = fields.keyAt(i);
////                ids[i] = fields.valueAt(i);
////            }
////
////            IntentSender authentication = SimpleAuthActivity.newIntentSenderForResponse(this, hints,
////                    ids, mAuthenticateDatasets);
////            RemoteViews presentation = newDatasetPresentation(getPackageName(),
////                    "Tap to auth response");
////            Log.d(TAG+"from Package", getPackageName());
////            response = new FillResponse.Builder()
////                    .setAuthentication(ids, authentication, presentation).build();
//        } else {
//            response = createResponse(this, fields, mNumberDatasets,mAuthenticateDatasets);
//        }
//
//        // ... and return it
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
     * Parses the {@link AssistStructure} representing the activity being autofilled, and returns a
     * map of autofillable fields (represented by their autofill ids) mapped by the hint associate
     * with them.
     *
     * <p>An autofillable field is a {@link AssistStructure.ViewNode} whose {@link #getHint(AssistStructure.ViewNode)} metho
     */
    @NonNull
    private ArrayMap<String, AutofillId> getAutofillableFields(@NonNull AssistStructure structure) {
        ArrayMap<String, AutofillId> fields = new ArrayMap<>();
        int nodes = structure.getWindowNodeCount();
        for (int i = 0; i < nodes; i++) {
            AssistStructure.ViewNode node = structure.getWindowNodeAt(i).getRootViewNode();
            addAutofillableFields(fields, node);
            Log.d("TAG",node.toString());
        }
        return fields;
    }

    /**
     * Adds any autofillable view from the {@link AssistStructure.ViewNode} and its descendants to the map.
     */
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
            // We're simple, we only care about the first hint
            return hints[0].toLowerCase();
        }


        // Then try some rudimentary heuristics based on other node properties

        String viewHint = node.getHint();
        String hint = inferHint(node, viewHint);
        if (hint != null) {
            Log.d(TAG, "Found hint using view hint(" + viewHint + "): " + hint);
            return hint;
        } else if (!TextUtils.isEmpty(viewHint)) {
            Log.v(TAG, "No hint using view hint: " + viewHint);
        }

        String resourceId = node.getIdEntry();
        hint = inferHint(node, resourceId);
        if (hint != null) {
            Log.d(TAG, "Found hint using resourceId(" + resourceId + "): " + hint);
            return hint;
        } else if (!TextUtils.isEmpty(resourceId)) {
            Log.v(TAG, "No hint using resourceId: " + resourceId);
        }

        CharSequence text = node.getText();
        CharSequence className = node.getClassName();
        if (text != null && className != null && className.toString().contains("EditText")) {
            hint = inferHint(node, text.toString());
            if (hint != null) {
                // NODE: text should not be logged, as it could contain PII
                Log.d(TAG, "Found hint using text(" + text + "): " + hint);
                return hint;
            }
        } else if (!TextUtils.isEmpty(text)) {
            // NODE: text should not be logged, as it could contain PII
            Log.v(TAG, "No hint using text: " + text + " and class " + className);
        }
        return null;
    }

    /**
     * Uses heuristics to infer an autofill hint from a {@code string}.
     *
     * @return standard autofill hint, or {@code null} when it could not be inferred.
     */
    @Nullable
    protected String inferHint(AssistStructure.ViewNode node, @Nullable String actualHint) {
        if (actualHint == null) return null;

        String hint = actualHint.toLowerCase();
        if (hint.contains("label") || hint.contains("container")) {
            Log.v(TAG, "Ignoring 'label/container' hint: " + hint);
            return null;
        }

        if (hint.contains("password")) return View.AUTOFILL_HINT_PASSWORD;
        if (hint.contains("username")
                || (hint.contains("login") && hint.contains("id")))
            return View.AUTOFILL_HINT_USERNAME;
        if (hint.contains("email")) return View.AUTOFILL_HINT_EMAIL_ADDRESS;
        if (hint.contains("name")) return View.AUTOFILL_HINT_NAME;
        if (hint.contains("phone")) return View.AUTOFILL_HINT_PHONE;

        // When everything else fails, return the full string - this is helpful to help app
        // developers visualize when autofill is triggered when it shouldn't (for example, in a
        // chat conversation window), so they can mark the root view of such activities with
        // android:importantForAutofill=noExcludeDescendants
        if (node.isEnabled() && node.getAutofillType() != View.AUTOFILL_TYPE_NONE) {
            Log.v(TAG, "Falling back to " + actualHint);
            return actualHint;
        }
        return null;
    }

    static FillResponse createResponse(@NonNull Context context,
                                       @NonNull ArrayMap<String, AutofillId> fields, int numDatasets,
                                       boolean authenticateDatasets) {
        String packageName = context.getPackageName();
        FillResponse.Builder response = new FillResponse.Builder();
        // 1.Add the dynamic datasets
        for (int i = 1; i <= numDatasets; i++) {
            Dataset unlockedDataset = newUnlockedDataset(fields, packageName, i);
            if (authenticateDatasets) {
//                Dataset.Builder lockedDataset = new Dataset.Builder();
//                for (Map.Entry<String, AutofillId> field : fields.entrySet()) {
//                    String hint = field.getKey();
//                    AutofillId id = field.getValue();
//                    String value = i + "-" + hint;
//                    IntentSender authentication =
//                            SimpleAuthActivity.newIntentSenderForDataset(context, unlockedDataset);
//                    RemoteViews presentation = newDatasetPresentation(packageName,
//                            "Tap to auth " + value);
//                    lockedDataset.setValue(id, null, presentation)
//                            .setAuthentication(authentication);
//                }
//                response.addDataset(lockedDataset.build());
            } else {
                response.addDataset(unlockedDataset);
            }
        }

        // 2.Add save info
        Collection<AutofillId> ids = fields.values();
        AutofillId[] requiredIds = new AutofillId[ids.size()];
        ids.toArray(requiredIds);
        response.setSaveInfo(
                // We're simple, so we're generic
                new SaveInfo.Builder(SaveInfo.SAVE_DATA_TYPE_GENERIC, requiredIds).build());

        // 3.Profit!
        return response.build();
    }

    static Dataset  newUnlockedDataset(@NonNull Map<String, AutofillId> fields,
                                      @NonNull String packageName, int i) {
        Dataset.Builder dataset = new Dataset.Builder();
        for (Map.Entry<String, AutofillId> field : fields.entrySet()) {
            String hint = field.getKey();
            AutofillId id = field.getValue();
            String value = i + "-" + hint;

            // We're simple - our dataset values are hardcoded as "N-hint" (for example,
            // "1-username", "2-username") and they're displayed as such, except if they're a
            // password
            String displayValue = hint.contains("password") ? "password for #" + i : value;
            RemoteViews presentation = newDatasetPresentation(packageName, displayValue);
            dataset.setValue(id, AutofillValue.forText(value), presentation);
        }

        return dataset.build();
    }

    /**
     * Helper method to create a dataset presentation with the given text.
     */
    @NonNull
    static RemoteViews newDatasetPresentation(@NonNull String packageName,
                                              @NonNull CharSequence text) {
        RemoteViews presentation =
                new RemoteViews(packageName, R.layout.multidataset_service_list_item);
        presentation.setTextViewText(R.id.text, text);
        presentation.setImageViewResource(R.id.icon, R.mipmap.ic_launcher);
        return presentation;
    }

    /**
     * Displays a toast with the given message.
     */
    private void toast(@NonNull CharSequence message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
}
