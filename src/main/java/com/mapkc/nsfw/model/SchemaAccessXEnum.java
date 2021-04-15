package com.mapkc.nsfw.model;

import com.mapkc.nsfw.input.FormFieldModel;
import com.mapkc.nsfw.query.ArrayBasedResultMapSet;
import com.mapkc.nsfw.query.ResultMapSet;
import com.mapkc.nsfw.query.ResultRow;
import com.mapkc.nsfw.site.SiteStore;
import com.mapkc.nsfw.util.VolatileBag;

import java.io.IOException;
import java.util.Map;

/**
 * Created by chy on 16/12/17.
 */
public class SchemaAccessXEnum extends SchemaAccess {


    @Override
    protected void check(String id,
                         Map<String, String> values) {
        // XEnum x = new XEnum();
        // x.setAttributes(values);
        // x.id = id;
        // x.postInit(rc.site);
        // x.type.createObject().fromXEnum(x, rc.site);

    }

    @Override
    public int changeFieldValue(Schema schema,
                                final Map<String, ? extends Object> values,
                                final Map<String, ? extends Object> conditions) {
        return 0;

    }

    @Override
    public int changeFieldValue(String id, Schema schema,
                                final Map<String, ? extends Object> values) {

        Site site = schema.site;
        Map<String, String> map = schema.site.getXEnum(id).normalAttributes(schema.site);
        for (Map.Entry<String, ? extends Object> r : values.entrySet()) {
            map.put(r.getKey(), r.getValue() == null ? null : String.valueOf(r.getValue()));
        }
        try {
            site.getSiteStore().saveAttributes(id, map);
            site.reloadEnum(id);
            return 1;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    @Override
    public ResultMapSet load(String[] fields,
                             final Map<String, ResultRow> ids, Schema schema) {
        ArrayBasedResultMapSet abrs = new ArrayBasedResultMapSet(fields,
                ids);
        Site site = schema.site;
        for (String s : ids.keySet()) {

            XEnum x = site.getXEnum(s);
            if (s != null) {
                Object[] vs = new Object[fields.length];
                for (int i = 0; i < fields.length; i++) {
                    vs[i] = x.getAttribute(fields[i]);
                }
                abrs.addResult(s, vs);

            }

        }
        return abrs;
    }

    @Override
    public void addUsingGivenId(String id, Schema schema,
                                Map<String, String> values)
            throws IOException {

        Site site = schema.site;

        XEnum xe = com.mapkc.nsfw.model.XEnum.createObj(site, id, values);
        VolatileBag<XEnum> bag = site.getXEnumBagCreateIfEmpty(id);
        if (!(xe instanceof ShortCut)) {


            XEnum old = bag.getValue(); // may exist from base
            bag.setValue(xe);
            if (old != null) {
                xe.items = old.items;
            }

        }
        site.getXEnum(xe.getParentId()).addSingleChild(xe.getName(), bag);


        SiteStore ss = site.getSiteStore();
        ss.create(id);
        // TODO :ADD XEnum Type here?

        ss.saveAttributes(id, values);

        //
        // VolatileBag<XEnum> vb = site.reloadEnum(id);
        // String pid = vb.getValue().parentId;
        // site.getXEnum(pid).addChild(vb.getValue().name, vb);

    }

    @Override
    public void updateUsingGivenId(String id, Schema schema,
                                   Map<String, String> values)
            throws IOException {
        Site site = schema.site;
        site.getSiteStore().saveAttributes(id, values);

        site.reloadEnum(id);

    }

    @Override
    public void addOrUpdateUsingGivenId(String id, Schema schema,
                                        Map<String, String> values)
            throws IOException {
        SiteStore ss = schema.site.getSiteStore();
        if (ss.exists(id)) {
            this.updateUsingGivenId(id, schema, values);
        } else {
            this.addUsingGivenId(id, schema, values);
        }

    }

    public Map<String, Object> getFields(Schema schema,
                                         final Map<String, Object> fields, String idValue) {
        Map<String, String> m = schema.site.getXEnum(idValue).normalAttributes(schema.site);
        for (Map.Entry<String, Object> e : fields.entrySet()) {
            e.setValue(m.get(e.getKey()));
        }
        return fields;
    }

    @Override
    public void delete(String id, Schema schema) {
        SiteStore ss = schema.site.getSiteStore();
        try {
            ss.delete(id);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    // public Map<String, String> load(Map<String, String> fields,
    // Schema schema, String id, RenderContext rc) {
    // XEnum x = rc.site.getXEnum(id);
    // if (x == null) {
    // return null;
    // }
    //
    // for (String s : fields.keySet()) {
    // fields.put(s, x.getAttribute(s));
    // }
    // return fields;
    // }

    /**
     * 从底层数据源加载
     */
    @Override
    public Map<String, String> load(FormModel model, String id,
                                    RenderContext rc) {

        if (id == null) {
            return null;
        }
        final XEnum x = rc.site.getXEnum(id);
        if (x == null) {
            return null;
        }
        final Map<String, String> normalAttributes = x.normalAttributes(rc.site);
        final Map<String, String> ret = new java.util.HashMap<String, String>();

        model.travelFields(new FormModel.FormFieldVisitor() {

            @Override
            public void visit(FormFieldModel ffm) {
                String fn = ffm.name;
                String s = normalAttributes.get(fn);
                if (s != null) {

                    ret.put(fn, s);
                }

            }
        }, rc, true);

        return ret;

    }
}
