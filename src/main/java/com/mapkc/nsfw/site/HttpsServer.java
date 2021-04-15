package com.mapkc.nsfw.site;

import com.mapkc.nsfw.http.SslContextFactory;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLEngine;

public class HttpsServer extends HttpServer {


    public HttpsServer(SiteManager siteManager) {
        super(siteManager);

    }

    @Override
    protected void addSSL(ChannelPipeline pipeline) {
        SSLEngine engine = SslContextFactory.getServerContext()
                .createSSLEngine();
        engine.setUseClientMode(false);
        pipeline.addLast("ssl", new SslHandler(engine));

    }

    @Override
    protected int defaultPort() {
        return 443;
    }

    @Override
    protected String getSeverName() {
        return "httpsserver";
    }

}
