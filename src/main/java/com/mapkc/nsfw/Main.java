package com.mapkc.nsfw;

import com.mapkc.nsfw.site.HttpServer;
import com.mapkc.nsfw.site.HttpsServer;
import com.mapkc.nsfw.site.SiteManager;
import com.mapkc.nsfw.util.logging.ESLogger;
import com.mapkc.nsfw.util.logging.Loggers;
import org.mvel2.optimizers.OptimizerFactory;

import java.io.File;

public class Main {
    final static ESLogger log = Loggers.getLogger(Main.class);

    public static void main(String[] ss) {

        System.out.println(new File(".").getAbsoluteFile().toString());


        OptimizerFactory.setDefaultOptimizer("ASM");


        final SiteManager siteManager = new SiteManager();
        final HttpServer server = new HttpServer(siteManager);
        server.start();
        final HttpServer servers = new HttpsServer(siteManager);
        servers.start();


        java.lang.Runtime.getRuntime().addShutdownHook(
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        server.stop();

                        log.info("Server stopped!");

                        try {
                            Thread.sleep(1000);
                            // 停止1s等待可能sql执行完毕
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                })
        );
    }

}
