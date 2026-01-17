package org.example.springboot3java21demo.exercise.sort;

import org.example.springboot3java21demo.exercise.domain.Item;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class ItemSortUtil2<T> {

    /**
     * 检测并返回图中的环（非递归方式）
     *
     * @param itemMap 邻接表
     * @return 包含环的子图
     */
    private Map<T, List<T>> getCircle(Map<T, List<T>> itemMap) {
        Set<T> visited = new HashSet<>();
        Set<T> recStack = new HashSet<>();
        Map<T, List<T>> circleMap = new HashMap<>();

        for (T node : itemMap.keySet()) {
            if (!visited.contains(node)) {
                if (detectCycle(node, itemMap, visited, recStack, circleMap)) {
                    return circleMap;
                }
            }
        }
        return circleMap;
    }

    /**
     * DFS检测环
     */
    private boolean detectCycle(T node, Map<T, List<T>> itemMap, Set<T> visited, Set<T> recStack, Map<T, List<T>> circleMap) {
        if (recStack.contains(node)) {
            circleMap.put(node, new ArrayList<>(itemMap.get(node)));
            return true;
        }
        if (visited.contains(node)) {
            return false;
        }

        visited.add(node);
        recStack.add(node);

        List<T> neighbors = itemMap.get(node);
        if (neighbors != null) {
            for (T neighbor : neighbors) {
                if (detectCycle(neighbor, itemMap, visited, recStack, circleMap)) {
                    if (!circleMap.containsKey(neighbor)) {
                        circleMap.put(neighbor, new ArrayList<>(itemMap.get(neighbor)));
                    }
                    return true;
                }
            }
        }

        recStack.remove(node);
        return false;
    }

    /**
     * 移除节点并更新邻接表（不修改原始数据）
     *
     * @param itemVO  要移除的节点
     * @param itemMap 原始邻接表
     * @return 更新后的邻接表
     */
    private Map<T, List<T>> getLeftItemMap(T itemVO, Map<T, List<T>> itemMap) {
        Map<T, List<T>> newItemMap = new HashMap<>();
        for (Map.Entry<T, List<T>> entry : itemMap.entrySet()) {
            if (!entry.getKey().equals(itemVO)) {
                List<T> newList = new ArrayList<>(entry.getValue());
                newList.remove(itemVO);
                newItemMap.put(entry.getKey(), newList);
            }
        }
        return newItemMap;
    }

    /**
     * 拓扑排序
     *
     * @param itemMap 邻接表
     * @return 排序结果
     */
    public LinkedList<T> topologicalSort(Map<T, List<T>> itemMap) {
        LinkedList<T> topSeq = new LinkedList<>();
        Map<T, Integer> inDegree = new HashMap<>();
        Queue<T> zeroInDegreeQueue = new LinkedList<>();

        // 初始化入度表
        for (T node : itemMap.keySet()) {
            inDegree.putIfAbsent(node, 0);
            List<T> neighbors = itemMap.get(node);
            if (neighbors != null) {
                for (T neighbor : neighbors) {
                    inDegree.put(neighbor, inDegree.getOrDefault(neighbor, 0) + 1);
                }
            }
        }

        // 将入度为0的节点加入队列
        for (Map.Entry<T, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                zeroInDegreeQueue.offer(entry.getKey());
            }
        }

        // 拓扑排序
        while (!zeroInDegreeQueue.isEmpty()) {
            T current = zeroInDegreeQueue.poll();
            topSeq.addLast(current);

            List<T> neighbors = itemMap.get(current);
            if (neighbors != null) {
                for (T neighbor : neighbors) {
                    int newInDegree = inDegree.get(neighbor) - 1;
                    inDegree.put(neighbor, newInDegree);
                    if (newInDegree == 0) {
                        zeroInDegreeQueue.offer(neighbor);
                    }
                }
            }
        }

        // 检查是否存在环
        if (topSeq.size() != itemMap.size()) {
            Map<T, List<T>> circleMap = this.getCircle(itemMap);
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (T item : circleMap.keySet()) {
                if (!first) {
                    sb.append(", ");
                }
                sb.append("[").append(item.toString()).append("]");
                first = false;
            }
            throw new RuntimeException(sb.toString() + "项目存在环状相互依赖关系！请修改项目。");
        }

        return topSeq;
    }

    /**
     * {@link ItemSortUtil2#topologicalSort ItemSortUtil.topologicalSort}
     *
     * @param args
     */
    public static void main(String[] args) {
        Map<Item, List<Item>> adjList = ItemSortUtil2.buildAdjList();
        LinkedList<Item> items = new ItemSortUtil2<Item>().topologicalSort(adjList);
        for (int i = 0; i < items.size(); i++) {
            items.get(i).setSeq(i + 1);
        }
        log.error(items.toString());
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
        depList6.add(item9);
        adjList.put(item6, depList6);
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
