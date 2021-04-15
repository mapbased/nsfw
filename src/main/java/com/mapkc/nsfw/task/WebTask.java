package com.mapkc.nsfw.task;

/**
 * Created by chy on 14-5-14.
 */
public interface WebTask extends Runnable {

    long ctime();

    float getPercent();

    String getStack();

    String getName();

    String getProgressInfo();

}
