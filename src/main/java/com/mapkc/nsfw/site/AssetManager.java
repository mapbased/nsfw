package com.mapkc.nsfw.site;

import com.mapkc.nsfw.component.Asset;
import com.mapkc.nsfw.model.Site;
import com.mapkc.nsfw.util.logging.ESLogger;
import com.mapkc.nsfw.util.logging.Loggers;

import java.util.concurrent.ConcurrentMap;

/**
 * Created by chy on 14/12/11.
 */
public class AssetManager {
    static ESLogger log = Loggers.getLogger(AssetManager.class);
    String prefix;
    private final Site site;
    private final SiteStore store;
    private final ConcurrentMap<String, Asset> assets = new java.util.concurrent.ConcurrentHashMap<>();

    public AssetManager(Site site) {
        this.site = site;
        this.prefix = site.getConfig("static");
        this.store = site.getSiteStore();
        // this.storeRoot=site.getSiteStore().e)
    }

    public Asset getAsset(String path) {
        Asset asset = assets.get(path);
        if (asset != null) {
            return asset;
        }
        if (!store.exists(path)) {
            log.warn("Cannot find res:{}", path);
            //throw  new RuntimeException("Cannot find static res:"+path);
        }
        // long lmd = store.getLasModified(path);
        String p = path;

        //todo 临时删除
//        if (!site.isDevelopeMode()) {
//            p = this.prefix + path;
//        }


        asset = new Asset(p + "?m=" + store.hashAsset(path));
        Asset back = this.assets.putIfAbsent(path, asset);
        if (back != null) {
            return back;
        }
        return asset;
    }


}
