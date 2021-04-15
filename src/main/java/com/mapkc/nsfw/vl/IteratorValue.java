package com.mapkc.nsfw.vl;

import java.util.Iterator;


abstract class IteratorValue implements Value, Iterator<Value> {
    final AbstractValueList avl;

    protected Object current;

    IteratorValue(AbstractValueList avl) {
        this.avl = avl;
    }


    @Override
    final public void remove() {


    }

    @Override
    public String getScreenName() {

        return avl.getScreenName(this.current);
    }

    @Override
    public String getValue() {

        return avl.getValue(this.current);
    }

    @Override
    public Object getSrc() {
        // TODO Auto-generated method stub
        return current;
    }


}
