package org.example.springboot3java21demo.entity;

import java.io.Serializable;
import java.util.*;

/**
 * <p>MapList维护了一个key对一个列表的映射关系.
 * <p>如果列表不存在,则创建列表,否则,每次放入一个键值对时,会将值放在该键对应的列表中.
 */
public class MapList<K, V> implements Serializable {

    private static final long serialVersionUID = 7049807504122075199L;

    /**
     * 存放key和列表映射关系的数据集合
     */
    private final Map<K, List<V>> map = new HashMap<>();

    /**
     * 是否包含当前键
     *
     * @param key 键
     * @return 当前MapList包含此键时，返回true
     */
    public boolean containsKey(K key) {
        return this.map.containsKey(key);
    }

    /**
     * 根据键获取列表
     *
     * @param key 键
     * @return 键对应的列表
     */
    public List<V> get(K key) {
        return this.map.get(key);
    }


    /**
     * 根据键移除值
     *
     * @param key 键
     * @return 键对应的列表
     */
    public List<V> remove(K key) {
        return this.map.remove(key);
    }

    /**
     * 获取键的集合
     *
     * @return 键的集合
     */
    public Set<K> keySet() {
        return this.map.keySet();
    }

    /**
     * 获取当前MapList的视图，用来快速访问里面存储的元素
     *
     * @return 当前MapList的视图
     */
    public Set<Map.Entry<K, List<V>>> entrySet() {
        return this.map.entrySet();
    }

    /**
     * 加入一个键值对。当前键不存在时，会自动创建列表。否则，将值加入到对应的列表中
     *
     * @param key   键
     * @param value 值
     */
    public void put(K key, V value) {
        List<V> l = this.map.computeIfAbsent(key, k -> new ArrayList<>());
        l.add(value);
    }

    /**
     * 将一个值列表全部加入到键值对中
     *
     * @param key       键
     * @param valueList 值的列表
     */
    public void putAll(K key, List<V> valueList) {
        List<V> l = this.map.computeIfAbsent(key, k -> new ArrayList<>());
        l.addAll(valueList);
    }

    /**
     * 得到键的数量
     *
     * @return 当前MapList的大小
     */
    public int size() {
        return this.map.size();
    }

    /**
     * 转化为原始的Map对象
     *
     * @return JDK基本数据结构形式
     */
    public Map<K, List<V>> toMap() {
        return this.map;
    }

    public List<V> values() {
        Collection<List<V>> collections = this.map.values();
        if (collections.isEmpty()) {
            return Collections.emptyList();
        }
        List<V> resultList = new ArrayList<>();
        for (List<V> list : collections) {
            resultList.addAll(list);
        }
        return resultList;
    }
}
