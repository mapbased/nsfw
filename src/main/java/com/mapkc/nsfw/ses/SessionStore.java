package com.mapkc.nsfw.ses;

/**
 * Created by chy on 14-11-10.
 */
public interface SessionStore {

    Session getSession(String sid);

    Session createSession();


}
