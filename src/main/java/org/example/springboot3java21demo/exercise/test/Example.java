package org.example.springboot3java21demo.exercise.test;

import org.example.springboot3java21demo.exercise.domain.User;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 */
@Slf4j
public class Example {
    private static final Logger LOGGER = LoggerFactory.getLogger(Example.class);

    private static NavigableMap<Integer, Integer> rankingMap = Maps.newTreeMap();
    private static Map<String, User> map = new HashMap<>();
    private static Map<String, User> map3 = new HashMap<>();
    private static Map<String, String> map2 = new HashMap<>();

    public static void main(String[] args) {

    }

    private void traversalCategory(Category next, List<String> categoryIdList, Iterator<Category> iterator) {
        if (next.getChildren() == null) {
            boolean contains = categoryIdList.contains(next.getId());
            if (!contains) {
                iterator.next().setChildren(null);
                return;
            }
        }
        Iterator<Category> iteratorSub = next.getChildren().iterator();
        while (iteratorSub.hasNext()) {
            Category nextSub = iterator.next();
            traversalCategory(nextSub, categoryIdList, iteratorSub);
        }
    }

    private List<Category> dealCategoryNode(List<Category> categoryList) {
        Map<String, Category> categoryMap = categoryList.stream().collect(Collectors.toMap(Category::getId, Function.identity()));
        for (Category category : categoryList) {
            Category parentCategory = categoryMap.get(category.getParentid()) == null ? null : categoryMap.get(category.getParentid());
            if (parentCategory == null) {
                continue;
            }
            List<Category> children = parentCategory.getChildren();
            if (children == null) {
                children = new ArrayList<>();
            }
            children.add(category);
            parentCategory.setChildren(children);
        }
        categoryList = new ArrayList<>();
        for (String k : categoryMap.keySet()) {
            Category category = categoryMap.get(k);
        }
        return categoryList;
    }

    private List<Category> creatList() {
        List<Category> list = new ArrayList<>();
        Category category1 = new Category("1", "目录1", null, null);
        Category category2 = new Category("2", "目录2", null, null);
        Category category3 = new Category("3", "目录3", null, null);
        Category category4 = new Category("4", "目录4", null, null);
        Category category5 = new Category("5", "目录5", null, null);
        Category category11 = new Category("11", "目录11", "1", null);
        Category category21 = new Category("21", "目录21", "2", null);
        Category category31 = new Category("31", "目录31", "3", null);
        Category category41 = new Category("41", "目录41", "4", null);
        Category category51 = new Category("51", "目录51", "5", null);
        Category category111 = new Category("111", "目录111", "11", null);
        Category category112 = new Category("112", "目录112", "11", null);
        Category category113 = new Category("113", "目录113", "11", null);
        Category category1111 = new Category("1111", "目录1111", "111", null);
        Category category211 = new Category("211", "目录211", "21", null);
        Category category212 = new Category("212", "目录212", "21", null);
        Category category2111 = new Category("2111", "目录2111", "211", null);
        list.add(category1);
        list.add(category2);
        list.add(category3);
        list.add(category4);
        list.add(category5);
        list.add(category11);
        list.add(category21);
        list.add(category31);
        list.add(category41);
        list.add(category51);
        list.add(category111);
        list.add(category112);
        list.add(category113);
        list.add(category1111);
        list.add(category211);
        list.add(category212);
        list.add(category2111);
        return list;
    }

    private String createString() {
        String str = "[\n" +
                "  {\n" +
                "    \"type\": \"org\",\n" +
                "    \"scopes\": [\n" +
                "      {\n" +
                "        \"name\": \"二级公司\",\n" +
                "        \"id\": \"2680312687628544\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"type\": \"pcategory\",\n" +
                "    \"scopes\": []\n" +
                "  },\n" +
                "  {\n" +
                "    \"type\": \"jobgrade\",\n" +
                "    \"scopes\": []\n" +
                "  },\n" +
                "  {\n" +
                "    \"type\": \"jobrank\",\n" +
                "    \"scopes\": []\n" +
                "  },\n" +
                "  {\n" +
                "    \"type\": \"staffstate\",\n" +
                "    \"scopes\": []\n" +
                "  },\n" +
                "  {\n" +
                "    \"type\": \"changetype\",\n" +
                "    \"scopes\": []\n" +
                "  },\n" +
                "  {\n" +
                "    \"type\": \"jobtype\",\n" +
                "    \"scopes\": []\n" +
                "  },\n" +
                "  {\n" +
                "    \"type\": \"wagegroup\",\n" +
                "    \"scopes\": [\n" +
                "      {\n" +
                "        \"name\": \"100001\",\n" +
                "        \"id\": \"1451367116933955587\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"name\": \"testGroup31\",\n" +
                "        \"id\": \"1451341080830148611\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"name\": \"wageGroup4\",\n" +
                "        \"id\": \"1456808067389194244\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"type\": \"job\",\n" +
                "    \"scopes\": []\n" +
                "  }\n" +
                "]";
        return str;
    }

    @Test
    public void testListSort() throws InterruptedException {
        List<User> userList = new ArrayList<>();
        User user_1 = new User();
        user_1.setId("1");
        user_1.setName("hahahah");
        user_1.setOrderId(1);
        user_1.setCreationTime(new Date());
        Thread.sleep(1000);
        User user_2 = new User();
        user_2.setId("1");
        user_2.setName("heheheh");
        user_2.setOrderId(2);
        user_2.setCreationTime(new Date());
        Thread.sleep(1000);
        User user_3 = new User();
        user_3.setId("1");
        user_3.setName("xixixix");
        user_3.setCreationTime(new Date());
        userList.add(user_1);
        userList.add(user_2);
        userList.add(user_3);
        // 列表对象排序
        userList = userList.stream().sorted(Comparator.comparing(User::getCreationTime, Comparator.nullsFirst(Date::compareTo)).reversed()).collect(Collectors.toList());
        log.info("userList:{}", userList);
        // 列表对象去重
        List<User> waStaffNumVOS = userList.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(User::getId))), ArrayList::new));
        log.info("waStaffNumVOS:{}", waStaffNumVOS);
        log.info(Thread.currentThread().getStackTrace()[1].getMethodName());
        log.info(user_3.toString());
    }

    static {
        rankingMap.put(1, 1);  //ranking 1 ->1
        rankingMap.put(2, 2); //ranking 2 ->2
        rankingMap.put(3, 3); //ranking 3 -> 3
        rankingMap.put(4, 4); //ranking 4-10 ->4
        rankingMap.put(11, 5); //ranking 11-20 ->5
        rankingMap.put(21, 6); //ranking 21-30 ->6
        rankingMap.put(31, 7); //ranking >= 31 ->7
        User user1 = new User();
        user1.setId("1");
        user1.setName("张三");
        User user2 = new User();
        user2.setId("2");
        user2.setName("王麻子");
        user1.setChildren(user2);
        map.put("1", user1);
        User user3 = new User();
        user3.setId("3");
        user3.setName("李四");
        User user4 = new User();
        user4.setId("4");
        user4.setName("王五");
        user3.setChildren(user4);
        User user5 = new User();
        user5.setId("5");
        user5.setName("李六");
        user4.setChildren(user5);
        map.put("3", user3);
        map3.put("1", user1);
        map3.put("2", user2);
        map3.put("3", user3);
        map3.put("4", user4);
        map3.put("5", user5);
    }
}
