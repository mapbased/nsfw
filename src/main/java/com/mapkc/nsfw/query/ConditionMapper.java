/**
 *
 */
package com.mapkc.nsfw.query;

/**
 * @author chy
 */
public interface ConditionMapper {
    SchemaField map(Object value);

    SchemaField[] allConditions();

    class SchemaField {
        public String schema;
        public String fieldName;
    }

    class StrConditionMapper implements ConditionMapper {
        String[] values;

        /*
         * (non-Javadoc)
         *
         * @see ConditionMapper#map(java.lang.Object)
         */
        @Override
        public SchemaField map(Object value) {
            // TODO Auto-generated method stub
            return null;
        }

        /*
         * (non-Javadoc)
         *
         * @see ConditionMapper#allConditions()
         */
        @Override
        public SchemaField[] allConditions() {
            // TODO Auto-generated method stub
            return null;
        }

    }

}
