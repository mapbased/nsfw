package com.mapkc.nsfw.valid;

import com.mapkc.nsfw.model.RenderContext;
import com.mapkc.nsfw.model.Schema;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chy on 15/1/25.
 */
public class EMail extends BaseValidator {
    @Override
    public void validate(RenderContext rc, Schema schema, Map<String, ? extends Object> values, String name) {
        Object emailObj = values.get(name);
        if (emailObj != null) {
            String email = String.valueOf(emailObj);
            if (!isEmail(email)) {
                this.reportError(rc, name);
            }


        }


    }

    public static void main(String[] ss) {
        System.out.println(isEmail("chy_hs@126.com"));
    }

    public static boolean isEmail(String email) {
        String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
        Pattern regex = Pattern.compile(check);
        Matcher matcher = regex.matcher(email);
        boolean isMatched = matcher.matches();
        return isMatched;
    }

    @Override
    public void renderJS(RenderContext rc) {

    }
}
