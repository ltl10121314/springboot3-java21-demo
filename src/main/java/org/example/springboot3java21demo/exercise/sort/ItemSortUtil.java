package org.example.springboot3java21demo.exercise.sort;

import org.example.springboot3java21demo.exercise.domain.Item;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 对项目项目排序的帮助类
 * 对确定好相互依赖关系的项目进行排序。（按照有向图的拓扑排序原理）
 * <li>a) 从项目VO中找到不依赖其他项目的项目记入顺序队列中；
 * <li>b) 从项目中删除所有项目对该项目的依赖关系；
 * <li>c) 重复以上两步，直至全部项目都排好序，拓扑排序顺利完成。否则，若剩有还依赖于其他项目的项目，说明图中有环，拓扑排序不能进行。
 */
@Slf4j
public class ItemSortUtil<T> {

    /**
     * 得到项目的环， 便于显示
     *
     * @param itemMap
     * @return
     */
    private Map<T, List<T>> getCircle(Map<T, List<T>> itemMap) {
        Map<T, List<T>> leftItemMap = new HashMap<>();
        Iterator<T> it = itemMap.keySet().iterator();
        List<T> usefulValue = new LinkedList<T>();
        while (it.hasNext()) {
            T keyItem = it.next();
            List<T> values = itemMap.get(keyItem);
            for (T value : values) {
                if (!usefulValue.contains(value)) {
                    usefulValue.add(value);
                }
            }
        }

        for (T useful : usefulValue) {
            if (itemMap.containsKey(useful)) {
                leftItemMap.put(useful, itemMap.get(useful));
            }
        }

        if (itemMap.size() != leftItemMap.size()) {
            return this.getCircle(leftItemMap);
        } else {
            return leftItemMap;
        }
    }

    /**
     * 对于 v的每个邻接点入度减1
     *
     * @param itemVO
     * @param itemMap
     * @return
     */
    private Map<T, List<T>> getLeftItemMap(T itemVO, Map<T, List<T>> itemMap) {
        itemMap.remove(itemVO);
        if (!itemMap.isEmpty()) {
            Iterator<T> it = itemMap.keySet().iterator();
            while (it.hasNext()) {
                List<T> list = itemMap.get(it.next());
                if (list != null && !list.isEmpty() && list.contains(itemVO)) {
                    list.remove(itemVO);
                }
            }
        }

        return itemMap;
    }

    /**
     * @param itemMap 邻接表
     * @return 排序结果
     */
    public LinkedList<T> topologicalSort(Map<T, List<T>> itemMap) {
        // 拓扑序列
        LinkedList<T> topSeq = new LinkedList<T>();
        LinkedList<T> s = new LinkedList<T>();
        Iterator<T> it = itemMap.keySet().iterator();
        int numberItems = itemMap.size();
        // 初始化顶点集应用信息
        // 生成拓扑序列
        while (it.hasNext()) {
            T keyItem = it.next();
            List<T> dependItems = itemMap.get(keyItem);
            if (dependItems == null || dependItems.isEmpty()) {
                s.addLast(keyItem);
            }
        }
        while (!s.isEmpty()) {
            Map<T, List<T>> adjMap = null;
            // 入度为 0 的顶点入栈
            while (!s.isEmpty()) {
                T v = s.removeFirst();
                topSeq.addLast(v);
                // 对于 v的每个邻接点入度减1
                adjMap = this.getLeftItemMap(v, itemMap);
            }
            Iterator<T> adjIt = adjMap.keySet().iterator();
            while (adjIt.hasNext()) {
                T adjKeyItem = adjIt.next();
                List<T> dependItems = adjMap.get(adjKeyItem);
                if (dependItems == null || dependItems.isEmpty()) {
                    s.addFirst(adjKeyItem);
                }
            }
        }
        if (topSeq.size() < numberItems) {
            itemMap = this.getCircle(itemMap);
            Set<T> circularItems = itemMap.keySet();
            String strIn = null;
            for (T item : circularItems) {
                if (strIn == null) {
                    strIn = "[" + item.toString() + "]";
                } else {
                    strIn = strIn + ", " + "[" + item.toString() + "]";
                }
            }
            throw new RuntimeException(strIn + "项目存在环状相互依赖关系！请修改项目。");
        } else {
            return topSeq;
        }
    }

    /**
     * {@link ItemSortUtil#topologicalSort ItemSortUtil.topologicalSort}
     *
     * @param args
     */
    public static void main(String[] args) {
        Map<Item, List<Item>> adjList = ItemSortUtil.buildAdjList();
        LinkedList<Item> items = new ItemSortUtil<Item>().topologicalSort(adjList);
        for (int i = 0; i < items.size(); i++) {
            items.get(i).setSeq(i + 1);
        }
        log.error(items.toString());
        List<String> list = Collections.synchronizedList(new LinkedList<String>());

    }

    private static Map<Item, List<Item>> buildAdjList() {
        Item item1 = Item.builder().code("f_n_1").name("应发工资1").build();
        Item item2 = Item.builder().code("f_n_2").name("应发工资2").build();
        Item item3 = Item.builder().code("f_n_3").name("应发工资3").build();
        Item item4 = Item.builder().code("f_n_4").name("应发工资4").build();
        Item item5 = Item.builder().code("f_n_5").name("应发工资5").build();
        Item item6 = Item.builder().code("f_n_6").name("应发工资6").build();
        Item item7 = Item.builder().code("f_n_7").name("应发工资7").build();
        Item item8 = Item.builder().code("f_n_8").name("应发工资8").build();
        Item item9 = Item.builder().code("f_n_9").name("应发工资9").build();
        Item item10 = Item.builder().code("f_n_10").name("应发工资10").build();
        Map<Item, List<Item>> adjList = new HashMap<>();
        adjList.put(item1, null);
        adjList.put(item2, null);
        adjList.put(item3, null);
        List<Item> depList4 = new ArrayList<>();
        depList4.add(item5);
        depList4.add(item7);
        adjList.put(item4, depList4);
        List<Item> depList5 = new ArrayList<>();
        depList5.add(item1);
        depList5.add(item2);
        depList5.add(item3);
//        depList5.add(item8);
        adjList.put(item5, depList5);
        List<Item> depList6 = new ArrayList<>();
        adjList.put(item6, depList6);
        depList6.add(item9);
        List<Item> depList7 = new ArrayList<>();
        depList7.add(item6);
        adjList.put(item7, null);
        List<Item> depList8 = new ArrayList<>();
        depList8.add(item4);
        adjList.put(item8, depList8);
        List<Item> depList9 = new ArrayList<>();
        depList9.add(item8);
        adjList.put(item9, depList9);
        adjList.put(item10, null);
        return adjList;
    }

}

