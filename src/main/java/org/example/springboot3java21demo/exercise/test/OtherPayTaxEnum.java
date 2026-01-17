package org.example.springboot3java21demo.exercise.test;

/**
 * 
 */

public enum OtherPayTaxEnum {

    TAX_AFTER_INCOME("税后工资", "f_n_201"),
    TAX_AFTER_INCOME_LAST_TOTAL("上期累计税后工资", "f_n_202");
    // 成员变量
    private String name;
    private String index;

    // 构造方法
    OtherPayTaxEnum(String name, String index) {
        this.name = name;
        this.index = index;
    }

    public static String getName(String index) {
        for (OtherPayTaxEnum c : OtherPayTaxEnum.values()) {
            if (c.getIndex().equals(index)) {
                return c.name;
            }
        }
        return null;
    }

    // get set 方法
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }
}
