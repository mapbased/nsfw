package com.mapkc.nsfw.handler;

import com.mapkc.nsfw.util.ReflectExt;
import com.mapkc.nsfw.util.Strings;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class MethodBag implements Comparable<MethodBag> {

    Method method;
    //  Class[] paraClasses;
    String[] paraNames;
    AccessMode acceseMode;



    String privilege = "";

    public MethodBag(Method method) {
        this.method = method;

        // this.paraClasses = method.getParameterTypes();
        Parameter[] ps = method.getParameters();
        this.paraNames = new String[ps.length];
        for (int i = 0; i < ps.length; i++) {

            paraNames[i] = ps[i].getName();

            Name name = ps[i].getAnnotation(Name.class);
            if (name != null) {
                paraNames[i] = name.value();
            }
        }

    }
    public String getPrivilege() {
        return privilege;
    }
    public String getComment() {
        Comment comment = this.method.getAnnotation(Comment.class);
        if (comment != null) {
            return comment.value();
        }
        return null;
    }

    public String leftComment() {
        return Strings.left(this.getComment(), new char[]{',', ' ', '，'});
    }

    public Class firstGenericClass() {
        return ReflectExt.firstGenericClass(this.method);
    }

    public Method getMethod() {
        return method;
    }

    public String[] getParaNames() {
        return paraNames;
    }


    public void getM() {
        Parameter ps[] = this.method.getParameters();


        for (Parameter parameter : ps) {
            //  parameter.getType().()
            Comment comment = parameter.getAnnotation(Comment.class);
            comment.value();
        }
        //  method.getAnnotation(Comment.class).value()
    }


    public AccessMode getAcceseMode() {
        return acceseMode;
    }


    public String getMethodName() {
        return this.method.getName();
    }

    @Override
    public int compareTo(MethodBag o) {
        return this.getMethodName().compareTo(o.getMethodName());
    }
}