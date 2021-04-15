package com.mapkc.nsfw.ses;

import com.mapkc.nsfw.model.Site;
import com.mapkc.nsfw.util.Random;
import com.mapkc.nsfw.util.logging.ESLogger;
import com.mapkc.nsfw.util.logging.Loggers;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MemSessionStore implements SessionStore {


    final static ESLogger log = Loggers.getLogger(MemSessionStore.class);

    // private ConcurrentMap<String, Session> sessions;
    com.google.common.cache.Cache<String, Session> cache;

    // ConcurrentHashMap<String, Session>();


    public MemSessionStore(Site site) {
        cache = com.google.common.cache.CacheBuilder
                .newBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build();
        //  this.sessions = cache.asMap();

    }

    static public void main(String[] ss) {


    }

    public Session getSession(String sid) {


        return this.cache.getIfPresent(sid);
    }

    public Session createSession() {

        String sid = Random.randStr();


        try {
            return cache.get(sid, new Callable<Session>() {
                @Override
                public Session call() throws Exception {
                    return new Session(sid);
                }
            });
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
            // log.error("Cannot create session",e);
        }

//        if (this.sessions.putIfAbsent(sid, s) != null) {
//
//            log.error("Session dup:{}", sid);
//
//            return this.createSession();
//        }
//        return s;
        // return seq.incrementAndGet() + "-" + Math.random();
    }

}
