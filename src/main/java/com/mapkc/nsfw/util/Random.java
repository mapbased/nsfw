package com.mapkc.nsfw.util;

/**
 * Created by chy on 14-11-10.
 */
public class Random {

    static public String randStr() {
        return new java.math.BigInteger(100, java.util.concurrent.ThreadLocalRandom.current()).toString(36);
    }

    static public String createGUID() {
        StringBuilder sb = new StringBuilder(10);
        sb.append(randStr()).append(
                Long.toString(System.currentTimeMillis() / 1000, 36));
        return sb.toString();
    }
}
