package org.example.springboot3java21demo.exercise.constructor;

import java.util.Arrays;
import java.util.List;

/**
 * 
 */
public class TextNode implements Node {
    private final String text;

    public TextNode(String text) {
        this.text = text;
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
        return text;
    }
}
