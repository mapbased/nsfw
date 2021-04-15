package com.mapkc.nsfw.query;

import com.mapkc.nsfw.model.RenderContext;
import com.mapkc.nsfw.model.Renderable;

/**
 * 根据一个字段，动态生成Query Definition
 * <p>
 * map的value保存配置项，应该配置下列信息：
 * <ul>
 * <li>是否group：如果不存在valuelist，则一定group，如果存在，则可以group也可以不group</li>
 * <li>Group sql：如果group，group的sql可以由用户填写，也可以自动生成，如果用户不填写，则尝试尝试生成</li>
 * <li>ScreenName</li>
 * </ul>
 */
public class VQConfig {

    Renderable groupSql;

    public boolean isGroup() {
        return this.groupSql != null;
    }

    public String getGroupSql(RenderContext rc) {
        if (this.groupSql == null) {
            return null;
        }
        return this.groupSql.getRenderValue(rc);
    }


}
