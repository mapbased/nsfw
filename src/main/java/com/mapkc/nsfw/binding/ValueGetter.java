package com.mapkc.nsfw.binding;

import com.mapkc.nsfw.model.LoadContext;
import com.mapkc.nsfw.model.RenderContext;
import com.mapkc.nsfw.model.Schema;
import com.mapkc.nsfw.model.SchemaField;
import com.mapkc.nsfw.query.LinkItem;
import com.mapkc.nsfw.query.QueryLoader;
import com.mapkc.nsfw.query.QueryResult;
import com.mapkc.nsfw.query.ResultRow;
import com.mapkc.nsfw.ses.SessionValueCreator;
import com.mapkc.nsfw.vl.ValueList;
import com.mapkc.nsfw.vl.ValueListFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class ValueGetter {

    abstract Object get(RenderContext rc, String key);

    static final Map<String, ValueGetter> map = new HashMap<String, ValueGetter>();

    static {
        RC rc = new RC();
        map.put("rc", rc);

        Var v = new Var();
        map.put("_var", v);
        map.put("_v", v);


        VVV vvv = new VVV();

        map.put("_vvv", vvv);

        VVP vvp = new VVP();

        map.put("_vvp", vvp);

        Header h = new Header();
        map.put("_header", h);
        map.put("_h", h);
        Param p = new Param();
        map.put("_param", p);
        map.put("_p", p);
        map.put("_req", p);
        ParamInt pi = new ParamInt();
        map.put("_pi", pi);
        map.put("_paramint", pi);
        map.put("_paramInt", pi);

        VarORParam vop = new VarORParam();
        map.put("_vop", vop);

        Enum e = new Enum();
        map.put("_e", e);
        map.put("_enum", e);
        map.put("_evp", new EVP());


        // EnumObj eo = new EnumObj();
        // map.put("_eo", eo);
        // map.put("_enumobj", eo);

        map.put("_vl", new VL());

        QueryResultVG qr = new QueryResultVG();
        map.put("_qr", qr);
        map.put("_query", qr);

        Config cfg = new Config();
        map.put("_config", cfg);
        map.put("_conf", cfg);
        map.put("_cfg", cfg);

        Cookie c = new Cookie();

        map.put("_cookie", c);
        map.put("_cook", c);
        map.put("_c", c);

        Session ses = new Session();
        map.put("_ses", ses);
        map.put("_session", ses);


    }


    public static ValueGetter from(String name) {

        return map.get(name);
    }

    public Object mock(RenderContext rc, String key, LoadContext lc) {

        return null;
    }
}


class Var extends ValueGetter {

    @Override
    public Object get(RenderContext rc, String key) {

        Object o = rc.getVar(key);
        // done in rc.getVar
        // if (o instanceof ForBag) {
        // return ((ForBag) o).value;
        // }
        return o;
    }
}

class RC extends ValueGetter {

    @Override
    public Object get(RenderContext rc, String key) {

        return rc;
    }
}

/**
 * Var value Var
 *
 * @author Administrator
 */
class VVV extends ValueGetter {

    @Override
    public Object get(RenderContext rc, String key) {

        Object o = rc.getVar(key);
        if (o == null) {
            return null;
        }
        return rc.getVar(String.valueOf(o));
    }
}

class VVP extends ValueGetter {

    @Override
    public Object get(RenderContext rc, String key) {

        Object o = rc.getVar(key);
        if (o == null) {
            return null;
        }
        return rc.getParameter(String.valueOf(o));
    }
}

class Cookie extends ValueGetter {

    @Override
    public Object get(RenderContext rc, String key) {
       io.netty.handler.codec.http.cookie.Cookie o = rc.getCookie(key);
        if (o == null) {
            return null;
        }
        return o.value();
    }
}

class Header extends ValueGetter {

    @Override
    public Object get(RenderContext rc, String key) {

        return rc.getHeader(key);
    }
}

class VarORParam extends ValueGetter {

    @Override
    public Object get(RenderContext rc, String key) {

        return rc.getVarOrParam(key);
    }
}

class Param extends ValueGetter {

    @Override
    public Object get(RenderContext rc, String key) {

        return rc.getParameter(key);
    }
}

class ParamInt extends ValueGetter {

