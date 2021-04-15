package com.mapkc.nsfw.query;

import com.mapkc.nsfw.vl.Value;

import java.util.*;

/**
 * Created by chy on 14-10-15.
 */
public class LinkItemContainer {
    List<LinkItemObj> list = new ArrayList<LinkItemObj>();
    Map<String, LinkItemObj> map = new HashMap<String, LinkItemObj>();


    public LinkItem linkItem(final QueryResult queryResult,
                             final String fieldname) {

        return new LinkItem() {
            String p = queryResult.config.componentId + "." + fieldname;

            Iterator<LinkItemObj> i = list.iterator();
            LinkItemObj obj;

            @Override
            public String getLabel() {
                return obj.screenName;
            }

            @Override
            public String getLink() {
                return queryResult.changeUrl(p, obj.id);
            }

            @Override
            public int getCount() {
                return obj.count;
            }

            @Override
            public boolean isSelected() {
                return queryResult.rc.param(p, "").equals(obj.id);
            }

            @Override
            public boolean hasMoreElements() {
                return i.hasNext();
            }

            @Override
            public LinkItem nextElement() {
                obj = i.next();
                return this;
            }
        };
    }

    public void add(Value value) {
        this.add(value.getScreenName(), value.getValue());
    }

    public void add(String screenName, String id) {
        if (map.containsKey(id)) {
            return;
        }
        LinkItemObj linkItemObj = new LinkItemObj();
        linkItemObj.screenName = screenName;
        linkItemObj.id = id;
        list.add(linkItemObj);
        map.put(id, linkItemObj);
    }

    public void add(String screenName, String id, int cnt) {

        LinkItemObj lo = map.get(id);
        if (lo != null) {
            lo.count = cnt;
            if (lo.screenName == lo.id)
                lo.screenName = screenName;
            return;
        }
        LinkItemObj linkItemObj = new LinkItemObj();
        linkItemObj.screenName = screenName;
        linkItemObj.id = id;
        linkItemObj.count = cnt;
        list.add(linkItemObj);
        map.put(id, linkItemObj);

    }
}