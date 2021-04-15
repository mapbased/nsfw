package com.mapkc.nsfw.util;

import java.lang.reflect.Array;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

public class DynamicLoop {

    static public void loopIterator(Iterator vdata, DLVisitor v) {
        Iterator i = vdata;
        if (i == null) {
            return;
        }
        while (i.hasNext()) {
            v.visit(i.next(), null);

        }
    }

    static public Iterator getIterator(final Object vdata) {
        if (vdata instanceof Map) {
            return ((Map) vdata).entrySet().iterator();

        }
        if (vdata.getClass().isEnum()) {
            // Enum.valueOf(enumType, name) e=(Enum)vdata;
            return getIterator(vdata.getClass().getEnumConstants());
        }
        if (vdata instanceof Iterator) {
            return (Iterator) vdata;
        }
        if (vdata instanceof Iterable) {
            return ((Iterable<?>) vdata).iterator();
        }

        if (vdata instanceof Enumeration) {
            final Enumeration<?> i = (Enumeration<?>) vdata;

            return new Iterator() {

                @Override
                public boolean hasNext() {
                    // TODO Auto-generated method stub
                    return i.hasMoreElements();
                }

                @Override
                public Object next() {
                    // TODO Auto-generated method stub
                    return i.nextElement();
                }

                @Override
                public void remove() {
                    throw new java.lang.UnsupportedOperationException("remove");
                }

            };


        }


        if (vdata.getClass().isArray()) {


            return new Iterator() {
                final int len = Array.getLength(vdata);
                int i = 0;

                @Override
                public boolean hasNext() {
                    // TODO Auto-generated method stub
                    return i < len;
                }

                @Override
                public Object next() {
                    // TODO Auto-generated method stub

                    Object o = Array.get(vdata, i);
                    i++;
                    return o;
                }

                @Override
                public void remove() {
                    throw new java.lang.UnsupportedOperationException("remove");
                }

            };

        } else {
            return getIterator(new Object[]{vdata});

        }

    }

    static public void loop(Object vdata, DLVisitor v) {
        if (vdata == null) {
            return;
        }

        if (vdata instanceof Map) {
            Map m = (Map) vdata;

            Iterator<Map.Entry> i = m.entrySet().iterator();

            while (i.hasNext()) {
                Map.Entry e = i.next();
                v.visit(e, e.getKey());

            }

        } else if (vdata instanceof Iterable) {
            Iterator i = ((Iterable<?>) vdata).iterator();

            while (i.hasNext()) {
                v.visit(i.next(), null);

            }
        } else if (vdata instanceof Iterator) {
            Iterator i = (Iterator<?>) vdata;
            while (i.hasNext()) {
                v.visit(i.next(), null);

            }
        } else if (vdata instanceof Enumeration) {
            Enumeration<?> i = (Enumeration<?>) vdata;
            while (i.hasMoreElements()) {
                v.visit(i.nextElement(), null);

            }

        } else if (vdata.getClass().isArray()) {

            int len = Array.getLength(vdata);
            for (int i = 0; i < len; i++) {
                v.visit(Array.get(vdata, i), i);

            }

        } else if (vdata.getClass().isEnum()) {
            // Enum.valueOf(enumType, name) e=(Enum)vdata;
            loop(vdata.getClass().getEnumConstants(), v);
        } else {
            v.visit(vdata, null);

        }

    }

    public interface DLVisitor {
        void visit(Object value, Object key);
    }

}
