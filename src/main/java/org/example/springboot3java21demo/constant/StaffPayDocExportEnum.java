package org.example.springboot3java21demo.constant;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>发薪档案列表导出,表头枚举
 */
public enum StaffPayDocExportEnum {

    /**
     * 员工编码
     */
    STAFF_CODE(StaffPayDocExportConstant.STAFF_CODE, "员工编码", "staffCode"),

    /**
     * 员工姓名
     */
    STAFF_NAME(StaffPayDocExportConstant.STAFF_NAME, "员工姓名", "staffName"),

    /**
     * 任职组织
     */
    ORG_NAME(StaffPayDocExportConstant.ORG_NAME, "任职组织", "orgName"),

    /**
     * 任职部门
     */
    DEPT_NAME(StaffPayDocExportConstant.DEPT_NAME, "任职部门", "deptName"),

    /**
     * 发薪方案
     */
    WA_SCHEME_NAME(StaffPayDocExportConstant.WA_SCHEME_NAME, "发薪方案", "waSchemeName"),

    /**
     * 开始日期
     */
    BEGIN_DATE(StaffPayDocExportConstant.BEGIN_DATE, "开始日期", "beginDate"),

    /**
     * 结束日期
     */
    END_DATE(StaffPayDocExportConstant.END_DATE, "结束日期", "endDate"),

    /**
     * 财务组织
     */
    TAX_ORG_NAME(StaffPayDocExportConstant.TAX_ORG_NAME, "财务组织", "taxOrgName"),

    /**
     * 财务部门
     */
    TAX_DEPT_NAME(StaffPayDocExportConstant.TAX_DEPT_NAME, "财务部门", "taxDeptName"),

    /**
     * 险种
     */
    INSURANCE_NAME(StaffPayDocExportConstant.INSURANCE_NAME, "险种", "insuranceName"),

    /**
     * 扣税方式
     */
    TAX_TYPE(StaffPayDocExportConstant.TAX_TYPE_NAME, "扣税方式", "taxType"),

    /**
     * 所得项目类型
     */
    MUTI_PROJECT_IN_NAME(StaffPayDocExportConstant.MUTI_PROJECT_IN_NAME, "所得项目类型", "mutiProjectInName"),

    /**
     * 个税申报组织
     */
    PER_TAX_ORG(StaffPayDocExportConstant.PER_TAX_ORG_NAME, "个税申报组织", "perTaxOrg"),

    /**
     * 减免税
     */
    IS_DERATE(StaffPayDocExportConstant.IS_DERATE, "减免税", "isDerate"),

    /**
     * 减免税比例
     */
    DERATEPTG(StaffPayDocExportConstant.DERATEPTG, "减免税比例", "derateptg");

    private final Integer index;
    private final String name;
    private final String code;

    StaffPayDocExportEnum(Integer index, String name, String code) {
        this.index = index;
        this.name = name;
        this.code = code;
    }

    public Integer getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public static List<String> headerName(){
        StaffPayDocExportEnum[] values = StaffPayDocExportEnum.values();
        List<StaffPayDocExportEnum> enums = Arrays.stream(values).sorted(Comparator.comparing(StaffPayDocExportEnum::getIndex)).collect(Collectors.toList());
        return enums.stream().map(StaffPayDocExportEnum::getName).collect(Collectors.toList());
    }

    public static void main(String[] args) {
        List<String> strings = StaffPayDocExportEnum.headerName();
        System.out.println(strings);
    }
}
