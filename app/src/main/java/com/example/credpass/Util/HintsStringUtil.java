package com.example.credpass.Util;

import android.app.assist.AssistStructure;
import android.view.View;

import java.util.Arrays;
import java.util.List;

public class HintsStringUtil {

    public static final List<String> emailList= Arrays.asList("email","mail");

    public static final String email="email";

    public static final List<String> userList=Arrays.asList("users","users","username","name","login id","id","Email or phone number","Phone number or email address");

    public static final String user ="user";

    public static final List<String> passwordList=Arrays.asList("password","pass");

    public static final String password="password";

    public static final List<String> phoneList=Arrays.asList("mobile no.", "mobile","phone no.","number");

    public static final String phone="phone";

    public static final String passwordOnly="P";

    public static final String passwordIdBoth="B";

    public static final String IdOnly="I";




    public static String getHintByHeuristic(AssistStructure.ViewNode node){
        //By resource Id
        String mHint = node.getHint();
        String hint=(mHint!=null)?getHintManual(mHint):null;
        if(hint!=null){
            return hint;
        }

        //By resource Id


        String resourceId = node.getIdEntry();
        hint=(resourceId!=null)?getHintManual(resourceId):null;
        if(hint!=null){
            return hint;
        }
        //
        CharSequence text = node.getText();
        CharSequence className = node.getClassName();
        if (text != null && className != null && className.toString().contains("EditText")){
            hint=getHintManual(text.toString());
            if(hint!=null){}
            return hint;
        }
        return null;
    }


    public static final String getHintManual(String word){
        //breaking string to get actual hint like (email or phone no ) to extract email and phone no separat


        String[] keywords=word.split(" |\\(|\\)");
        for(String keyWord:keywords){
            if(keyWord==null){
                return null;
            }
            if(emailList.contains(keyWord.toLowerCase())){
                return View.AUTOFILL_HINT_EMAIL_ADDRESS;
            }else if (passwordList.contains(keyWord.toLowerCase())){
                return View.AUTOFILL_HINT_PASSWORD;
            }else if (phoneList.contains(keyWord.toLowerCase())){
                return View.AUTOFILL_HINT_PHONE;
            }else if (userList.contains(keyWord.toLowerCase())){
                return View.AUTOFILL_HINT_USERNAME;
            }else{
                return null;
            }
        }
        return null;
    }

    
}
