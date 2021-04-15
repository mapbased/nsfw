/**
 *
 */
package com.mapkc.nsfw.vl;

import com.mapkc.nsfw.model.Renderable;
import com.mapkc.nsfw.model.Schema;
import com.mapkc.nsfw.model.XEnum;
import com.mapkc.nsfw.util.VolatileBag;

import java.util.List;

/**
 * @author chy
 */
public class SqlValueList extends SchemaValueList {

    /**
     * @param nameEx
     * @param valueEx
     * @param schema
     * @param sql
     */
    public SqlValueList(String nameEx, String valueEx,
                        VolatileBag<XEnum> schema, Renderable sql) {
        super(nameEx, valueEx, schema, sql);

    }

    @Override
    protected List<Object[]> list(String ss) {
        return ((Schema) schema.getValue()).listObjectByFullSql(Object[].class,
                ss, null);
    }
}
