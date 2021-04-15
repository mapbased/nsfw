package com.mapkc.nsfw.util;

import com.google.common.io.Files;
import com.mapkc.nsfw.model.AutoAssign;
import com.mapkc.nsfw.model.Site;
import com.mapkc.nsfw.model.XEnum;
import com.mapkc.nsfw.site.SiteCustomize;
import com.mapkc.nsfw.util.logging.ESLogger;
import com.mapkc.nsfw.util.logging.Loggers;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.TreeMap;

public class DynamicClassLoader {
    final static ESLogger log = Loggers.getLogger(DynamicClassLoader.class);

    public static Object load(String className, Site site)
            throws ClassNotFoundException {


        @SuppressWarnings("rawtypes")
        Class c = loadClass(className, site);
        if (c == null) {
            return null;
        }


        Object o;
        try {
            o = c.newInstance();
        } catch (Exception e) {

            throw new java.lang.RuntimeException(e);
        }
        autoAssign(o, c, site);
        return o;

    }


    public static void autoAssign(Object o, Class c, Site site) {


        Field[] flz = c.getDeclaredFields();

        for (Field f : flz) {
            AutoAssign a = f.getAnnotation(AutoAssign.class);
            if (a != null) {
                if (!f.isAccessible()) {
                    f.setAccessible(true);
                }

                // check :report error right now
                if (a.path().equals("")) {
                    Object fv = null;
                    try {
                        fv = f.get(o);
                    } catch (Exception e) {
                        throw new java.lang.RuntimeException(e);
                    }
                    if (fv != null) {
                        autoAssign(fv, fv.getClass(), site);

                    } else if (f.getType().isInstance(site)) {
                        try {
                            f.set(o, site);
                        } catch (Exception e) {
                            throw new java.lang.RuntimeException(e);
                        }
                    } else if (f.getType().isInstance(site.getCustomize())) {
                        try {
                            f.set(o, site.getCustomize());
                        } catch (Exception e) {
                            throw new java.lang.RuntimeException(e);
                        }
                    } else {
                        try {
                            Object newv = site.autoAssignAndCreate(f);
                            f.set(o, newv);
                        } catch (Exception e) {
                            throw new java.lang.RuntimeException(e);
                        }
                    }
                } else {
                    VolatileBag<XEnum> vbx = site
                            .getXEnumBagCreateIfEmpty(a.path());
                    Object type = f.getGenericType();
                    if (type instanceof ParameterizedType && vbx.getValue() != null) {
                        ParameterizedType pt = (ParameterizedType) type;
                        Type[] ts = pt.getActualTypeArguments();
                        if (ts != null && ts.length > 0) {
                            Class decaltype = (Class) ts[0];
                            if (!decaltype.isInstance(vbx.getValue())) {
                                throw new java.lang.ClassCastException(
                                        "Cannot cast "
                                                + vbx.getValue().getClass()
                                                .getName()
                                                + " from [" + a.path() + "] to "
                                                + decaltype.getName()
                                );
                            }
                        }

                    }


                    try {
                        f.set(o, vbx);
                    } catch (Exception e) {
                        throw new java.lang.RuntimeException(e);
                    }
                }

            }
        }
        Class sc = c.getSuperclass();
        if (sc != null)
            autoAssign(o, sc, site);// c.getSuperclass()

    }

    public static Class loadClass(String className, Site site)
            throws ClassNotFoundException {
        if (className == null || className.equals("")) {
            return null;
        }
        if (site != null && site.isDevelopeMode()) {
            String pkg = site.getConfig("action-package-base") + ".";
            // String pkg
            // =site.getConfig("")site.getCustomize().getClass().getPackage().getName();

            if (className.startsWith(pkg)) {
                // log.debug("Load Class in dynamic loader:{}", className);
                return new PClassLoader(site, pkg).findClass(className);

            }
        }
        return DynamicClassLoader.class.getClassLoader().loadClass(
                className);


    }

    static class PClassLoader extends ClassLoader {
        Site site;
        String packageName;
        Map<String, Class> classMap = new TreeMap<>();
        private String loadingClass;

        PClassLoader(Site site, String packageName) {
            this.site = site;
            this.packageName = packageName;

        }

        /**
         * Finds the specified class.
         *
         * @param name The name of the class
         * @return The resulting <tt>Class</tt> object
         * @throws ClassNotFoundException If the class could not be found
         * @todo Implement this java.lang.ClassLoader method
         */
        @Override
        public Class findClass(String name) throws ClassNotFoundException {
            Class old = classMap.get(name);
            if (old != null) {
                return old;
            }

            /**
             * when first call,assign the class name,if reenter with the same
             * class,throws exception,because every classloader only use once,
             * didn't clear the lodingClass flag here
             */
            if (this.loadingClass == null) {
                this.loadingClass = name;
            } else if (this.loadingClass == (name)) {
                throw new ClassNotFoundException(name);

            }
            if (!name.startsWith(this.packageName) || name.endsWith(SiteCustomize.class.getSimpleName())) {
                return DynamicClassLoader.class.getClassLoader()
                        .loadClass(name);
            }
            String classpath = site.getConfig("class-path", "../build/classes/");
            if (!classpath.endsWith("/")) {
                classpath = classpath + "/";
            }
            String n = new StringBuffer(classpath)
                    .append(name.replace('.', '/')).append(".class").toString();
            try {
                File f = new File(n);
                if (!f.exists()) {
                    // log.debug("loading from parent:{}", f.getAbsolutePath());
                    return DynamicClassLoader.class.getClassLoader().loadClass(
                            name);
                } else {
                    log.trace("DynClassLoading:{}", f.getAbsolutePath());
                }

                byte[] bs = Files.toByteArray(f);

                Class c = this.defineClass(name, bs, 0, bs.length);
                classMap.put(name, c);
                return c;
                // return  ;
            } catch (Exception ex) {
                throw new ClassNotFoundException(name, ex);
            }

        }

        /**
         * Loads the class with the specified name.
         *
         * @param name The name of the class
         * @return The resulting <tt>Class</tt> object
         * @throws ClassNotFoundException If the class was not found
         * @todo Implement this java.lang.ClassLoader method
         */
        @Override
        public Class loadClass(String name) throws ClassNotFoundException {

//
//            if (name.indexOf(this.loadingClass) >= 0) // for inner calss
//            {
//                return this.findClass(name);
//            }
            if (name.startsWith(this.packageName)) // 对webroot 包里的对象自动重新加载
            {
                return this.findClass(name);
            }

            try {

                return DynamicClassLoader.class.getClassLoader()
                        .loadClass(name);

            } catch (ClassNotFoundException ex) {
                // if 2 classes in "/WEB-INF/classes/" one references another,
                // the classed be refreenced will be loaded from here
                return this.findClass(name);
            }

        }


    }

}
