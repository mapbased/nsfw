package com.mapkc.nsfw.model;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by chy on 16/5/9.
 */
public class Slaves extends XEnum {
    AtomicLong dx = new AtomicLong(0);

    @Override
    protected String defaultIcon() {
        return "fa   fa-cogs";
    }

    @Override
    public String[] mightChildTypes() {

        return new String[]{SlaveInfo.class.getSimpleName()};
    }

    public Connection selectConnection() throws SQLException {
        List<SlaveInfo> cs = this.getChildren(SlaveInfo.class);
        long cnt = dx.incrementAndGet();

        if (cs.size() == 0) {
            return null;
        }
        return cs.get((int) (cnt % cs.size())).getConnection();


    }


}