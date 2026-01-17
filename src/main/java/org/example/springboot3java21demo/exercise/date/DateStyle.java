package org.example.springboot3java21demo.exercise.date;

public enum DateStyle {
    YYYY("yyyy", false),
    MM("MM", false),
    YYYY_MM("yyyy-MM", false),
    YYYYMM("yyyyMM", false),
    YYYY_MM_DD("yyyy-MM-dd", false),
    YYYY_MM_DD_HH_MM("yyyy-MM-dd HH:mm", false),
    YYYY_MM_DD_HH_MM_SS("yyyy-MM-dd HH:mm:ss", false),
    YYYYMMDDHHMMSS("yyyyMMddHHmmss", false),
    YYYY_MM_EN("yyyy/MM", false),
    YYYY_MM_DD_EN("yyyy/MM/dd", false),
    YYYY_MM_DD_HH_MM_EN("yyyy/MM/dd HH:mm", false),
    YYYY_MM_DD_HH_MM_SS_EN("yyyy/MM/dd HH:mm:ss", false),

    YYYY_MM_CN("ficloud.001120" /* "yyyy年MM月" */, false),
    YYYY_MM_DD_CN("P_YS_HR_HRXC_0000110612" /* "yyyy年MM月dd日" */, false),
    YYYY_MM_DD_HH_MM_CN("P_YS_HR_HRXC_0000110234" /* "yyyy年MM月dd日 HH:mm" */, false),
    YYYY_MM_DD_HH_MM_SS_CN("P_YS_HR_HRXC_0000110568" /* "yyyy年MM月dd日 HH:mm:ss" */, false),

    HH_MM("HH:mm", true),
    HH_MM_SS("HH:mm:ss", true),

    MM_DD("MM-dd", true),
    MM_DD_HH_MM("MM-dd HH:mm", true),
    MM_DD_HH_MM_SS("MM-dd HH:mm:ss", true),

    MM_DD_EN("MM/dd", true),
    MM_DD_HH_MM_EN("MM/dd HH:mm", true),
    MM_DD_HH_MM_SS_EN("MM/dd HH:mm:ss", true),

    MM_DD_CN("P_YS_HR_HRJQ_0000030956" /* "MM月dd日" */, true),
    MM_DD_HH_MM_CN("P_YS_HR_HRXC_0000110861" /* "MM月dd日 HH:mm" */, true),
    MM_DD_HH_MM_SS_CN("P_YS_HR_HRXC_0000110787" /* "MM月dd日 HH:mm:ss" */, true);

    private final String value;

    private final boolean isShowOnly;

    DateStyle(String value, boolean isShowOnly) {
        this.value = value;
        this.isShowOnly = isShowOnly;
    }

    public String getValue() {
        if (Character.isDigit(value.charAt(value.length() - 1))) {
        }
        return value;
    }

    public boolean isShowOnly() {
        return isShowOnly;
    }
}
