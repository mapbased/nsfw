/**
 *
 */
package com.mapkc.nsfw.vl;

import com.mapkc.nsfw.model.RenderContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author chy
 */
public class EnumValueList extends DynamicValueList {


    List<Object> list;

    EnumValueList(String nameE, String valueE, Class c) {

        super(nameE, valueE);

        Object[] vs = c.getEnumConstants();
        list = new ArrayList<Object>(vs.length);
        for (Object o : vs) {
            list.add(o);
        }
        // TODO Auto-generated constructor stub
    }

    @Override
    public Iterator<Value> iterator(RenderContext rc) {
        // TODO Auto-generated method stub
        return new ItIteratorValue(this, list.iterator());
    }

    /* (non-Javadoc)
     * @see AbstractValueList#getScreenName(java.lang.Object)
     */
    @Override
    protected String getScreenName(Object o) {

        if (this.nameExp == null) {
            return o.toString();
        }
        return super.getScreenName(o);
    }

    /* (non-Javadoc)
     * @see AbstractValueList#getValue(java.lang.Object)
     */
    @SuppressWarnings("rawtypes")
    @Override
    protected String getValue(Object o) {
        if (this.valueExp == null) {
            // name 保持不变还是 index保持不变？
            return ((java.lang.Enum) o).name();
        }
        return super.getValue(o);
    }

}
