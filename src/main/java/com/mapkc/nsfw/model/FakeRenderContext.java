/**
 *
 */
package com.mapkc.nsfw.model;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * @author chy
 */
public class FakeRenderContext extends RenderContext {


    Channel channel = new Channel() {
        @Override
        public ChannelId id() {
            return null;
        }

        @Override
        public EventLoop eventLoop() {
            return null;
        }

        @Override
        public Channel parent() {
            return null;
        }

        @Override
        public ChannelConfig config() {
            return null;
        }

        @Override
        public boolean isOpen() {
            return false;
        }

        @Override
        public boolean isRegistered() {
            return false;
        }

        @Override
        public boolean isActive() {
            return false;
        }

        @Override
        public ChannelMetadata metadata() {
            return null;
        }

        @Override
        public SocketAddress localAddress() {
            return null;
        }

        @Override
        public SocketAddress remoteAddress() {
            return null;
        }

        @Override
        public ChannelFuture closeFuture() {
            return null;
        }

        @Override
        public boolean isWritable() {
            return false;
        }

        @Override
        public long bytesBeforeUnwritable() {
            return 0;
        }

        @Override
        public long bytesBeforeWritable() {
            return 0;
        }

        @Override
        public Unsafe unsafe() {
            return null;
        }

        @Override
        public ChannelPipeline pipeline() {
            return null;
        }

        @Override
        public ByteBufAllocator alloc() {
            return null;
        }

        @Override
        public Channel read() {
            return null;
        }

        @Override
        public Channel flush() {
            return null;
        }

        @Override
        public ChannelFuture bind(SocketAddress localAddress) {
            return null;
        }

        @Override
        public ChannelFuture connect(SocketAddress remoteAddress) {
            return null;
        }

        @Override
        public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress) {
            return null;
        }

        @Override
        public ChannelFuture disconnect() {
            return null;
        }

        @Override
        public ChannelFuture close() {
            return null;
        }

        @Override
        public ChannelFuture deregister() {
            return null;
        }

        @Override
        public ChannelFuture bind(SocketAddress localAddress, ChannelPromise promise) {
            return null;
        }

        @Override
        public ChannelFuture connect(SocketAddress remoteAddress, ChannelPromise promise) {
            return null;
        }

        @Override
        public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
            return null;
        }

        @Override
        public ChannelFuture disconnect(ChannelPromise promise) {
            return null;
        }

        @Override
        public ChannelFuture close(ChannelPromise promise) {
            return null;
        }

        @Override
        public ChannelFuture deregister(ChannelPromise promise) {
            return null;
        }

        @Override
        public ChannelFuture write(Object msg) {
            return new ChannelFuture() {
                @Override
                public Channel channel() {
                    return null;
                }

                @Override
                public ChannelFuture addListener(GenericFutureListener<? extends Future<? super Void>> listener) {
                    return null;
                }

                @Override
                public ChannelFuture addListeners(GenericFutureListener<? extends Future<? super Void>>... listeners) {
                    return null;
                }

                @Override
                public ChannelFuture removeListener(GenericFutureListener<? extends Future<? super Void>> listener) {
                    return null;
                }

                @Override
                public ChannelFuture removeListeners(GenericFutureListener<? extends Future<? super Void>>... listeners) {
                    return null;
                }

                @Override
                public ChannelFuture sync() throws InterruptedException {
                    return null;
                }

                @Override
                public ChannelFuture syncUninterruptibly() {
                    return null;
                }

                @Override
                public ChannelFuture await() throws InterruptedException {
                    return null;
                }

                @Override
                public ChannelFuture awaitUninterruptibly() {
                    return null;
                }

                @Override
                public boolean isVoid() {
                    return false;
                }

                @Override
                public boolean isSuccess() {
                    return false;
                }

                @Override
                public boolean isCancellable() {
                    return false;
                }

                @Override
                public Throwable cause() {
                    return null;
                }

                @Override
                public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
                    return false;
                }

                @Override
                public boolean await(long timeoutMillis) throws InterruptedException {
                    return false;
                }

                @Override
                public boolean awaitUninterruptibly(long timeout, TimeUnit unit) {
                    return false;
                }

                @Override
                public boolean awaitUninterruptibly(long timeoutMillis) {
                    return false;
                }

                @Override
                public Void getNow() {
                    return null;
                }

                @Override
                public boolean cancel(boolean mayInterruptIfRunning) {
                    return false;
                }

                @Override
                public boolean isCancelled() {
                    return false;
                }

                @Override
                public boolean isDone() {
                    return false;
                }

                @Override
                public Void get() throws InterruptedException, ExecutionException {
                    return null;
                }

                @Override
                public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                    return null;
                }
            };
        }

        @Override
        public ChannelFuture write(Object msg, ChannelPromise promise) {
            return null;
        }

        @Override
        public ChannelFuture writeAndFlush(Object msg, ChannelPromise promise) {
            return null;
        }

        @Override
        public ChannelFuture writeAndFlush(Object msg) {
            return null;
        }

        @Override
        public ChannelPromise newPromise() {
            return null;
        }

        @Override
        public ChannelProgressivePromise newProgressivePromise() {
            return null;
        }

        @Override
        public ChannelFuture newSucceededFuture() {
            return null;
        }

        @Override
        public ChannelFuture newFailedFuture(Throwable cause) {
            return null;
        }

        @Override
        public ChannelPromise voidPromise() {
            return null;
        }

        @Override
        public <T> Attribute<T> attr(AttributeKey<T> key) {
            return null;
        }

        @Override
        public <T> boolean hasAttr(AttributeKey<T> key) {
            return false;
        }

        @Override
        public int compareTo(Channel o) {
            return 0;
        }
    };

    /**

     */
    public FakeRenderContext(Site site) {
        super(site, new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
                HttpMethod.GET, "/"));


    }

    public FakeRenderContext(Site s, DefaultFullHttpRequest req) {
        super(s, req);
    }

    public void setCookie(RenderContext rc) {
        this.cookies = rc.cookies;
        this.setCookie = rc.setCookie;

    }

    @Override
    public Channel getChannel() {

        return channel;
    }

    public String getRenderedString() {
        try {
            return this.getRenderedContent().toString("utf-8");
        } catch (UnsupportedEncodingException e) {
            return this.getRenderedContent().toString();
        }
    }

    public ByteArrayOutputStream getRenderedContent() {
        try {
            this.writer.close();
        } catch (IOException e) {
            log.error("error close writer", e);
        }
        return this.baos;

    }

}
