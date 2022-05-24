package com.mapkc.nsfw.site;

import com.mapkc.nsfw.util.Config;
import com.mapkc.nsfw.util.logging.ESLogger;
import com.mapkc.nsfw.util.logging.Loggers;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class HttpServer {

    protected ESLogger log = Loggers.getLogger(this.getSeverName());

    protected ServerBootstrap bootstrap;
    protected Channel channel = null;
    SiteManager siteManager;

    public HttpServer(SiteManager siteManager) {
        this.siteManager = siteManager;

    }

    protected void addSSL(ChannelPipeline pipeline) {
        // do nothing

    }


    protected final SocketAddress getSocketAddress() {
        // Inet4Address address=new Inet4Address();

        String hostname = Config.get().get(this.getSeverName() + ".host");
        if (hostname != null)
            return new InetSocketAddress(hostname, Config.get().getInt(
                    this.getSeverName() + ".port", this.defaultPort()));
        return new InetSocketAddress(Config.get().getInt(
                this.getSeverName() + ".port", this.defaultPort()));
    }

    protected int defaultPort() {
        return 80;
    }

    protected String getSeverName() {
        return "httpserver";
    }


    ChannelInitializer<SocketChannel> channelInitializer() {
        return new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                addSSL(pipeline);

                pipeline.addLast("decoder", new HttpRequestDecoder());
                // pipeline.addLast("aggregator", new HttpChunkAggregator(
                // 1024 * 1024));
                pipeline.addLast("encoder", new HttpResponseEncoder());
                //  pipeline.addLast("deflater", new HttpContentCompressor());
                //  pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());

                pipeline.addLast("handler", new ChunkableHttpHandler(
                        siteManager));


            }
        };
    }

    /**
     * start
     */
    public void start() {
        // this.channelFactory = this.createChannelFactory();

        bootstrap = new ServerBootstrap();
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        bootstrap.group(bossGroup, workerGroup);
        bootstrap.channel(NioServerSocketChannel.class);

        bootstrap.childHandler(channelInitializer());
        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.childOption(ChannelOption.TCP_NODELAY, true);


        try {
            channel = bootstrap.bind(this.getSocketAddress()).sync().channel();
            channel.closeFuture().sync();

        } catch (Exception e) {
            log.error("Error while start server", e);
        } finally {


            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();


        }

    }

    /**
     * stop
     */
    public void stop() {
        log.info(" server:{} is stopping ...", this.getSeverName());
        channel.close().syncUninterruptibly();

        // if (channelFactory != null) {
        // new Thread(getSeverName() + "-destory") {
        // @Override
        // public void run() {
        // channelFactory.releaseExternalResources();
        // }
        // }.start();
        //
        // }

    }
}
