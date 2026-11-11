package org.example.practice.jdk.core.constructor;

import java.util.List;

/**
 * 
 */
public interface Node {
    /**
     * 添加一个节点为子节点
     *
     * @param node
     * @return
     */
    Node add(Node node);

    /**
     * 获取子节点
     *
     * @return
     */
    List<Node> children();

    /**
     * 输出为XML
     *
     * @return
     */
    String toXml();
}
