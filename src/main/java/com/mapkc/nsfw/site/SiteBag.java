package com.mapkc.nsfw.site;

import com.mapkc.nsfw.model.Site;

public class SiteBag {
    Site site;

    public SiteBag(Site site) {
        this.site = site;
    }

    public Site getSite() {
        return this.site;
    }

}
