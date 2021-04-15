package com.mapkc.nsfw.handler;

import com.mapkc.nsfw.model.ActionHandler;
import com.mapkc.nsfw.model.RenderContext;
import com.mapkc.nsfw.util.logging.ESLogger;
import com.mapkc.nsfw.util.logging.Loggers;
import io.netty.handler.codec.http.multipart.FileUpload;

import java.io.File;
import java.io.IOException;

/**
 * 文件上传
 * Created by chenyuan on 14-9-1.
 */
public class Uploadmulti implements ActionHandler {

    static ESLogger log = Loggers.getLogger(Uploadmulti.class);

    public static File uploadFile(FileUpload fileUpload) throws IOException {
        File file = null;
        if (fileUpload.isInMemory()) {
            String n = fileUpload.getFilename();
            if (n.lastIndexOf('.') > 0) {
                n = n.substring(n.lastIndexOf('.'));
            }
            file = new File("../storeroot/default/pathroot/upload/" + System.currentTimeMillis()
                    + n);

            fileUpload.renameTo(file);
        } else {
            file = fileUpload.getFile();
        }
        return file;
    }

    @Override
    public boolean filterAction(RenderContext rc) {
        if (!rc.isPost()) {
            return false;
        }
        try {
            FileUpload fileUpload = rc.getFileUpload("upload");


            File file = uploadFile(fileUpload);
//            FileClient dzqClient = FileUploaderUtil.upload(file);
//            log.debug("返回参数: ResponseText={}", dzqClient.getResponseText());
//            if (dzqClient.isUploadOk()) {
//                String filename = dzqClient.getFilename();
////				StringBuffer sb = new StringBuffer();
////				sb.append("{\"filename\":\"" + filename + "\"}");
//                rc.sendResponseHtml(filename);
//            } else {
//                rc.sendError(HttpResponseStatus.BAD_REQUEST);
//            }
            file.delete();
        } catch (IOException e) {
            log.error("文件上传失败", e);
            rc.sendServerError(e);
        }
        return true;
    }
}
