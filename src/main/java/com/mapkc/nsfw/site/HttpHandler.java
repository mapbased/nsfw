//package com.mapkc.nsfw.site;
//
//import com.mapkc.nsfw.FKNames;
//import com.mapkc.nsfw.model.RenderContext;
//import com.mapkc.nsfw.model.Site;
//import com.mapkc.nsfw.util.Strings;
//import com.mapkc.nsfw.util.concurrent.JMXConfigurableThreadPoolExecutor;
//import com.mapkc.nsfw.util.logging.ESLogger;
//import com.mapkc.nsfw.util.logging.Loggers;
//import org.jboss.netty.channel.SimpleChannelHandler;
//
//import java.util.concurrent.RejectedExecutionException;
//import java.util.concurrent.ThreadPoolExecutor;
//
//@Deprecated
//public class HttpHandler extends SimpleChannelHandler {
//    static ESLogger log = Loggers.getLogger(HttpHandler.class);
//    final SiteManager siteManager;
//    ThreadPoolExecutor executor = new JMXConfigurableThreadPoolExecutor(4, 50,
//            1000, "Main");
//
//    public HttpHandler(SiteManager siteManager) {
//        this.siteManager = siteManager;
//    }
//
//    @Override
//    public void messageReceived(ChannelHandlerContext ctx, final MessageEvent me)
//            throws Exception {
//        // HttpRequest hr = (HttpRequest) me.getMessage();
//
//        final RenderContext rc = new RenderContext(me);
//        // rc.addHeader(Names.SERVER, "Nsfw");
//        try {
//            executor.submit(new Runnable() {
//
//                @Override
//                public void run() {
//
//                    String host = rc.getHeader(HttpHeaders.Names.HOST);
//
//                    Site site = siteManager.getSite(host);
//
//
//                    if (site != null) {
//                        try {
//                            site.service(rc);
//                        } catch (Exception e) {
//                            rc.addException(e);
//                            log.error(rc.req.getUri(), e);
//                            rc.sendError(HttpResponseStatus.INTERNAL_SERVER_ERROR,
//                                    Strings.throwableToString(e));
//                        }
//                    } else {
//                        rc.sendNotFound();
//
//                    }
//                    log(rc);
//
//
//                }
//            });
//        } catch (RejectedExecutionException e) {
//            log(rc);
//            rc.sendError(HttpResponseStatus.SERVICE_UNAVAILABLE, "服务器繁忙，稍后再试");
//
//        }
//
//    }
//
//    private void log(RenderContext rc) {
//        log.trace("{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}",
//                rc.getRemoteIP(), // ip
//
//                System.currentTimeMillis() - rc.ctime, // 执行时间
//                rc.getUserId(), // 用户id
//                rc.getResp().getStatus().getCode(), // code
//
//                rc.getResp().headers().getTarget(HttpHeaders.Names.CONTENT_LENGTH),
//                rc.cookieValue(FKNames.FK_GUID), // guid
//                rc.getMethod().getName(), // method
//                rc.getHeader(HttpHeaders.Names.HOST), // host
//                rc.getUri(),// uri
//                rc.getFormPostSrc(),// post
//                rc.getReferer(), // refeer
//                rc.getUserAgent()
//
//        );
//    }
//
//    @Override
//    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
//            throws Exception {
//
//
//        e.getCause().printStackTrace();
//        e.getChannel().close();
//    }
//
//}