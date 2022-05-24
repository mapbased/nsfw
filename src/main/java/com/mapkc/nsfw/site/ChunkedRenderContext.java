package com.mapkc.nsfw.site;

import com.mapkc.nsfw.model.RenderContext;
import com.mapkc.nsfw.model.ReqHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpPostRequestDecoder;

import java.io.IOException;

public class ChunkedRenderContext extends RenderContext {
    InterfaceHttpPostRequestDecoder postDecoder;
    private ReqHandler reqHandler;

    public ChunkedRenderContext(ChannelHandlerContext channel, FullHttpRequest request) {
        super(channel, request);

    }

    public synchronized void setReqHandler(ReqHandler reqHandler) {
        this.reqHandler = reqHandler;
    }

    /**
     * file upload will need overwite this method
     */
    @Override
    protected void extractChrunkedContent() {
        //super.extractChrunkedContent();
    }

    public void doExtractChrunkedContent() {
        super.extractChrunkedContent();
    }

    public byte[] getReqRawContent() {
        if (this.postDecoder instanceof RawHttpPostRequestDecoder) {
            RawHttpPostRequestDecoder rawHttpPostRequestDecoder = (RawHttpPostRequestDecoder) this.postDecoder;
            try {
                return rawHttpPostRequestDecoder.rowdata.get();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return super.getReqRawContent();

    }

    @Override
    public String getReqContent(String contenttype) {
        if (this.postDecoder instanceof RawHttpPostRequestDecoder) {
            RawHttpPostRequestDecoder rawHttpPostRequestDecoder = (RawHttpPostRequestDecoder) this.postDecoder;
            try {
                return rawHttpPostRequestDecoder.rowdata.getString(getCharset(contenttype));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return super.getReqContent(contenttype);
    }

    @Override
    protected InterfaceHttpPostRequestDecoder getHttpPostRequestDecoder()
            throws HttpPostRequestDecoder.ErrorDataDecoderException {
        return postDecoder;
    }

    @Override
    protected void cleanup() {
        super.cleanup();
        if(this.postDecoder!=null){
            this.postDecoder.destroy();
        }
    }
}