package com.mapkc.nsfw.site;

import com.mapkc.nsfw.model.Site;
import com.mapkc.nsfw.util.Config;
import com.mapkc.nsfw.util.concurrent.JMXConfigurableThreadPoolExecutor;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadPoolExecutor;

import static java.nio.file.StandardWatchEventKinds.*;

public class SiteManager {
    ConcurrentMap<String, SiteBag> sites = new ConcurrentHashMap<String, SiteBag>();

    // /SiteBag global = new SiteBag(new Site("global", null));
    Site defaultSite = null;// new Site("default", global);
    //ThreadPoolExecutor executor = new JMXConfigurableThreadPoolExecutor(16, 50,
    //       1000, "Main");
    ThreadPoolExecutor executor = JMXConfigurableThreadPoolExecutor.newCachedThreadPool("Main-pool");
    Site base = new Site("base", null);
    File root;
    WatchService watcher;

    public SiteManager() {


        for (String s : this.getSiteList()) {

            // this.sites.put(s, );
            Site site = s.equals("base") ? base : new Site(s, base);
            SiteBag sb = this.getSiteBag(s);
            sb.site = site;

            java.lang.Iterable<String> ds = site.getDomains();
            if (ds != null) {
                for (String ss : ds) {
                    Object old = sites.put(ss, sb);
                    if (old != null) {
                        throw new RuntimeException("Host duplicated:" + ss);
                    }
                }
            }

        }
        SiteBag defaultsb = this.sites.get("default");
        if (defaultsb != null) {
            this.defaultSite = defaultsb.site;
        }

        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {

                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    refeshAll();
                }
            }
        }, "Reload daemon");
        t.setDaemon(true);
        t.start();
//		try {
//			this.watchFiles();
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		}
    }

    public void handleEvents() throws InterruptedException {
        while (true) {
            WatchKey key = watcher.take();
            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind kind = event.kind();

                if (kind == OVERFLOW) {//事件可能lost or discarded
                    continue;
                }

                WatchEvent<Path> e = (WatchEvent<Path>) event;
                Path fileName = e.context();


                System.out.printf("Event %s has happened,which fileName is %s%n"
                        , kind.name(), fileName);
            }
            if (!key.reset()) {
                break;
            }
        }
    }

    private void watchFiles() throws IOException {
        FileSystem fs = FileSystems.getDefault();
        watcher = fs.newWatchService();
        Path path = fs.getPath(root.getAbsolutePath());
        path.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    handleEvents();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }).start();


    }

    private void refeshAll() {
        for (SiteBag sb : this.sites.values()) {
            Site s = sb.getSite();
            if (s != null && s.isDevelopeMode()) {
                s.refeshAll();
            }
        }

    }

    private SiteBag getSiteBag(String key) {

        SiteBag sb = this.sites.get(key);
        if (sb == null) {
            sb = new SiteBag(null);
            SiteBag back = this.sites.putIfAbsent(key, sb);
            if (back != null) {
                sb = back;
            }
        }
        return sb;
    }

    private void put(Site s) {
        String key = s.getSiteStore().siteId;
        SiteBag sb = this.sites.get(key);
        if (sb == null) {
            sb = new SiteBag(s);
            SiteBag back = this.sites.putIfAbsent(key, sb);
            if (back != null) {
                sb = back;
            }
        }
        sb.site = s;
    }

    public List<String> getSiteList() {

        root = new File(Config.get().get("storeroot", "../storeroot/"));
        if (!root.exists()) {
            root = new File("storeroot");
            if (!root.exists()) {
                root = new File("../storeroot/");
            }
        }

        File[] ffs = root.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory()
                        && !new File(pathname, "skip.txt").exists();
            }
        });
        List<String> l = new java.util.ArrayList<String>(ffs.length);


        for (File f : ffs) {
            l.add(f.getName());
        }
        return l;
    }

    public Site getSite(String domain) {
        domain = domain.toLowerCase();
        SiteBag bag = sites.get(domain);
        if (bag == null) {
            return defaultSite;
        }

        return bag.site;
    }

    public Site getDefaultSite() {
        return this.defaultSite;
    }

}
