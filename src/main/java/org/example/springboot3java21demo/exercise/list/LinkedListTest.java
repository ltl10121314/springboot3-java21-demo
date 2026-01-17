package org.example.springboot3java21demo.exercise.list;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 */
public class LinkedListTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(LinkedListTest.class);

    public static void main(String[] args) {
        List<String> list = new ArrayList<>();
        list.add("1");
        list.add("2");
        List<String> list2 = new ArrayList<>();
        list2.add("3");
        list2.add("4");
        List<String> list3 = new ArrayList<>();
        list3.add("5");
        list3.add("6");
        List<List<String>> strings = new LinkedList<>();
        strings.add(0, list);
        strings.add(0, list2);
        strings.add(1, list2);
        strings.add(1, list3);
        strings.add(2, list);
        strings.add(2, list3);
        LOGGER.info(strings.toString());
        for (int i = 0; i < strings.size(); i++) {
            LOGGER.info(strings.get(i).toString());
        }
    }
}
