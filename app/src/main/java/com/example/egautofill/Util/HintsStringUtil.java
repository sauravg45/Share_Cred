package com.example.egautofill.Util;

import android.app.assist.AssistStructure;

import com.example.egautofill.Parser.HintStringParser;

import java.util.Arrays;
import java.util.List;

public class HintsStringUtil {

    public static final List<String> emailList= Arrays.asList("email","mail");

    public static final String email="email";

    public static final List<String> userList=Arrays.asList("users","users","username","name","login id","id","Email or phone number");

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
        String hint= HintStringParser.getHintManual(mHint);
        if(hint!=null){
            return hint;
        }

        //By resource Id
        String resourceId = node.getIdEntry();
        hint=HintStringParser.getHintManual(resourceId);
        if(hint!=null){
            return hint;
        }
        //
        CharSequence text = node.getText();
        CharSequence className = node.getClassName();
        if (text != null && className != null && className.toString().contains("EditText")){
            hint=HintStringParser.getHintManual(text.toString());
            if(hint!=null){}
            return hint;
        }
        return null;
    }


    
}
