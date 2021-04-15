package com.mapkc.nsfw.model;

import java.util.List;

public interface TreeNode {
    <T extends TreeNode> List<T> getChildren();

    String getId();

    String getAttribute(String name);

}
