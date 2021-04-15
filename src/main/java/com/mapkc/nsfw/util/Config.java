package com.mapkc.nsfw.util;

import com.mapkc.nsfw.util.logging.ESLogger;
import com.mapkc.nsfw.util.logging.Loggers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * <p>
 * Title: MA LUCENE
 * </p>
 * <p/>
 * <p>
 * Description:
 * </p>
 * <p/>
 * <p>
 * Copyright: Copyright (c) 2010
 * </p>
 * <p/>
 * <p>
 * </p>
 *
 * @version 1.0
 */
public abstract class Config {
    static ESLogger log = Loggers.getLogger(Config.class);
    private static final Config CONFIG = new Config() {
        final java.util.Properties p = new Properties();
        boolean changed = false;
        // java.util.Properties ptoStore = null;
        long lmd;
        private File cf = new File("../conf/main.properties");

        {

            try {
                if (!cf.exists()) {

                    cf.getParentFile().mkdirs();

                    //  cf.createNewFile();

                    cf = new File("conf/main.properties");

                }
                log.info("using config file :{}", cf.getAbsolutePath());
                // ptoStore = new Properties();
                java.lang.Runtime.getRuntime().addShutdownHook(
                        new Thread("store-config") {
                            @Override
                            public void run() {
                                try {
                                    if (changed) {
                                        boolean autoUpdate = p
                                                .containsKey("autoUpdate");
                                        if (autoUpdate) {
                                            FileOutputStream fos = new java.io.FileOutputStream(
                                                    cf);
                                            p.store(fos,
                                                    "add an <autoUpdate> key to auto update config form default values");
                                            fos.close();
                                        }
                                    }
                                } catch (Exception ex) {
                                    log.warn("store config", ex);
                                }
                            }
                        }
                );

                p.load(new java.io.FileInputStream(cf));
                //  log.info("loading config from:" + cf.getAbsolutePath());

                lmd = cf.lastModified();

                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            Thread.sleep(60000);
                        } catch (InterruptedException e) {
                        }
                        long newlmd = cf.lastModified();
                        if (newlmd > lmd) {
                            lmd = newlmd;
                            log.info("Config file {} is changed,reloading ...",
                                    cf.getAbsolutePath());
                            java.io.FileInputStream fis = null;
                            try {
                                fis = new java.io.FileInputStream(cf);

                                p.load(fis);
                            } catch (IOException e) {
                                log.error("Error while loading config file:{}",
                                        cf.getAbsolutePath());
                            } finally {
                                if (fis != null)
                                    try {
                                        fis.close();
                                    } catch (IOException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }
                            }
                        }
                    }
                }, "Config file refresher");
                t.setDaemon(true);
                t.start();

            } catch (IOException ex) {
                log.warn("cannot create log file", ex);
            }

            // p.load(new);
        }

        @Override
        public String get(String k, String defaultValue) {
            String s = p.getProperty(k);
            if (s == null) {

                p.setProperty(k, defaultValue);
                changed = true;

                return defaultValue;
            }

            return s;
        }

        @Override
        public int getInt(String k, int defaultValue) {
            String s = this.get(k, defaultValue + "");

            try {
                return Integer.parseInt(s);
            } catch (Exception e) {
                return defaultValue;
            }

        }

        @Override
        public Long getLong(String k, long defaultValue) {
            String s = this.get(k, defaultValue + "");

            try {
                return Long.parseLong(s);
            } catch (Exception e) {
                return defaultValue;
            }

        }

        @Override
        public float getFloat(String k, float defaultValue) {
            String s = this.get(k, defaultValue + "");

            try {
                return Float.parseFloat(s);
            } catch (Exception e) {
                return defaultValue;
            }
        }

        @Override
        public boolean getBoolean(String k, boolean defaultValue) {
            String s = this.get(k, defaultValue + "");
            try {
                return Boolean.parseBoolean(s);
            } catch (Exception e) {
                return defaultValue;
            }

        }

        public boolean setProperty(String key, String value) {
            p.setProperty(key, value);
            try {
                FileOutputStream fos = new java.io.FileOutputStream(cf);
                p.store(fos, "");
                fos.close();
                return true;
            } catch (Exception ex) {
                log.warn("store config", ex);
                return false;
            }
        }

        @Override
        public String get(String key) {
            return p.getProperty(key);
        }
    };

    private Config() {

    }

    public static final Config get() {
        return CONFIG;

    }

    abstract public String get(String k, String defaultValue);

    abstract public int getInt(String k, int defaultValue);

    abstract public Long getLong(String k, long defaultValue);

    abstract public float getFloat(String k, float defaultValue);

    abstract public boolean getBoolean(String k, boolean defaultValue);

    // end added

    abstract public String get(String key);
}
