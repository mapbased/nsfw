package com.mapkc.nsfw.model;

import com.mapkc.nsfw.input.FormField;
import com.mapkc.nsfw.util.DynamicClassLoader;
import com.mapkc.nsfw.util.logging.ESLogger;
import com.mapkc.nsfw.util.logging.Loggers;
import groovy.lang.GroovyClassLoader;

/**
 * Created by chy on 15/11/21.
 */
public class GroovyCode extends XEnum {

    final static ESLogger log = Loggers.getLogger(GroovyCode.class);
    @FormField(caption = "Code", input = "code", sort = 10000)
    String content;
    Object groovyObj;

    public <T> T getGroovyObj() {

        return (T) groovyObj;
    }

    protected String defaultIcon() {
        return "fa   fa-file-code-o";
    }

    @Override
    protected void init(Site site) {
        super.init(site);
        if (content == null || content.length() <= 0) {
            return;
        }
        try {

            GroovyClassLoader classLoader = new GroovyClassLoader(this.getClass().getClassLoader());


            Class groovyClass = classLoader.parseClass(
                    this.content);
            groovyObj = groovyClass.newInstance();
            DynamicClassLoader.autoAssign(groovyObj, groovyClass, site);
            classLoader.close();
        } catch (Exception t) {
            log.error("Error parse Groovy:{}", t, this.getId());
        }

    }
}
