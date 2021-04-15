package com.mapkc.nsfw.input;

import com.mapkc.nsfw.model.FormModel;
import com.mapkc.nsfw.model.RenderContext;
import com.mapkc.nsfw.util.logging.ESLogger;
import com.mapkc.nsfw.util.logging.Loggers;

import java.io.IOException;
import java.util.Map;

public class PrintOnlyFormHandler implements FormHandler {
    final static ESLogger log = Loggers.getLogger(PrintOnlyFormHandler.class);

    @Override
    public Map<String, String> load(RenderContext rc, String id, FormModel model)
            throws IOException {
        log.info("Load values for id:{} ", id);
        return null;
    }

    @Override
    public boolean update(FormModel model, String id, RenderContext rc,
                          Map<String, String> values) throws IOException {

        log.info("Update values for id:{},values:{} ", id, values);

        return false;
    }

}
