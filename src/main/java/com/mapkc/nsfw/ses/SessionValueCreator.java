package com.mapkc.nsfw.ses;

import com.mapkc.nsfw.model.RenderContext;

/**
 * 懒加载Session中的值，用到了自动创建并放到Session中
 * Created by chy on 14-7-16.
 */
public interface SessionValueCreator {

    Object initSessionValue(String key, RenderContext rc);
}
