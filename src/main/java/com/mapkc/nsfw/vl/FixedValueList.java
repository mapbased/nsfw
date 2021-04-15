package com.mapkc.nsfw.vl;

import com.mapkc.nsfw.model.RenderContext;
import com.mapkc.nsfw.util.StringPair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FixedValueList extends AbstractValueList {

    List<StringPair> values;

    FixedValueList(List<StringPair> values) {

        this.values = values;

    }

    public static FixedValueList from(String[] ss) {
        List<StringPair> sps = new ArrayList<>(ss.length);
        for (String as : ss) {
            String s = as.trim();
            sps.add(new StringPair(s, s));
        }
        return new FixedValueList(sps);
    }

    @Override
    public Iterator<Value> iterator(RenderContext rc) {
        IteratorValue iv = new ItIteratorValue(this, values.iterator());
        return iv;
    }

    @Override
    protected String getScreenName(Object o) {
        // TODO Auto-generated method stub
        return ((StringPair) o).name;
    }

    @Override
    protected String getValue(Object o) {
        return ((StringPair) o).value;
    }


}
