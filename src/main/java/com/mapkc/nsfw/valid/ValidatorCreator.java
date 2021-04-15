package com.mapkc.nsfw.valid;

import com.mapkc.nsfw.model.Site;

import java.util.Map;

public interface ValidatorCreator {

    Validator create(Map<String, String> as, Site site);
}
