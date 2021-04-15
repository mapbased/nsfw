/**
 *
 */
package com.mapkc.nsfw.vl;

import com.mapkc.nsfw.model.RenderContext;
import com.mapkc.nsfw.model.XEnum;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author chy
 */
public class XEnumValueList extends AbstractValueList {

    String path;
    String nameExp;
    String valueExp;

    public XEnumValueList(String path, String nameExp,
                          String valueExp) {
        this.path = path.trim();
        this.nameExp = nameExp;
        this.valueExp = valueExp;
    }

    /* (non-Javadoc)
     * @see AbstractValueList#iterator(RenderContext)
     */
    @Override
    public Iterator<Value> iterator(RenderContext rc) {

        XEnum x = rc.getSite().getXEnum(path);
        if (x == null) {
            return null;
        }
        // TODO Auto-generated method stub
        // Iterator<Map.Entry<String, VolatileBag<XEnum>>> ii =
        // x.getItems().entrySet().iterator();

        List<XEnum> xs = x.getChildren();
        Collections.sort(xs);
        return new ItIteratorValue(this, xs.iterator());
    }

    /* (non-Javadoc)
     * @see AbstractValueList#getScreenName(java.lang.Object)
     */
    @Override
    protected String getScreenName(Object o) {
        XEnum x = (XEnum) o;
        if (this.nameExp != null) {
            String s = x.getAttribute(nameExp);
            if (s != null) {
                return s;
            }

        }
        return x.getScreenName();
    }

    /* (non-Javadoc)
     * @see AbstractValueList#getValue(java.lang.Object)
     */
    @Override
    protected String getValue(Object o) {

        XEnum x = (XEnum) o;
        if (this.valueExp != null) {
            String s = x.getAttribute(valueExp);
            if (s != null) {
                return s;
            }

        }
        return x.getName();
    }

}
