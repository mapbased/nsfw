package com.mapkc.nsfw.model;

import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chy on 16/12/17.
 */
public class SchemaAccessPgCock extends SchemaAccessMysql {
    static Map<Integer, ParamConverter> converterMap = new HashMap<>();

    static {

        converterMap.put(Types.INTEGER, new ParamConverter() {
            @Override
            public Object convert(Object object) {
                if (object instanceof Number)
                    return ((Number) object).intValue();
                return Integer.parseInt(object.toString().trim());

            }
        });
        converterMap.put(Types.BIGINT, new ParamConverter() {
            @Override
            public Object convert(Object object) {
                if (object instanceof Number)
                    return ((Number) object).longValue();
                return Long.parseLong(object.toString().trim());

            }
        });

    }

    public static void setPreparedStatementConvereted(PreparedStatement preparedStatement, List<String> args) throws SQLException {

        if (args == null) {
            return;
        }
        setPreparedStatementConvereted(preparedStatement, args.toArray());
    }

    public static void setPreparedStatementConvereted(PreparedStatement preparedStatement, Object[] args) throws SQLException {

        ParameterMetaData metaData = preparedStatement.getParameterMetaData();

        for (int i = 0; i < args.length; i++) {
            Object inobj = args[i];
            int idx = i + 1;
            if (inobj instanceof String) {


                int paramType = metaData.getParameterType(idx);
                ParamConverter converter = converterMap.get(paramType);

                if (converter != null) {
                    inobj = converter.convert(inobj);
                }
            }
            preparedStatement.setObject(idx, inobj);
        }


    }

    protected char sqlNameChar() {
        return '"';
    }

    public void setStatementConvereted(PreparedStatement preparedStatement, Object[] args) throws SQLException {
        setPreparedStatementConvereted(preparedStatement, args);
    }

    public interface ParamConverter {
        Object convert(Object object);
    }


}