    @Override
    public Object get(RenderContext rc, String key) {

        return rc.paramInt(key);
    }
}

class Enum extends ValueGetter {

    @Override
    public Object get(RenderContext rc, String key) {

        return rc.getSite().getXEnum(key);
    }
}

class EVP extends ValueGetter {
    @Override
    public Object get(RenderContext rc, String key) {

        return rc.getSite().getXEnum(rc.p(key));
    }
}


// class EnumObj extends ValueGetter {
//
// @Override
// public Object getTarget(RenderContext rc, String key) {
//
// return rc.getSite().getXEnumObj(key);
// }
// }
//
class VL extends ValueGetter {

    ValueList vl;

    @Override
    public Object get(RenderContext rc, String key) {

        if (vl == null) {
            vl = ValueListFactory.from(key, rc.getSite());
        }
        return vl;
    }
}

class Session extends ValueGetter {
    final static Object EMPTYOBJ = new Object();

    @Override
    public Object get(RenderContext rc, String key) {
        com.mapkc.nsfw.ses.Session se = rc.getSessionAllowCreate();
        Object v = se.getValue(key);

        if (v == EMPTYOBJ) {
            return null;
        }
        if (v != null) {
            return v;
        }

        SessionValueCreator sc = rc.getSiteCustomize().getSessionValueCreator();
        if (sc != null) {
            v = sc.initSessionValue(key, rc);
        }

        if (v == null) {
            se.setValue(key, EMPTYOBJ);
        } else {
            se.setValue(key, v);
        }
        return v;

    }
}

class Config extends ValueGetter {

    @Override
    public Object get(RenderContext rc, String key) {

        return rc.getSite().getConfig(key);
    }
}

/**
 * Uncomplete
 *
 * @author chy
 */
class QueryResultVG extends ValueGetter {

    @Override
    public Object get(RenderContext rc, String key) {

        return rc.getVar(key);

    }

    @Override
    public Object mock(RenderContext rc, final String key, LoadContext lc) {
        final QueryLoader ql = lc.getQueryLoader(key);
        return new QueryResult() {

            @Override
            public Object getCollected(String fieldName, String schemaName,
                                       String schemaField) {
                return null;
            }

            @Override
            public Object get(String fieldName) {
                ql.add(fieldName);
                return null;
            }

            @Override
            public String getMapped(String fieldName) {
                ql.add(fieldName);
                return null;
            }


            @Override
            public String getAsStr(String fieldName) {
                get(fieldName);
                return null;
            }

            @Override
            public Object get(String fieldName, String schemaName,
                              String schemaField) {
                ql.add(fieldName, schemaName, schemaField);

                return null;
            }

            public LinkItem getFieldDefinedQuery(String fieldName, String groupsql) {
                ql.addFieldDefineQuery(fieldName, groupsql);
                return null;
            }
            public LinkItem getFieldDefinedQueryEx(String fieldName, String groupsql,int cnt,int order) {
                ql.addFieldDefineQuery(fieldName, groupsql);
                return null;
            }

            @Override
            public Object get(String fieldName, String schemaName,
                              String schemaField,
                              String schemaName2,
                              String schemaField2
            ) {
                ql.add(fieldName, schemaName, schemaField, schemaName2, schemaField2);

                return null;
            }


            @Override
            public List<ResultRow> getMultiTag(String fieldName, String schemaName, String schemaFields) {
                for (String s : schemaFields.split(",")) {
                    String ss = s.trim();
                    if (ss.length() > 0)
                        ql.add(fieldName, schemaName, ss);
                }

                return null;
            }


            @Override
            public int getTotal() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public int getTime() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public LinkItem getFacet(String fieldname) {
                Schema sc = this.getSchema();
                if (sc != null) {
                    SchemaField sf = sc.getField(fieldname);
                    if (sf != null && sf.hasJoinField()) {
                        ql.add(fieldname, sf.joinSchema.getValue().getId(),
                                sf.joinField);
                    }
                }

                return null;
            }

            @Override
            public LinkItem getFacet(String fieldName, String schemaName,
                                     final String schemaField) {
                ql.add(fieldName, schemaName, schemaField);
                return null;
            }

            @Override
            public String getHighlight(String fieldName) {
                ql.addHighlight(fieldName);
                ql.add(fieldName);
                return null;
            }

        };
    }
}
