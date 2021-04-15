package com.mapkc.nsfw.vl;

import com.mapkc.nsfw.binding.Binding;
import com.mapkc.nsfw.model.RenderContext;
import com.mapkc.nsfw.util.DynamicLoop;

import java.util.Iterator;

/**
 * @author chy
 */
public class BindingValueList extends DynamicValueList {

    Binding bind;

    BindingValueList(String nameE, String valueE, Binding bind) {
        super(nameE, valueE);
        this.bind = bind;
        // TODO Auto-generated constructor stub
    }

    @Override
    public Iterator<Value> iterator(RenderContext rc) {
        // TODO Auto-generated method stub
        Object o = this.bind.getValue(rc);
        if (o == null) {
            return EMPTY;
        }

        return new ItIteratorValue(this, DynamicLoop.getIterator(o));
    }

}
