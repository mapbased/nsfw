/**
 *
 */
package com.mapkc.nsfw.query;

import com.mapkc.nsfw.model.RenderContext;
import com.mapkc.nsfw.model.Schema;
import com.mapkc.nsfw.util.TagSplitter;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author chy
 */
public class DetailResult extends QueryResult {

    DetailQueryConfig detailConfig;

    public DetailResult(DetailQueryConfig config, RenderContext rc) {
        this.rc = rc;
        this.detailConfig = config;
        Map<String, ResultRow> m = new TreeMap();
        String id = config.getId(rc);
        ResultMapSet rms = null;
        Schema sc = config.getSchema(rc);
        if (id == null) {
            if (config.fixedQuery != null) {
                String query = config.fixedQuery.getRenderValue(rc);

                rms = config.getSchema(rc).load(config.getFields(), m, sc.getKeyFieldName(),
                        query, new Object[]{});
            }

        } else {
            m.put(id, null);
            // config.getSchema(rc).load(config.getFields(), m, rc);
            //

            rms = config.getSchemaType(rc).getAccess().load(config.getFields(), m,
                    config.getSchema(rc));
        }
        if (rms != null && id != null) {


            this.currentRow = rms.getRowById(id);
        }
        if (this.currentRow == null) {
            return;
        }
        this.rowset = new SingleResultRowSet(this.currentRow);

        // autoWrap
        QueryLoader ql = config.queryloader;
        for (Map.Entry<String, List<String>> e : ql.schema2Fields.schemaFields
                .entrySet()) {

            List<String> lookfields = e.getValue();
            String schema = e.getKey();
            List<String> loadfields = ql.allSchemas.schemaFields.get(schema);
            Schema schemaobj = rc.getSite().getSchema(schema);
            if (schemaobj == null) {
                // TODO logerror
                continue;
            }
            Map<String, ResultRow> ids = new java.util.HashMap<String, ResultRow>();
            int cnt = lookfields.size();
            // for (ResultRow rr : this.rowset.getRows()) {
            for (int i = 0; i < cnt; i++) {
                String sid = String.valueOf(this.currentRow.getField(lookfields
                        .get(i)));


                if (sid.indexOf(' ') < 0) {
                    ids.put(sid, null);
                } else {
                    for (String tid : TagSplitter.split(sid)) {
                        ids.put(tid, null);
                    }
                }

            }

            ResultMapSet nrms = schemaobj.getType().getAccess().load(
                    loadfields.toArray(new String[loadfields.size()]), ids,
                    schemaobj);
            this.wrappedData.put(schema, nrms);

        }
        this.updateRowObj(config);


    }

    public boolean isEmpty() {
        return this.currentRow == null;
    }

    /* (non-Javadoc)
     * @see QueryResult#getTotal()
     */
    @Override
    public int getTotal() {
        if (this.currentRow == null) {
            return 0;
        }
        return 1;
    }

    /* (non-Javadoc)
     * @see QueryResult#getTime()
     */
    @Override
    public int getTime() {
        // TODO Auto-generated method stub
        return 0;
    }

    public QueryConfig getQueryConfig() {
        return this.detailConfig;
    }

}
