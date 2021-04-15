package com.mapkc.nsfw.site;

import com.mapkc.nsfw.FKNames;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.MixedAttribute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chy on 14-7-30.
 */
public class RawHttpPostRequestDecoder implements InterfaceHttpPostRequestDecoder {

    MixedAttribute rowdata = new MixedAttribute(FKNames.FK_RAW_STREAM, 1024 * 1024 * 5);

    @Override
    public boolean isMultipart() {
        return false;
    }

    @Override
    public List<InterfaceHttpData> getBodyHttpDatas() throws HttpPostRequestDecoder.NotEnoughDataDecoderException {
        List<InterfaceHttpData> l = new ArrayList<>(1);
        l.add(rowdata);
        return l;
    }

    @Override
    public List<InterfaceHttpData> getBodyHttpDatas(String name) throws HttpPostRequestDecoder.NotEnoughDataDecoderException {
        List<InterfaceHttpData> l = new ArrayList<>(1);
        l.add(rowdata);
        return l;
    }

    @Override
    public InterfaceHttpData getBodyHttpData(String name) throws HttpPostRequestDecoder.NotEnoughDataDecoderException {
        return rowdata;
    }


    @Override
    public boolean hasNext() throws HttpPostRequestDecoder.EndOfDataDecoderException {
        return false;
    }

    @Override
    public InterfaceHttpData next() throws HttpPostRequestDecoder.EndOfDataDecoderException {
        return null;
    }

    @Override
    public void cleanFiles() {
        rowdata.delete();
    }

    @Override
    public void removeHttpDataFromClean(InterfaceHttpData data) {

    }

    /**
     * Return the threshold in bytes after which read data in the buffer should be discarded.
     */
    @Override
    public int getDiscardThreshold() {
        return 0;
    }

    /**
     * Set the amount of bytes after which read bytes in the buffer should be discarded.
     * Setting this lower gives lower memory usage but with the overhead of more memory copies.
     * Use {@code 0} to disable it.
     *
     * @param discardThreshold
     */
    @Override
    public void setDiscardThreshold(int discardThreshold) {

    }

    /**
     * Initialized the internals from a new chunk
     *
     * @param content the new received chunk
     * @throws HttpPostRequestDecoder.ErrorDataDecoderException if there is a problem with the charset decoding or other
     *                                                          errors
     */
    @Override
    public InterfaceHttpPostRequestDecoder offer(HttpContent content) {

        try {
            rowdata.addContent(content.content(), content instanceof LastHttpContent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * Returns the current InterfaceHttpData if currently in decoding status,
     * meaning all data are not yet within, or null if there is no InterfaceHttpData
     * currently in decoding status (either because none yet decoded or none currently partially
     * decoded). Full decoded ones are accessible through hasNext() and next() methods.
     *
     * @return the current InterfaceHttpData if currently in decoding status or null if none.
     */
    @Override
    public InterfaceHttpData currentPartialHttpData() {
        return null;
    }

    /**
     * Destroy the {@link InterfaceHttpPostRequestDecoder} and release all it resources. After this method
     * was called it is not possible to operate on it anymore.
     */
    @Override
    public void destroy() {

        this.cleanFiles();
        //  this.rowdata.release();

    }
}
