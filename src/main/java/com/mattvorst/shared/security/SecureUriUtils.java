package com.mattvorst.shared.security;

import java.lang.reflect.Type;

public class SecureUriUtils {

    private static final String[] SECURE_TERM_LIST = new String[]{"invite", "login", "logout", "one-time-token", "password", "verify"};

    private static final Type[] SECURE_TYPE_LIST = new Type[]{
    };

    public static boolean isUsingSecureTerm(String uri){

        boolean secure = true;

        if(uri != null){
            secure = false;
            for(String term : SECURE_TERM_LIST) {
                if (uri.contains(term)){
                    secure = true;
                    break;
                }
            }
        }
        return secure;
    }

    public static boolean isSecureObject(Type targetType){
        boolean secure = true;

        if(targetType != null){
            secure = false;
            for(Type type : SECURE_TYPE_LIST) {
                if (targetType.equals(type)){
                    secure = true;
                    break;
                }
            }
        }
        return secure;
    }
}
