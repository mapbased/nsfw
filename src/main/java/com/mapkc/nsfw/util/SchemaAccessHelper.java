package com.mapkc.nsfw.util;

import com.mapkc.nsfw.binding.TypeTranslator;
import com.mapkc.nsfw.model.Column;
import com.mapkc.nsfw.model.ParamField;
import com.mapkc.nsfw.model.Schema;
import com.mapkc.nsfw.model.Site;
import com.mapkc.nsfw.query.ResultRow;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chy on 14-5-14.
 */
public class SchemaAccessHelper {


    /**
     * TODO :性能提高
     *
     * @param o
     * @param site
     * @param <T>
     */
    public static <T> void fill(Object o, Site site) {
        if (o == null) {
            return;
        }
        List ol = new ArrayList<>(1);
        ol.add(o);
        fill(ol, o.getClass(), site);

    }

    public static <T> void fill(List<T> list, Class<T> tClass, Site site) {
        if (list.size() == 0) {
            return;
        }
        ClassMeta cm = createClassMeta(tClass);
        Map<String, Map<String, ResultRow>> schemaIds = new HashMap<String, Map<String, ResultRow>>();
        for (T t : list) {
            for (IDAccess ida : cm.accessList) {
                String id = null;
                try {
                    id = ida.getId(t);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                if (id != null && id.length() > 0) {
                    Map<String, ResultRow> ll = schemaIds.get(ida.schemaName);
                    if (ll == null) {
                        ll = new HashMap<String, ResultRow>();

                        schemaIds.put(ida.schemaName, ll);
                    }
                    ll.put(id, null);

                }

            }
        }

        for (Map.Entry<String, Map<String, ResultRow>> e : schemaIds.entrySet()) {
            Schema sc = site.getSchema(e.getKey());
            List<String> fields = cm.schemaFields.get(e.getKey());
            if (fields == null || fields.size() == 0) {
                //TODO add log
                continue;

            }
            sc.load(fields.toArray(new String[fields.size()]), e.getValue());

        }

        for (T t : list) {
            for (IDAccess ida : cm.accessList) {
                String id = null;
                try {
                    id = ida.getId(t);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                if (id != null && id.length() > 0) {
                    Map<String, ResultRow> ll = schemaIds.get(ida.schemaName);
                    if (ll != null) {
                        ResultRow rr = ll.get(id);
                        if (rr != null) {
                            Object o = rr.getField(ida.fieldName);
                            try {
                                ida.field.set(t, o);
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }


                }

            }
        }

    }

    /**
     * 要求list中所有对象类型一致
     *
     * @param t
     * @param <T>
     */
    public static <T> ClassMeta createClassMeta(Class<T> t) {
        ClassMeta meta = new ClassMeta();

        Field[] flz = t.getDeclaredFields();
        for (Field f : flz) {
            Column c = f.getAnnotation(Column.class);

            if (c != null) {
                String schema = c.schema();
                if (schema.length() == 0) {
                    //需要到别的schema加载数据，直接越过
                    continue;
                }
                String n = f.getName();
                if (c.field().length() > 0) {
                    n = c.field();
                }
                meta.addField(schema, n);


                String idsrc = c.idValue();
                IDAccess ida = new IDAccess();
                ida.schemaName = schema;
                try {
                    Method m = t.getDeclaredMethod(idsrc);
                    ida.ao = m;
                } catch (NoSuchMethodException e) {
                    try {
                        ida.ao = t.getDeclaredField(idsrc);
                    } catch (NoSuchFieldException e1) {
                        throw new RuntimeException(e1);
                    }
                }
                ida.ao.setAccessible(true);
                meta.accessList.add(ida);
                ida.field = f;
                f.setAccessible(true);
                ida.fieldName = n;


            }
        }


        return meta;
    }

    static public Map<String, String> fromObj(Object t, Site site) {
        Map<String, String> ret = new HashMap<>();

        try {

            Class c = t.getClass();

            while (true) {
                if (c == null) {
                    break;
                }
                Field[] fields = c.getDeclaredFields();
                for (Field field : fields) {
                    ParamField paramField = field.getAnnotation(ParamField.class);
                    String fn = null;
                    if (paramField != null) {
                        fn = paramField.field();
                    } else {
                        Column column = field.getAnnotation(Column.class);
                        if (column != null) {
                            fn = column.field();
                        }
                    }
                    if (fn != null) {
                        if (fn.length() == 0) {
                            fn = field.getName();
                        }
                        if (!field.isAccessible())
                            field.setAccessible(true);
                        Object ov = field.get(t);
                        if (ov == null) {
                            continue;
                        }


                        TypeTranslator typeTranslator = TypeTranslator.from(field.getType());
                        ret.put(fn, typeTranslator.translate(ov, site));
                    }
                }
                c = c.getSuperclass();
            }


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return ret;
    }

    /**
     * 从Map 中获取标对象标注了ParamField、Column的字段
     *
     * @param tClass
     * @param values
     * @param <T>
     * @return
     */

    static public <T> T toObj(Class<T> tClass, Map<String, String> values, Site site) {
        T t = null;
        try {
            t = tClass.newInstance();
            Class c = tClass;

            while (true) {
                if (c == null) {
                    break;
                }
                Field[] fields = c.getDeclaredFields();
                for (Field field : fields) {
                    //  if(field.iss)
                    ParamField paramField = field.getAnnotation(ParamField.class);
                    String fn = null;
                    if (paramField != null) {
                        fn = paramField.field();
                    } else {
                        Column column = field.getAnnotation(Column.class);
                        if (column != null)
                            fn = column.field();
                    }
                    if (fn != null) {

                        //  String fn = paramField.field();
                        if (fn.length() == 0) {
                            fn = field.getName();
                        }
                        String s = values.get(fn);
                        if (s != null) {
                            field.setAccessible(true);
                            TypeTranslator typeTranslator = TypeTranslator.from(field.getType());
                            field.set(t, typeTranslator.translate(s, site));
                        }
                    }
                }
                c = c.getSuperclass();
            }


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return t;
    }

    static class IDAccess {
        String schemaName;
        AccessibleObject ao;
        Field field;
        String fieldName;

        public String getId(Object o) throws IllegalAccessException, InvocationTargetException {
            Object ret;
            if (ao instanceof Field) {
                Field f = (Field) ao;
                ret = f.get(o);
            } else {
                Method m = (Method) ao;
                ret = m.invoke(o);
            }
            if (ret == null) {
                return null;
            }
            return String.valueOf(ret);
        }
    }

    static class ClassMeta {
        Map<String, List<String>> schemaFields = new HashMap<String, List<String>>(5);
        List<IDAccess> accessList = new ArrayList<IDAccess>(8);

        public void addField(String schema, String field) {
            List<String> ll = this.schemaFields.get(schema);
            if (ll == null) {
                ll = new ArrayList<String>(5);
                ll.add(field);
                schemaFields.put(schema, ll);
            } else {
                if (!ll.contains(field)) {
                    ll.add(field);
                }
            }
        }

    }


}
