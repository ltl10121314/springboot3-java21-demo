package org.example.springboot3java21demo.exercise.test;

import org.example.springboot3java21demo.exercise.domain.Item;
import org.example.springboot3java21demo.exercise.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;
import org.springframework.lang.NonNull;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.regex.Pattern.compile;

/**
 * 
 */
@Slf4j
public class Task2Test {

    public static ThreadLocal<String> isExistDynamicAuthToken = new ThreadLocal<>();

    public static void main(String[] args) throws InterruptedException {
//        String temp = "nmme2n2z-dataenc"; // 三峡
//        String temp = "0000M03FWZ035BYADG0000-dataenc"; // 测试:20240821超级人力测试
        String temp = "j80zc0cv-dataenc"; // new20:240322薪酬计算
        System.out.println("hashCode=" + temp.hashCode());
        int hash = temp.hashCode();
        String sss;
        if (hash < 0) {
            sss = (hash + "").replaceFirst("-", "");
            System.out.println("-sss=" + sss);
        } else {
            sss = hash + "";
            System.out.println("sss" + sss);
        }
    }


    @Test
    public void test7() {
        Function<User, String> keyFunc = User::getName;
        BiConsumer<Item, User> consumer = (item, user) -> {
            item.setCode(user.getId());
            item.setName(user.getName());
        };
        User user1 = new User();
        user1.setId("1");
        user1.setCode("a");
        user1.setName("爱");
        Item item1 = new Item();
        log.error(user1.toString());
        log.error(item1.toString());
        consumer.accept(item1, user1);
        log.error(user1.toString());
        log.error(item1.toString());
        log.error(keyFunc.apply(user1));
        BiFunction<Item, User, String> biFunction = (item2, user) -> {
            String code = item2.getCode();
            String name = user.getName();
            return code + "_" + name;
        };
        String result = biFunction.apply(item1, user1);
        log.error(result);
    }

    public static String getNane() {
        return "hello";
    }

    private static void testListSort() throws InterruptedException {
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
    }

    public static boolean isNeedParser(@NonNull String formulastr) {
        Pattern numberPattern = compile("[0-9]*");
        boolean isneed = false;
        if (formulastr == null) {
            return false;
        } else {
            formulastr = formulastr.replaceAll("\\.", "");
            isneed = !numberPattern.matcher(formulastr).matches();
            return isneed;
        }
    }

    private static String encrypt3ToMD5(String password) {
        String md5 = "";
        md5 = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
        return md5;
    }

    public static boolean isDate(String date) {
        if (date == null) {
            return false;
        }
        try {
            Date date1 = DateUtils.parseDate(date, "yyyy-MM-dd");
            System.out.println(date1);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    @Test
    public void test1() {
        List<String> list1 = new ArrayList<>();
        list1.add("a");
        list1.add("b");
        List<String> list2 = new ArrayList<>();
        list2.add("a");
        list2.add("b");
        List<String> list3 = new ArrayList<>(list1);
        List<String> list4 = new ArrayList<>(list2);
        list3.removeAll(list2);
        list4.removeAll(list1);
        System.out.println(list3);
        System.out.println(list4);
    }

    @Test
    public void test2() {
        List<String> list = new ArrayList<>();
        list.add("2022-10-01&2022-10-20");
        list.add("2022-10-26&");
        list.add("2022-10-21&2022-10-25");
        System.out.println(list);
        Collections.sort(list);
        System.out.println(list);
    }

    private Integer getSalaryLength(String number) {
        String[] split = number.split("\\.");
        if (split.length == 1) {
            return 0;
        } else {
            return split[1].length();
        }
    }

    @Test
    public void test3() {
        String input = "9999-14"; // 输入的字符串
        String patternStr = "\\d{4}[-/]\\d{2}"; // 正则表达式
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(input);

        if (matcher.matches()) {
            System.out.println("匹配成功");
            // 可以在这里进行进一步的处理，例如提取年份和月份
        } else {
            System.out.println("匹配失败");
        }
    }

    @Test
    public void test4() {
        List<User> list = new ArrayList<>();
//        User user = new User();
//        user.setId("1");
//        user.setCode("a");
//        User user2 = new User();
//        user2.setId("1");
//        user2.setCode("a");
//        list.add(user);
//        list.add(user2);

        List<String> waSchemeAuthIds = new ArrayList<>();
        // 过滤去重
        list = list.stream().filter(
                v -> {
                    boolean flag = !waSchemeAuthIds.contains(v.getId());
                    waSchemeAuthIds.add(v.getId());
                    return flag;
                }
        ).collect(Collectors.toList());
        list = list.stream().sorted(Comparator.comparing(User::getOrderId)).collect(Collectors.toList());
        System.out.println(list);
    }

    private Integer test77(Integer num) {
        return num;
    }


    @Test
    public void test5() {
        List<Map<String, Object>> details = new ArrayList<>();
        Map<String, Object> map1 = new HashMap<>();
        Map<String, Object> map2 = new HashMap<>();
        String dateStr1 = "2021-11-01";
        String dateStr2 = "2021-11-02";
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date1 = null;
        Date date2 = null;
        try {
            date1 = formatter.parse(dateStr1);
            log.error(date1.toString());
            map1.put("payDate", date1);
            date2 = formatter.parse(dateStr2);
            log.error(date2.toString());
            map2.put("payDate", date2);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        log.error("before:{}", date1.before(date2));
        details.add(map1);
        details.add(map2);
        details.sort(Comparator.comparing(k -> (Date) k.get("payDate")));
        log.error(details.toString());
        Collections.reverse(details);
        log.error(details.toString());
    }


    @Test
    public void test6() {
        List<String> list1 = new ArrayList<>();
        List<String> list2 = new ArrayList<>();
        list1.add("A");
        list1.add("B");
        list1.add("C");
        list2.add("B");
        String join = StringUtils.join(list1, ",");
        System.out.println("薪资核算的定调薪档案中引用了此任职信息,JOB_ID:{0}".replace("{0}", join));
        try {
            int i = 1 / 0;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            System.out.println(12);
        }
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
}
