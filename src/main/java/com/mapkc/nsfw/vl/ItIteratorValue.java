package com.mapkc.nsfw.vl;

/**
 * TODO can avoid an object creating by add more code
 *
 * @author chy
 */
public class ItIteratorValue extends IteratorValue {

    final java.util.Iterator it;

    ItIteratorValue(AbstractValueList avl, java.util.Iterator it) {
        super(avl);
        this.it = it;
    }

    @Override
    public boolean hasNext() {
        // TODO Auto-generated method stub
        return it.hasNext();
    }

    @Override
    public Value next() {
        if (!it.hasNext()) {
            return null;
        }
        this.current = it.next();

        // TODO Auto-generated method stub
        return this;
    }

}
