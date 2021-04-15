package com.mapkc.nsfw.util.concurrent;

public interface JMXConfigurableThreadPoolExecutorMBean {
    /**
     * Get the current number of running tasks
     */
    int getActiveCount();

    /**
     * Get the number of completed tasks
     */
    long getCompletedTasks();

    /**
     * Get the number of tasks waiting to be executed
     */
    long getPendingTasks();

    int getMaximumPoolSize();

    void setMaximumPoolSize(int n);

    int getCorePoolSize();

    void setCorePoolSize(int n);

}
