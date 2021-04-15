package com.mapkc.nsfw.handler;

import com.mapkc.nsfw.model.ActionHandler;
import com.mapkc.nsfw.model.RenderContext;
import com.mapkc.nsfw.util.logging.ESLogger;
import com.mapkc.nsfw.util.logging.Loggers;
import io.netty.handler.codec.http.multipart.FileUpload;

import java.io.File;
import java.io.IOException;

/**
 * Created by chy on 14-8-30.
 */
public class Upload implements ActionHandler {
    static ESLogger log = Loggers.getLogger(Upload.class);

    @Override
    public boolean filterAction(RenderContext rc) {
        if (rc.isGet()) {
            return false;
        }
        try {
            FileUpload fileUpload = rc.getFileUpload("upload");


            File file = Uploadmulti.uploadFile(fileUpload);
            //File file = fileUpload.getFile();
//
//            FileClient dzqClient = FileUploaderUtil.upload(file);
//            log.debug("返回参数: ResponseText={}", dzqClient.getResponseText());
//            if (dzqClient.isUploadOk()) {
//                String img_server = Config.getTarget().getTarget("img_server");
//                String funcNum = rc.param("CKEditorFuncNum");
//
//                StringBuffer sb = new StringBuffer();
//                sb.append("<script>");
//                sb.append("window.parent.CKEDITOR.tools.callFunction(" + funcNum + ", '"
//                        + img_server + "/" + dzqClient.getFilename() + "');");
//                sb.append("</script>");
//                rc.sendResponseHtml(sb.toString());
//            } else {
//                rc.sendError(HttpResponseStatus.BAD_GATEWAY, "文件上传错误");
//            }
        } catch (IOException e) {
            log.error("文件上传失败", e);
            rc.sendServerError(e);
        }
        return true;
    }
}
