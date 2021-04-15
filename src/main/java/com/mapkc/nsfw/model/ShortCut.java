package com.mapkc.nsfw.model;

import com.mapkc.nsfw.input.FormField;

public class ShortCut extends XEnum {

    /**
     *
     */
    @FormField(caption = "path", input = "typeahead<path=/handler/rpcmisc>")
    String path;

    public ShortCut() {

    }

    @Override
    protected void init(Site site) {

        super.init(site);
        site.shortCut(path, this.getId());
        site.put(this.getId(), site.getXEnumBagCreateIfEmpty(path));
        // TODO :do this after load?


    }


}
