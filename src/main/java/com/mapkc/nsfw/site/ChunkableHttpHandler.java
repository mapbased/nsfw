package com.mapkc.nsfw.site;

import com.mapkc.nsfw.FKNames;
import com.mapkc.nsfw.model.RenderContext;
import com.mapkc.nsfw.model.Site;
import com.mapkc.nsfw.util.Config;
import com.mapkc.nsfw.util.Strings;
import com.mapkc.nsfw.util.logging.ESLogger;
import com.mapkc.nsfw.util.logging.Loggers;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;

import java.io.File;
import java.util.concurrent.RejectedExecutionException;

/**
 * @author chy
 */
public class ChunkableHttpHandler extends SimpleChannelInboundHandler<HttpObject> {
    private static final HttpDataFactory THE_HttpDataFactory =
            new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);
    static ESLogger log = Loggers.getLogger(ChunkableHttpHandler.class);

    static {
        String uploadroot = Config.get().get("upload-root", "../upload/");
        File f = new File(uploadroot);
        if (!f.exists()) {
            f.mkdirs();
        }
        DiskFileUpload.deleteOnExitTemporaryFile = false; // should delete file
        // on exit (in
        // normal exit)
        DiskFileUpload.baseDirectory = f.getAbsolutePath(); // system temp
        // directory
        DiskAttribute.deleteOnExitTemporaryFile = false; // should delete file
        // on
        // exit (in normal exit)
        DiskAttribute.baseDirectory = f.getAbsolutePath(); // system temp
        // directory
    }

    final SiteManager siteManager;
    HttpRequest request;
    InterfaceHttpPostRequestDecoder decoder;
    private ChunkedRenderContext mrc;

    public ChunkableHttpHandler(SiteManager siteManager) {
        this.siteManager = siteManager;
    }

    public static final void log(RenderContext rc) {
        //  HttpHeaders httpHeaders = rc.getResp().headers();
        log.trace(
                "{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}",
                rc.getRemoteIP(), // ip
                System.currentTimeMillis() - rc.ctime, // 执行时间
                rc.getUserId(), // 用户id

                rc.getResp().status().code(), // code
                rc.getResp().headers().get(HttpHeaders.Names.CONTENT_LENGTH),
                rc.req.headers().get(HttpHeaders.Names.CONTENT_TYPE),

                rc.req.headers().get("uid"),
                rc.cookieValue(FKNames.FK_GUID), // guid
                rc.getMethod().name(), // method
                rc.getHeader(HttpHeaders.Names.HOST), // host
                rc.getUri(),// uri

                rc.getFormPostSrc(),// post
                rc.getReferer(), // refeer
                rc.getUserAgent()
        );
    }

    private void serviceRC(final RenderContext rc) {
        try {
            this.siteManager.executor.submit(new Runnable() {
                @Override
                public void run() {
                    runRC(rc);
                }
            });
        } catch (RejectedExecutionException e) {
            rc.sendError(HttpResponseStatus.SERVICE_UNAVAILABLE, "服务器繁忙，稍后再试");
        }
    }

    private void runRC(RenderContext rc) {
        String host = rc.getHeader(HttpHeaders.Names.HOST);
        // 去掉端口
//		int idx = host.lastIndexOf(":");
//		if (idx > 0) {
//			host = host.substring(0, idx);
//		}
        Site site = siteManager.getSite(host);
        if (site != null) {
            try {
                site.service(rc);
            } catch (Throwable e) {
                log.error(rc.getUri(), e);
                log(rc);
                rc.addException(e);
                if (rc.getChannel().isOpen()) {
                    rc.sendError(HttpResponseStatus.INTERNAL_SERVER_ERROR, Strings.throwableToString(e));
                }
            }
        } else {
            rc.sendNotFound();
        }
    }

    /**
     * @param ctx
     * @param cause
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

        cause.printStackTrace();
        ctx.channel().close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject message) throws Exception {

        if (message instanceof HttpRequest) {

            HttpRequest req = this.request = (HttpRequest) message;

            String ctype = String.valueOf(req.headers().get(HttpHeaders.Names.CONTENT_TYPE)).toLowerCase();
            if (ctype.startsWith(
                    HttpHeaders.Values.APPLICATION_X_WWW_FORM_URLENCODED)
                    || ctype.startsWith(HttpHeaders.Values.MULTIPART_FORM_DATA)
            ) {
                this.decoder = new HttpPostRequestDecoder(THE_HttpDataFactory, req);
            } else {
                decoder = new RawHttpPostRequestDecoder();
            }

        }

        if (message instanceof HttpContent) {
            HttpContent content = (HttpContent) message;
            if (this.decoder != null) {
                this.decoder.offer(content.retain());
            }
            if (content instanceof LastHttpContent) {

                FullHttpRequest fullHttpRequest;

                if (HttpUtil.isTransferEncodingChunked(request)) {

                    fullHttpRequest = new DefaultFullHttpRequest(request.protocolVersion(), request.method(), request.uri(), Unpooled.EMPTY_BUFFER, request.headers(), new DefaultHttpHeaders(false));
                } else {
                    fullHttpRequest = new DefaultFullHttpRequest(request.protocolVersion(), request.method(), request.uri(), content.content(), request.headers(), new DefaultHttpHeaders(false));

                }

                ChunkedRenderContext renderContext = new ChunkedRenderContext(ctx, fullHttpRequest);
                renderContext.postDecoder = decoder;
                renderContext.doExtractChrunkedContent();

                this.serviceRC(renderContext);


            }

        }


    }

    /**
     * <strong>Please keep in mind that this method will be renamed to
     * {@code messageReceived(ChannelHandlerContext, I)} in 5.0.</strong>
     * <p>
     * Is called for each message of type {@link I}.
     *
     * @param ctx the {@link ChannelHandlerContext} which this {@link SimpleChannelInboundHandler}
     *            belongs to
     * @throws Exception is thrown if an error occurred
     */
//    @Override
//    protected void channelRead1(ChannelHandlerContext ctx, HttpObject message) throws Exception {
//
//        // Object message = e.getMessage();
//        if (message instanceof HttpRequest) {
//            HttpRequest req = (HttpRequest) message;
//            if (HttpUtil.isTransferEncodingChunked(req)) {
//                mrc = new ChunkedRenderContext(ctx.channel(), message);
//                //application/x-www-form-urlencoded
//                String ctype = String.valueOf(req.headers().getTarget(HttpHeaders.Names.CONTENT_TYPE)).toLowerCase();
//                if (ctype.startsWith(
//                        HttpHeaders.Values.APPLICATION_X_WWW_FORM_URLENCODED)
//                        || ctype.startsWith(HttpHeaders.Values.MULTIPART_FORM_DATA)
//                        ) {
//                    mrc.postDecoder = new HttpPostRequestDecoder(req);
//                } else {
//                    mrc.postDecoder = new RawHttpPostRequestDecoder();
//                }
//                this.runRC(mrc);
//            } else {
//                RenderContext rc = new RenderContext(ctx.channel(), message);
//                this.serviceRC(rc);
//            }
//            return;
//        }
//        //用于文件上传
//        if (message instanceof HttpContent) {
//            try {
//                HttpContent chunk = (HttpContent) message;
//                mrc.onChunkCome(chunk);
//                if (chunk instanceof LastHttpContent) {
//                    mrc.continueHandle();
//                }
//            } catch (Exception ex) {
//                log.error("", ex);
//                log(mrc);
//            }
//        }
//
//    }


}
