package com.mapkc.nsfw.model;

/**
 * 没个页面或Fragment均可有对应的ActionHandler,这里是写Java代码的地方。
 * 可以添加任意的方法，供页面调用.@{rc.fh.methodName}
 */
public interface ActionHandler {
    /**
     * 是否已经过滤掉正常的httppage，如果返回true，正常的页面渲染被打断，不再进行后续操作
     *
     * @param rc
     * @return
     */
    boolean filterAction(RenderContext rc);

}
