package com.mapkc.nsfw.site;

import com.mapkc.nsfw.model.RenderContext;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public abstract class SiteStore {

    final protected String siteId;

    public SiteStore(String siteId) {
        this.siteId = siteId;
    }

    public abstract java.util.Properties getSiteProperties();

    public abstract boolean exists(String id);

    public abstract void delete(String id) throws IOException;

    public abstract void rename(String id, String newname) throws IOException;


    public String hashAsset(String id) {
        return null;
    }

    public abstract void create(String id) throws IOException;

    public abstract void saveAttributes(String id,
                                        Map<String, String> attributes) throws IOException;

    /**
     * 不存在返回Collections.EMPTY_MAP <br/>
     * 否则，返回map
     *
     * @param id
     * @return
     * @throws IOException
     */
    public abstract Map<String, String> getAttributes(String id)
            throws IOException;

    public abstract List<String> getChildren(String id);

    public abstract long getLasModified(String id);

    protected abstract byte[] getContent(String id) throws IOException;

    public abstract void serviceRes(String id, RenderContext rc);


    /**
     * 不存在返回Collections.EMPTY_MAP <br/>
     * 如果修改过，返回map <br/>
     * 如果没修改过返回null
     *
     * @param id
     * @param lmd
     * @return
     * @throws IOException
     */
    public Map<String, String> getIfModified(String id, long lmd) throws IOException {
        if (this.getLasModified(id) > lmd) {
            return this.getAttributes(id);
        }
        return null;
    }

    // public XEnum loadXEnum(String id) throws IOException {
    // long lmd = this.getLasModified(id);
    // if (lmd <= 0) {
    // return null;
    // }
    // XEnum x = new XEnum();
    // x.setLastModified(lmd);
    // x.setId(id);
    // Map<String, String> map = this.getAttributes(id);
    // x.setAttributes(map);
    // List<String> ll = this.getChildren(id);
    // if (ll.size() > 0) {
    // Map<String, VolatileBag<XEnumObj>> m = new
    // java.util.concurrent.ConcurrentHashMap<String, VolatileBag<XEnumObj>>();
    // for (String s : ll) {
    // m.put(s, new VolatileBag<XEnumObj>());
    // }
    // x.setItems(m);
    //
    // }
    //
    // return x;
    // }

}
