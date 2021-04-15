package com.mapkc.nsfw.vl;

import com.mapkc.nsfw.model.RenderContext;
import org.mvel2.MVEL;

import java.util.Iterator;

public class DynamicValueList extends AbstractValueList {

    static final Iterator<Value> EMPTY = new Iterator<Value>() {

        @Override
        public void remove() {
            // TODO Auto-generated method stub

        }

        @Override
        public Value next() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean hasNext() {
            // TODO Auto-generated method stub
            return false;
        }
    };
    Object nameExp;
    Object valueExp;

    // TODO :解析格式和转译
    DynamicValueList(String nameE, String valueE) {
        if (nameE != null && nameE.length() > 0) {
            this.nameExp = MVEL.compileExpression(nameE);
        }
        if (valueE != null && valueE.length() > 0) {
            this.valueExp = MVEL.compileExpression(valueE);
        }

    }

    @Override
    public Iterator<Value> iterator(RenderContext rc) {
        // TODO Auto-generated method stub
        return EMPTY;
    }


    @Override
    protected String getScreenName(Object o) {
        if (this.nameExp == null) {
            return String.valueOf(o);
        }
        return String.valueOf(MVEL.executeExpression(this.nameExp, o));
    }

    @Override
    protected String getValue(Object o) {
        if (this.valueExp == null) {
            return this.getScreenName(o);
        }
        return String.valueOf(MVEL.executeExpression(this.valueExp, o));
    }

    @Override
    public Object getSrcByValue(String value, RenderContext rc) {
        // TODO Auto-generated method stub
        return null;
    }
}
