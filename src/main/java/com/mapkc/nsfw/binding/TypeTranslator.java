/**
 *
 */
package com.mapkc.nsfw.binding;

import com.mapkc.nsfw.component.XmlContext;
import com.mapkc.nsfw.input.FormHandler;
import com.mapkc.nsfw.model.*;
import com.mapkc.nsfw.util.DynamicBoolean;
import com.mapkc.nsfw.util.DynamicClassLoader;
import com.mapkc.nsfw.util.TagSplitter;
import com.mapkc.nsfw.util.VolatileBag;
import com.mapkc.nsfw.util.logging.ESLogger;
import com.mapkc.nsfw.util.logging.Loggers;
import com.mapkc.nsfw.vl.ValueList;
import com.mapkc.nsfw.vl.ValueListFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * @author chy
 */
public abstract class TypeTranslator {
    // protected Field field;
    final static ESLogger log = Loggers.getLogger(TypeTranslator.class);
    private static Map<Class, TypeTranslator> map = new ConcurrentHashMap<Class, TypeTranslator>();// HashMap<Class,

    static {

        map.put(String.class, new TypeTranslator() {

            @Override
            public Object translate(String value, Site rc) {
                // TODO Auto-generated method stub
                return value;
            }

            @Override
            public String translate(Object value, Site site) {
                // TODO Auto-generated method stub
                return (String) value;
            }

        });

        map.put(String[].class, new TypeTranslator() {

            @Override
            public Object translate(String value, Site rc) {

                return value.split(" ");
            }

            @Override
            public String translate(Object value, Site site) {
                String[] ss = (String[]) value;
                StringBuilder stringBuilder = new StringBuilder();
                for (String s : ss) {
                    stringBuilder.append(s).append(" ");
                }
                if (stringBuilder.length() > 0) {
                    stringBuilder.setLength(stringBuilder.length() - 1);
                }
                return stringBuilder.toString();
            }

        });


        map.put(int.class, new TypeTranslator() {

            @Override
            public Object translate(String value, Site rc) {

                if (value == null || "".equals(value)) {
                    return 0;
                }
                return Integer.parseInt(value);
            }


        });

        map.put(java.math.BigDecimal.class, new TypeTranslator() {
            @Override
            public Object translate(String value, Site rc) {
                if (value == null || value.length() == 0) {
                    return null;
                }
                return new java.math.BigDecimal(value);
                // return java.math.BigDecimal.;
            }

        });

        map.put(float.class, new TypeTranslator() {

            @Override
            public Object translate(String value, Site rc) {

                if (value == null || "".equals(value)) {
                    return 0;
                }
                return Float.parseFloat(value);
            }

        });
        map.put(long.class, new TypeTranslator() {

            @Override
            public Object translate(String value, Site rc) {
                if (value == null || "".equals(value)) {
                    return 0;
                }

                return Long.parseLong(value);
            }

        });
        map.put(double.class, new TypeTranslator() {

            @Override
            public Object translate(String value, Site rc) {
                if (value == null || "".equals(value)) {
                    return 0;
                }

                return Double.parseDouble(value);
            }

        });

        map.put(short.class, new TypeTranslator() {

            @Override
            public Object translate(String value, Site rc) {
                if (value == null || "".equals(value)) {
                    return 0;
                }
                // TODO Auto-generated method stub
                return Short.parseShort(value);
            }

        });
        map.put(char.class, new TypeTranslator() {

            @Override
            public Object translate(String value, Site rc) {
                // TODO Auto-generated method stub
                if (value == null) {
                    return null;
                }
                value = value.trim();
                if (value.length() != 1) {
                    throw new java.lang.RuntimeException(
                            "Cannot convert to char:" + value);

                }
                return value.charAt(0);
            }

        });

        map.put(boolean.class, new TypeTranslator() {

            @Override
            public Object translate(String value, Site rc) {
                // TODO Auto-generated method stub
                if (value == null) {
                    return false;
                }
                String s = value.toLowerCase();
                return s.startsWith("t") || s.startsWith("y");
                // return value;
            }

        });

        map.put(java.sql.Date.class, new TypeTranslator() {

            @Override
            public Object translate(String value, Site rc) {
                if (value == null || value.length() < 2) {
                    return null;
                }
                java.text.SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    return new java.sql.Date(simpleDateFormat.parse(value).getTime());
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }

            }

            @Override
            public String translate(Object value, Site site) {
                if (value == null) {
                    return null;
                }
                java.text.SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

                return simpleDateFormat.format(value);
            }
        });
        map.put(java.sql.Timestamp.class, new TypeTranslator() {

            @Override
            public Object translate(String value, Site rc) {
                if (value == null || value.length() < 2) {
                    return null;
                }
                java.text.SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    return new java.sql.Timestamp(simpleDateFormat.parse(value).getTime());
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }

            }

            @Override
            public String translate(Object value, Site site) {
                if (value == null) {
                    return null;
                }
                java.text.SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                return simpleDateFormat.format(value);
            }
        });
        // map.put(java.sql.datet)

        map.put(ActionHandler.class, new TypeTranslator() {

            @Override
            public Object translate(String value, Site rc) {
                if (value != null && value.length() > 0) {

                    try {
                        return DynamicClassLoader
                                .load(value, rc);
                    } catch (ClassNotFoundException e) {

                        log.error("Error while load class:{}", e, value);

                    }
                }

                return null;
            }

            @Override
            public String translate(Object value, Site site) {
                // TODO Auto-generated method stub
                return value == null ? null : value.getClass().getName();
            }

        });


        map.put(Binding.class, new TypeTranslator() {

            @Override
            public Object translate(String value, Site rc) {
                if (value != null && value.length() > 0) {


                    return LoadContext.getBinding(value);

                }

                return null;
            }

            @Override
            public String translate(Object value, Site site) {

                return value == null ? null : ((Binding) value).getSrc();
            }

        });

        map.put(DynamicBoolean.class, new TypeTranslator() {
            @Override
            public Object translate(String value, Site rc) {

                return new DynamicBoolean(value);
            }
        });
        map.put(Renderable.class, new TypeTranslator() {

            @Override
            public Object translate(String value, Site rc) {
                if (value != null) {

                    return LoadContext
                            .getRenderable(value);

                }
                return null;
            }

            @Override
            public String translate(Object value, Site site) {

                if (value instanceof Renderable) {
                    Renderable x = (Renderable) value;
                    XmlContext xc = new XmlContext();
                    x.toXml(xc);
                    return xc.toString();
                }
                throw new java.lang.RuntimeException(
                        "Cannot convert renderable to string:"
                                + value.toString()
                );

            }


        });

        map.put(java.util.regex.Pattern.class, new TypeTranslator() {

            @Override
            public Object translate(String value, Site rc) {
                if (value != null && !value.equals("")) {

                    return Pattern.compile(value);

                }
                return null;
            }

        });

        map.put(java.lang.Class.class, new TypeTranslator() {

            @Override
            public Object translate(String value, Site rc) {

                try {
                    return DynamicClassLoader
                            .loadClass(value, rc);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }

            }

            @Override
            public String translate(Object value, Site site) {
                // TODO Auto-generated method stub
                return value == null ? null : ((java.lang.Class) value)
                        .getName();
            }

        });

        map.put(ValueList.class, new TypeTranslator() {

            @Override
            public Object translate(String value, Site rc) {

                return ValueListFactory.from(value, rc);// util.DynamicClassLoader

            }


        });

        map.put(int[].class, new TypeTranslator() {

            @Override
            public Object translate(String value, Site rc) {

                if (value == null) {
                    return null;
                }

                List<String> ss = TagSplitter.split(value);
                int[] ints = new int[ss.size()];
                for (int i = 0; i < ints.length; i++) {
                    ints[i] = Integer.parseInt(ss.get(i));
                }
                return ints;
                // return ValueListFactory.from(value, rc);// util.DynamicClassLoader

            }

            @Override
            public String translate(Object value, Site site) {
                if (value == null) {
                    return null;
                }
                int[] ints = (int[]) value;
                StringBuilder stringBuilder = new StringBuilder();
                for (int i : ints) {
                    stringBuilder.append(i).append(" ");
                }
                if (stringBuilder.length() > 0) {
                    stringBuilder.setLength(stringBuilder.length() - 1);
                }
                return stringBuilder.toString();

            }
        });


        map.put(FormHandler.class, new TypeTranslator() {

            @Override
            public Object translate(String value, Site rc) {
                if (value != null && value.length() > 0) {

                    try {
                        return DynamicClassLoader.load(
                                value, rc);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }// load(value);
                }
                return null;
            }

            @Override
            public String translate(Object value, Site site) {
                // TODO Auto-generated method stub
                return value == null || (value instanceof FormModel) ? null
                        : value.getClass()
                        .getName();
            }


        });

        map.put(VolatileBag.class, new TypeTranslator() {

            @Override
            public Object translate(String value, Site rc) {
                if (value == null || value.length() == 0) {
                    return null;
                }
                return rc.getXEnumBag(value);// util.DynamicClassLoader
                // .load(value);


            }

            @Override
            public String translate(Object value, Site site) {
                VolatileBag<XEnum> vx = (VolatileBag<XEnum>) value;
                if (vx == null || vx.getValue() == null) {
                    return null;
                }
                return vx.getValue().getId();
            }


        });
        map.put(Map.class, new TypeTranslator() {
            @Override
            public Object translate(String value, Site rc) {
                if (value == null || value.length() == 0)
                    return null;
                if (value.startsWith("{")) {
                    Map map1 = new HashMap();
                    String vs = value.substring(1, value.length() - 1);
                    String[] ss = vs.split(",");
                    //  List<StringPair> sps = new ArrayList<StringPair>(ss.length);
                    for (String s : ss) {
                        String n = s;
                        String v = s;
                        if (s.indexOf(':') > 0) {
                            String[] nvs = s.split("\\:");
                            n = nvs[0];
                            v = nvs[1];
                        }
                        map1.put(n, v);

                    }
                    return map1;

                }
                throw new RuntimeException("Cannot convert to map:" + value);
            }
        });
        map.put(Integer.class, map.get(int.class));
        map.put(Long.class, map.get(long.class));
        map.put(Double.class, map.get(double.class));
        map.put(Float.class, map.get(float.class));
        map.put(Short.class, map.get(short.class));
        map.put(Character.class, map.get(char.class));
        //  map.put(Byte.class, map.getTarget(byte.class));
        map.put(Boolean.class, map.get(boolean.class));


    }

    public abstract Object translate(String value, Site rc);


    public String translate(Object value, Site site) {
        // TODO Auto-generated method stub
        return value == null ? null : value.toString();
    }

    // TypeTranslator>();

    public static TypeTranslator get(Class c) {
        TypeTranslator t = map.get(c);
        if (t != null) {
            return t;
        }

        if (c.isEnum()) {
            t = new EnumTranslator(c);
            map.put(c, t);
            return t;

        }
        return classTypeTranslator;

    }

    static TypeTranslator classTypeTranslator = new TypeTranslator() {
        @Override
        public Object translate(String value, Site rc) {
            if (value != null && value.length() > 0 && value.indexOf(".") > 0) {

                try {
                    return DynamicClassLoader
                            .load(value, rc);
                } catch (ClassNotFoundException e) {

                    log.error("Error while load class:{}", e, value);

                }
            }

            return null;
        }

        @Override
        public String translate(Object value, Site site) {

            return value == null ? null : value.getClass().getName();
        }
    };

    public static TypeTranslator from(Class c) {

        TypeTranslator t = get(c);
        if (t == null)
            throw new java.lang.RuntimeException("Cannot make translator for type:"
                    + c.getName());
        return t;

        // return null;

    }

    public static void add(Class c, TypeTranslator typeTranslator) {
        map.put(c, typeTranslator);
    }

    public static class EnumTranslator extends TypeTranslator {
        final Class c;

        public EnumTranslator(Class c) {
            this.c = c;
        }

        @Override
        public String translate(Object value, Site site) {
            // TODO Auto-generated method stub
            return value == null ? null : ((java.lang.Enum) value).name();
        }

        @Override
        public Object translate(String value, Site rc) {
            try {
                int i = Integer.parseInt(value);

                return this.c.getEnumConstants()[i];
            } catch (java.lang.NumberFormatException e) {

                return java.lang.Enum.valueOf(c, value);
                // for (Object o : c.getEnumConstants()) {
                //
                // if (((java.lang.Enum) o).name().equals(value)) {
                // return o;
                // }
                // }
            }
            // return null;

        }

    }

}
