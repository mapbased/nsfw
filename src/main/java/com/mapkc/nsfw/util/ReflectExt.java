package com.mapkc.nsfw.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by chy on 15/2/6.
 */
public class ReflectExt {


    /**
     * 返回字段的第一个范型类型
     *
     * @param field
     * @return
     */
    public static Class firstGenericClass(Field field) {

        Type gt = field.getGenericType();
        if (gt instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) gt;
            Type type = parameterizedType.getActualTypeArguments()[0];
            if (type instanceof ParameterizedType) {
                return (Class) ((ParameterizedType) type).getRawType();

            } else {
                return (Class) type;
            }
            // init(genc, qf.schema(), fieldHelperMap.containsKey(genc) ? null : genc);
        }
        return null;

    }

    public static Class firstGenericClass(Method method) {
        Type gt = method.getGenericReturnType();


        if (gt instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) gt;
            Type type = parameterizedType.getActualTypeArguments()[0];
            if (type instanceof ParameterizedType) {
                return (Class) ((ParameterizedType) type).getRawType();

            } else {
                if (type instanceof Class)
                    return (Class) type;
            }
            // init(genc, qf.schema(), fieldHelperMap.containsKey(genc) ? null : genc);
        }
        return null;

    }
}
