package com.mapkc.nsfw.util.concurrent;

import com.mapkc.nsfw.util.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ConcurrentExecutor {

    final public static ExecutorService executor = Executors
            .newCachedThreadPool();
    final public static ExecutorService executor2 = new ThreadPoolExecutor(
            Config.get().getInt("concurrent.executor.thread.num", 8), Config
            .get().getInt("concurrent.executor.thread.num", 8), 0L,
            TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(Config
            .get().getInt("concurrent.executor.queue.num", 100000)));

    /**
     * sync
     *
     * @param <Response>
     * @param taskList
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public static <Response> List<Response> execute(
            List<Callable<Response>> taskList) throws InterruptedException,
            ExecutionException {

        return get(asyncExecute(taskList));
    }

    /**
     * async
     *
     * @param <Response>
     * @param taskList
     * @return
     */
    public static <Response> List<Future<Response>> asyncExecute(
            List<Callable<Response>> taskList) {
        List<Future<Response>> futureList = new ArrayList<Future<Response>>(
                taskList.size());
        for (int i = 0; i < taskList.size(); i++) {
            futureList.add(executor.submit(taskList.get(i)));
        }
        return futureList;
    }

    /**
     * @param <Response>
     * @param futureList
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public static <Response> List<Response> get(
            List<Future<Response>> futureList) throws InterruptedException,
            ExecutionException {

        List<Response> respList = new ArrayList<Response>(futureList.size());

        for (int i = 0; i < futureList.size(); i++) {
            respList.add(futureList.get(i).get());
        }
        return respList;

    }

}
