/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.credpass.screen;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Handler;
import android.service.autofill.Dataset;

import android.util.Log;
import android.view.autofill.AutofillId;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.credpass.MainActivity;
import com.example.credpass.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static android.view.autofill.AutofillManager.EXTRA_AUTHENTICATION_RESULT;

/**
 * Activity used for autofill authentication, it simply sets the dataste upon tapping OK.
 */
// TODO(b/114236837): should display a small dialog, not take the full screen
public class SimpleAuthActivity extends Activity {

    private static final String EXTRA_DATASET = "dataset";
    private static final String EXTRA_HINTS = "hints";
    private static final String EXTRA_IDS = "ids";
    private static final String EXTRA_AUTH_DATASETS = "auth_datasets";
    public static final int CODE_AUTHENTICATION_VERIFICATION = 31;
    private static int sPendingIntentId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setContentView(R.layout.simple_service_auth_activity);
        KeyguardManager km = (KeyguardManager)getSystemService(KEYGUARD_SERVICE);
        if(km.isKeyguardSecure()) {

            Intent i = km.createConfirmDeviceCredentialIntent("Authentication required", "password");
            startActivityForResult(i, CODE_AUTHENTICATION_VERIFICATION);
        }
        else{
            Toast.makeText(this, "No any security setup done by user(pattern or password or pin or fingerprint", Toast.LENGTH_SHORT).show();
        }

    }


    public static IntentSender newIntentSenderForDataset(@NonNull Context context,
                                                         @NonNull Dataset dataset) {
        return newIntentSender(context, dataset, null, null, false);
    }

    public static IntentSender newIntentSenderForResponse(@NonNull Context context,
                                                          @NonNull String[] hints, @NonNull AutofillId[] ids, boolean authenticateDatasets) {
        return newIntentSender(context, null, hints, ids, authenticateDatasets);
    }

    private static IntentSender newIntentSender(@NonNull Context context,
                                                @Nullable Dataset dataset, @Nullable String[] hints, @Nullable AutofillId[] ids,
                                                boolean authenticateDatasets) {
        Intent intent = new Intent(context, SimpleAuthActivity.class);
        if (dataset != null) {
            intent.putExtra(EXTRA_DATASET, dataset);
        } else {
            intent.putExtra(EXTRA_HINTS, hints);
            intent.putExtra(EXTRA_IDS, ids);
            intent.putExtra(EXTRA_AUTH_DATASETS, authenticateDatasets);
        }

        return PendingIntent.getActivity(context, ++sPendingIntentId, intent,
                PendingIntent.FLAG_CANCEL_CURRENT).getIntentSender();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK && requestCode==CODE_AUTHENTICATION_VERIFICATION)
        {

            Intent myIntent = getIntent();
            Intent replyIntent = new Intent();
            Dataset dataset = myIntent.getParcelableExtra(EXTRA_DATASET);
            if (dataset != null) {
                replyIntent.putExtra(EXTRA_AUTHENTICATION_RESULT, dataset);
            } else {

            }
            setResult(RESULT_OK, replyIntent);
            finish();
        }
        else
        {
            setResult(RESULT_CANCELED);
            finish();
        }
    }

}
