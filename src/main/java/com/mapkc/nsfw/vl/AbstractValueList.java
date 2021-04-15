package com.mapkc.nsfw.vl;

import com.mapkc.nsfw.model.RenderContext;

import java.util.Iterator;

/**
 * @author chy
 */
public abstract class AbstractValueList implements ValueList {

    String src;

    @Override
    public String toString() {
        if (src != null) {
            return src;
        }

        return super.toString();
    }

    @Override
    public Object getSrcByValue(String value, RenderContext rc) {
        Iterator<Value> ii = this.iterator(rc);
        if (ii == null)
            return null;
        while (ii.hasNext()) {
            Value v = ii.next();
            if (v.getValue().equals(value)) {
                return v.getSrc();
            }
        }
        return null;
    }

    @Override
    public String getScreenNameByValue(String value, RenderContext rc) {

        Iterator<Value> ii = this.iterator(rc);
        if (ii == null)
            return null;
        while (ii.hasNext()) {
            Value v = ii.next();
            if (v.getValue().equals(value)) {
                return v.getScreenName();
            }
        }
        return null;
    }


    @Override
    public abstract Iterator<Value> iterator(RenderContext rc);


    abstract protected String getScreenName(Object o);

    // {
    // return String.valueOf(MVEL.executeExpression(this.nameExp, o));
    // }

    abstract protected String getValue(Object o);
    // {
    // return String.valueOf(MVEL.executeExpression(this.valueExp, o));
    // }


}
