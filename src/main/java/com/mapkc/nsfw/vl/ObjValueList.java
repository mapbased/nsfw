package com.mapkc.nsfw.vl;

import com.mapkc.nsfw.model.RenderContext;
import com.mapkc.nsfw.util.DynamicLoop;

import java.util.Iterator;

/**
 * Created by chy on 15/9/16.
 */
public class ObjValueList extends DynamicValueList {


    Object bind;

    public ObjValueList(String nameE, String valueE, Object bind) {
        super(nameE, valueE);
        this.bind = bind;

    }

    @Override
    public Iterator<Value> iterator(RenderContext rc) {

        Object o = bind;
        if (bind instanceof ValueListSrc) {
            o = ((ValueListSrc) bind).getValues(rc);
        }
        return new ItIteratorValue(this, DynamicLoop.getIterator(o));
    }

}

