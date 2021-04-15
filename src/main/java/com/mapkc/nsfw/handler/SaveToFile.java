package com.mapkc.nsfw.handler;

import com.mapkc.nsfw.FKNames;
import com.mapkc.nsfw.model.ActionHandler;
import com.mapkc.nsfw.model.RenderContext;
import com.mapkc.nsfw.util.logging.ESLogger;
import com.mapkc.nsfw.util.logging.Loggers;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.multipart.MixedAttribute;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;

/**
 * 将post过来的流内容写到文件
 * Created by chy on 14-7-30.
 */
public class SaveToFile implements ActionHandler {
    static ESLogger log = Loggers.getLogger(SaveToFile.class);

    @Override
    public boolean filterAction(RenderContext rc) {

        MixedAttribute ma = (MixedAttribute) rc.v(FKNames.FK_RAW_STREAM);
        File f;
        SimpleDateFormat day = new SimpleDateFormat("yyyyMMdd/HHmmss-");
        String id = day.format(System.currentTimeMillis()) ;
        File root=new File("../upload/" );

        String img = null;
        if (ma != null) {
            try {



                //ma.getChannelBuffer().readerIndex(23);
                ByteArrayOutputStream bos = new ByteArrayOutputStream(25);
                ByteBuf cb = ma.getByteBuf();
                byte b = cb.readByte();
                while (b != ',') {
                    bos.write(b);
                    b = cb.readByte();
                }
                String pxs = bos.toString();
                String type = pxs.substring(11, bos.size() - 7);



                id=id+   Math.random() + "." + type;
                f = new File(root ,id);

                f.getParentFile().mkdirs();
                ByteBuffer bs = java.util.Base64.getDecoder().decode(cb.nioBuffer());

                FileOutputStream fos = new FileOutputStream(f);
                fos.write(bs.array());
                fos.close();
//
            } catch (IOException e) {
                log.error("", e);
            }
        } else {
            try {

                String pxs = rc.getReqContent();
                log.debug("Posted base64:{}", pxs);
                String type = "png";
                id=id+   Math.random() + "." + type;
                f = new File(root ,id);
                if (!f.getParentFile().exists()) {
                    f.getParentFile().mkdirs();
                }
                int ix = pxs.indexOf(',');
                if (ix > 0)
                    pxs = pxs.substring(ix + 1);

                byte[] bs = java.util.Base64.getDecoder().decode(pxs);
                FileOutputStream fos = new FileOutputStream(f);
                fos.write(bs);
                fos.close();
//
            } catch (Exception e) {
                log.error("eror", e);
            }


        }
        rc.sendResponseText(id);
        return true;
    }
}
