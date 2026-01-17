package org.example.springboot3java21demo.exercise.constructor;

import java.util.Arrays;
import java.util.List;

/**
 * xml
 */
public class CommentNode implements Node {
    private final String text;

    public CommentNode(String text) {
        this.text = text;
    }

    public static void main(String[] args) {
        Node root = new ElementNode("school");
        root.add(new ElementNode("classA")
                .add(new TextNode("Tom"))
                .add(new CommentNode("comment...")));
        root.add(new ElementNode("classB")
                .add(new TextNode("Bob"))
                .add(new TextNode("Grace"))
                .add(new CommentNode("comment...")));
        System.out.println(root.toXml());
    }

    @Override
    public Node add(Node node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Node> children() {
        return Arrays.asList();
    }

    @Override
    public String toXml() {
        return "<!-- " + text + " -->";
    }
}
