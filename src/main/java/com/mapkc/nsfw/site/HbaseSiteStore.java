package com.mapkc.nsfw.site;

import com.mapkc.nsfw.model.RenderContext;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class HbaseSiteStore extends SiteStore {

    public HbaseSiteStore(String siteId) {
        super(siteId);
        // TODO Auto-generated constructor stub
    }

    @Override
    public Map<String, String> getAttributes(String id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getChildren(String id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public byte[] getContent(String id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void serviceRes(String id, RenderContext rc) {
        // TODO Auto-generated method stub

    }

    @Override
    public long getLasModified(String id) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void saveAttributes(String id, Map<String, String> attributes)
            throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void create(String id) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean exists(String id) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void delete(String id) {
        // TODO Auto-generated method stub

    }

    @Override
    public Properties getSiteProperties() {
        // TODO Auto-generated method stub
        return null;
    }

    public void rename(String id, String newname) throws IOException {

    }

}
