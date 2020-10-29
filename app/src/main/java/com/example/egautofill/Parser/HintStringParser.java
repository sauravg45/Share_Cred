package com.example.egautofill.Parser;

import android.view.View;

import com.example.egautofill.Util.HintsStringUtil;

public class HintStringParser {

    public static final String getHintManual(String keyWord){
        if(keyWord==null){
            return null;
        }
        if(HintsStringUtil.emailList.contains(keyWord.toLowerCase())){
            return View.AUTOFILL_HINT_EMAIL_ADDRESS;
        }else if (HintsStringUtil.passwordList.contains(keyWord.toLowerCase())){
            return View.AUTOFILL_HINT_PASSWORD;
        }else if (HintsStringUtil.phoneList.contains(keyWord.toLowerCase())){
            return View.AUTOFILL_HINT_PHONE;
        }else if (HintsStringUtil.userList.contains(keyWord.toLowerCase())){
            return View.AUTOFILL_HINT_USERNAME;
        }else{
            return null;
        }
    }
}
