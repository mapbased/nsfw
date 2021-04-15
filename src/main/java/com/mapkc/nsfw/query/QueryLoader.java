package com.mapkc.nsfw.query;

import com.mapkc.nsfw.model.LoadContext;

import java.util.HashMap;
import java.util.Map;

/**
 * 存储级联关系
 *
 * @author chy
 */

public class QueryLoader {
    // 这个字段对应了哪些schema，貌似没啥用
    Map<String, QueryFields> fields = new java.util.HashMap<String, QueryFields>();
    // 存储了每个schema需要加载哪些字段
    QueryFields allSchemas = new QueryFields();
    // 存储每个schema对应哪些root字段
    QueryFields schema2Fields = new QueryFields();

    Map<String, Object> highlights = new java.util.HashMap<String, Object>(5);

    Map<String, QueryFields> secondLevelSchemas;
    Map<String, VQConfig> fieldDefinedQueries;

    public Map<String, VQConfig> getFieldDefinedQueries() {
        return fieldDefinedQueries;
    }

    public void addFieldDefineQuery(String fieldName, String sql) {
        if (this.fieldDefinedQueries == null) {
            this.fieldDefinedQueries = new HashMap<>();
        }
        VQConfig config = new VQConfig();
        config.groupSql = sql == null ? null : LoadContext.getRenderable(sql);

        this.fieldDefinedQueries.put(fieldName, config);
    }

    public void add(String field) {

        if (!this.fields.containsKey(field)) {
            this.fields.put(field, new QueryFields());
        }
    }

    public void addHighlight(String field) {
        this.highlights.put(field, field);
    }

    public String[] getFields() {
        // TODO need cache?
        return fields.keySet().toArray(new String[fields.size()]);
    }

    public void add(String field, String schema, String schemafield) {
        QueryFields qf = this.fields.get(field);
        if (qf == null) {
            qf = new QueryFields();
            this.fields.put(field, qf);
        }
        // String []ss=schemaAndfield.split("\\.");
        qf.addSchemaField(schema, schemafield);
        allSchemas.addSchemaField(schema, schemafield);
        schema2Fields.addSchemaField(schema, field);

    }

    /**
     * 二级级联加载
     *
     * @param field
     * @param schema
     * @param schemafield
     * @param schema2
     * @param schemafield2
     */
    public void add(String field, String schema, String schemafield, String schema2, String schemafield2) {
        this.add(field, schema, schemafield);
        allSchemas.addSchemaField(schema2, schemafield2);
        if (this.secondLevelSchemas == null) {
            this.secondLevelSchemas = new java.util.HashMap<String, QueryFields>();
        }
        QueryFields qf = this.secondLevelSchemas.get(schema2);
        if (qf == null) {
            qf = new QueryFields();
            this.secondLevelSchemas.put(schema2, qf);
        }
        qf.addSchemaField(schema, schemafield);
    }

//	public void add(String field, String schema, String[] schemafields) {
//		QueryFields qf = this.fields.getTarget(field);
//		if (qf == null) {
//			qf = new QueryFields();
//			this.fields.put(field, qf);
//		}
//		// String []ss=schemaAndfield.split("\\.");
//
//		for(String s:schemafields){
//			allSchemas.addSchemaField(schema, s);
//
//		}
//
//		schema2Fields.addSchemaField(schema, field);
//
//	}


}
