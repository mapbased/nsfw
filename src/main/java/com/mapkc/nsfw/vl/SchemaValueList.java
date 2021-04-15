package com.mapkc.nsfw.vl;

import com.google.common.cache.Cache;
import com.mapkc.nsfw.model.*;
import com.mapkc.nsfw.util.VolatileBag;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class SchemaValueList extends AbstractValueList implements Cleanable {

    VolatileBag<XEnum> schema;
    String[] fields = new String[2];
    Renderable cond;
    boolean nocache = false;

    private Cache<String, List<Object[]>> objCache = null;

    SchemaValueList(String nameEx, String valueEx, VolatileBag<XEnum> schema,
                    Renderable sql) {
        this.fields[0] = nameEx;
        this.fields[1] = valueEx;
        this.schema = schema;

        if (RenderGroup.startsWith(sql, "/*nocache*/")) {
            this.nocache = true;
        } else {
            this.objCache = com.google.common.cache.CacheBuilder.newBuilder()
                    .softValues().expireAfterAccess(5, TimeUnit.MINUTES).build();
        }
        this.cond = sql;

    }

    protected List<Object[]> list(String ss) {
        return ((Schema) schema.getValue()).listObjectBySql(fields, Object[].class,
                ss, null);
    }

    @Override
    public Iterator<Value> iterator(RenderContext rc) {
        // Map<>rc.getContextCache();
        List<Object[]> vs = (List<Object[]>) rc.getContextCache().get(this);
        if (vs == null) {

            final String ss = this.cond.getRenderValue(rc);

            if (nocache) {
                vs = this.list(ss);
            } else {

                try {
                    String cachekey = new StringBuilder(ss).append(this.fields[0]).append(this.fields[1]).toString();

                    vs = this.objCache.get(cachekey,
                            new Callable<List<Object[]>>() {

                                @Override
                                public List<Object[]> call() throws Exception {

                                    return list(ss);
                                }
                            });


                } catch (ExecutionException e) {
                    throw new java.lang.RuntimeException(e);
                }
            }
            rc.setContextCacheValue(this, vs);
        }
        return new ItIteratorValue(this, vs.iterator());


    }

    @Override
    protected String getScreenName(Object o) {

        return String.valueOf(((Object[]) o)[0]);
    }

    @Override
    protected String getValue(Object o) {

        return String.valueOf(((Object[]) o)[1]);
    }

    @Override
    public void clean() {
        if (this.objCache != null)
            this.objCache.invalidateAll();

    }

}
