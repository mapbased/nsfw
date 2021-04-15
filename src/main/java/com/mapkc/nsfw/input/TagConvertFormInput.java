package com.mapkc.nsfw.input;

import com.google.common.base.Splitter;
import com.mapkc.nsfw.model.AutoAssign;
import com.mapkc.nsfw.model.RenderContext;
import com.mapkc.nsfw.model.Schema;
import com.mapkc.nsfw.model.Site;
import com.mapkc.nsfw.util.EasyMap;

import java.util.List;
import java.util.function.Consumer;

/**
 * Created by chy on 14-8-1.
 */
public class TagConvertFormInput extends DefaultFormInput {


    @AutoAssign
    Site site;

    public static String tagsToName(String tags, final Schema schema) {
        final StringBuilder sb = new StringBuilder(tags.length());
        Splitter.on(' ').omitEmptyStrings().trimResults().split(tags).forEach(new Consumer<String>() {
            @Override
            public void accept(String s) {

                Object tagid = schema.getFieldBySql("name", "tagid=?", new Object[]{s});

                if (tagid != null) {
                    sb.append(tagid).append("|");
                }

            }
        });
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }

        return sb.toString();

    }

    public static String tagsToId(String tags, final Schema schema) {
        final StringBuilder sb = new StringBuilder(tags.length());
        Splitter.on('|').omitEmptyStrings().trimResults().split(tags).forEach(new Consumer<String>() {
            @Override
            public void accept(String s) {

                Object tagid = schema.getFieldBySql("tagid", "name=?", new Object[]{s});
                if (tagid == null) {
                    try {
                        tagid = schema.addUsingGeneratedId(EasyMap.make("name", s).toStrMap());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (tagid != null) {
                    sb.append(tagid).append(" ");
                }

            }
        });

        return sb.toString();

    }

    @Override
    public String fromParam(List<String> v, FormFieldModel ffm, RenderContext rc) {
        String tagschema = ffm.attr("tagschema", "/ds/hyq/tag");
        String s = super.fromParam(v, ffm, rc);
        Schema tag = site.getSchema(tagschema);
        return tagsToId(s, tag);

    }

    @Override
    public List<String> toParam(String v, FormFieldModel ffm, RenderContext rc) {
        List<String> ls = super.toParam(v, ffm, rc);
        if (ls.size() > 0) {
            String tagschema = ffm.attr("tagschema", "/ds/hyq/tag");
            Schema tag = site.getSchema(tagschema);
            ls.set(0, tagsToName(ls.get(0), tag));
        }
        return ls;
    }
}
