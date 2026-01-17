package org.example.springboot3java21demo.constant;

import com.google.common.base.Joiner;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static org.example.springboot3java21demo.constant.ProjectInConstant.*;

/**
 * 所得项目枚举
 */
@Getter
public enum WaSchemeProjectInEnum {

    /**
     * 工资薪金所得(居民)
     */
    PROJECT_IN_SALARY(0, "PROJECT_IN_SALARY", "工资薪金所得（居民）", UN_TRANS_PROJECTIN_SALARY, 1),
    PROJECT_IN_LABOUR(1, "nonresidentnormalsalary", "劳务报酬所得（居民）", UN_TRANS_PROJECTIN_LABOUR, 2),
    PROJECT_IN_LABOR_BROKER(17, "PROJECT_IN_LABOR_BROKER", "劳务报酬（保险营销员/证券经纪人/其他连续劳务）居民", UN_PROJECTIN_LABOR_BROKER, 3),
    PROJECT_IN_CONTRACT_TERMINATION(16, "PROJECT_IN_CONTRACT_TERMINATION", "解除劳动合同（居民）", UN_PROJECTIN_CONTRACT_TERMINATION, 4),
    PROJECT_IN_YEAR_END(4, "PROJECT_IN_YEAR_END", "全年一次性奖金所得", UN_TRANS_PROJECTIN_YEAREND, 5),
    PROJECT_IN_SALARY_FOREIGN(5, "PROJECT_IN_SALARY_FOREIGN", "工资薪金所得（非居民）", UN_TRANS_PROJECTIN_SALARY_FOREIGN, 6),
    PROJECT_IN_LABOUR_FOREIGN(2, "PROJECT_IN_LABOUR_FOREIGN", "劳务报酬所得（非居民）", UN_TRANS_PROJECTIN_LABOUR_FOREIGN, 7);

    private final Integer code;
    private final String name;
    private String desc;
    private String msg;

    /**
     * 优先级: 数字越小,优先级越高
     */
    private final Integer priority;


    /**
     * 工资薪金所得（居民）互斥所得项目
     */
    public static final List<Integer> SALARY_EXCLUSION = new ArrayList<>(
            Arrays.asList(PROJECT_IN_LABOUR.getCode()
                    , PROJECT_IN_LABOR_BROKER.getCode()
                    , PROJECT_IN_SALARY_FOREIGN.getCode()
                    , PROJECT_IN_LABOUR_FOREIGN.getCode()
            )
    );

    /**
     * 劳务报酬所得（居民）互斥所得项目
     */
    public static final List<Integer> LABOUR_EXCLUSION = new ArrayList<>(
            Arrays.asList(PROJECT_IN_SALARY.getCode()
                    , PROJECT_IN_LABOR_BROKER.getCode()
                    , PROJECT_IN_SALARY_FOREIGN.getCode()
                    , PROJECT_IN_LABOUR_FOREIGN.getCode()
            )
    );

    /**
     * 劳务报酬（保险营销员/证券经纪人/其他连续劳务）(居民) 互斥所得项目
     */
    public static final List<Integer> LABOR_BROKER_EXCLUSION = new ArrayList<>(
            Arrays.asList(PROJECT_IN_SALARY.getCode()
                    , PROJECT_IN_LABOUR.getCode()
                    , PROJECT_IN_SALARY_FOREIGN.getCode()
                    , PROJECT_IN_LABOUR_FOREIGN.getCode()
            )
    );

    /**
     * 解除劳动合同（居民）互斥所得项目
     */
    public static final List<Integer> CONTRACT_TERMINATION_EXCLUSION = new ArrayList<>(
            Arrays.asList(PROJECT_IN_SALARY_FOREIGN.getCode()
                    , PROJECT_IN_LABOUR_FOREIGN.getCode()
            )
    );

    /**
     * 全年一次性奖金所得 互斥所得项目
     */
    public static final List<Integer> YEAR_END_EXCLUSION = new ArrayList<>(
            Arrays.asList(PROJECT_IN_SALARY_FOREIGN.getCode()
                    , PROJECT_IN_LABOUR_FOREIGN.getCode()
            )
    );

    /**
     * 工资薪金所得（非居民）互斥所得项目
     */
    public static final List<Integer> SALARY_FOREIGN_EXCLUSION = new ArrayList<>(
            Arrays.asList(PROJECT_IN_SALARY.getCode()
                    , PROJECT_IN_LABOUR.getCode()
                    , PROJECT_IN_LABOR_BROKER.getCode()
                    , PROJECT_IN_CONTRACT_TERMINATION.getCode()
                    , PROJECT_IN_YEAR_END.getCode()
                    , PROJECT_IN_LABOUR_FOREIGN.getCode()
            )
    );

    /**
     * 劳务报酬所得（非居民）互斥所得项目
     */
    public static final List<Integer> LABOUR_FOREIGN_EXCLUSION = new ArrayList<>(
            Arrays.asList(PROJECT_IN_SALARY.getCode()
                    , PROJECT_IN_LABOUR.getCode()
                    , PROJECT_IN_LABOR_BROKER.getCode()
                    , PROJECT_IN_CONTRACT_TERMINATION.getCode()
                    , PROJECT_IN_YEAR_END.getCode()
                    , PROJECT_IN_SALARY_FOREIGN.getCode()
            )
    );

    WaSchemeProjectInEnum(Integer code, String name, String desc, String msg, Integer priority) {
        this.code = code;
        this.name = name;
        this.desc = desc;
        this.msg = msg;
        this.priority = priority;
    }

    public static WaSchemeProjectInEnum codeOf(Integer code) {
        for (WaSchemeProjectInEnum e : WaSchemeProjectInEnum.values()) {
            if (Objects.equals(e.getCode(), code)) {
                return e;
            }
        }
        return null;
    }

    public static Integer getPriority(List<Integer> codes) {
        if (codes == null || codes.isEmpty()) {
            throw new RuntimeException("params is empty.");
        }
        List<WaSchemeProjectInEnum> result = new ArrayList<>();
        for (Integer code : codes) {
            WaSchemeProjectInEnum projectInEnum = codeOf(code);
            result.add(projectInEnum);
        }
        List<WaSchemeProjectInEnum> enums = result.stream().sorted(Comparator.comparingInt(WaSchemeProjectInEnum::getPriority)).collect(Collectors.toList());
        return enums.get(0).getCode();
    }

    public static String getDescOfCode(String codeStr) {
        String descStr = "";
        if (codeStr == null || codeStr.isEmpty()) {
            return descStr;
        }
        String[] codeArr = codeStr.split(",");
        List<WaSchemeProjectInEnum> enumList = new LinkedList<>();
        for (String code : codeArr) {
            WaSchemeProjectInEnum projectInEnum = codeOf(Integer.valueOf(code));
            if (projectInEnum != null) {
                enumList.add(projectInEnum);
            }
        }
        if (!CollectionUtils.isEmpty(enumList)) {
            descStr = enumList.stream().map(WaSchemeProjectInEnum::getDesc).collect(Collectors.joining(","));
        }
        return descStr;
    }

    public static void main(String[] args) {
        List<Integer> projectInList = new ArrayList<>();
        projectInList.add(0);
        projectInList.add(4);
        projectInList.add(17);
        projectInList.add(1);
        projectInList.add(2);
        projectInList.add(5);
        projectInList.add(16);
        Integer highestPriority = getPriority(projectInList);
        System.out.println(highestPriority);
        switch (highestPriority) {
            case IWaSchemeConstant.PROJECTIN_SALARY:
                projectInList.removeAll(WaSchemeProjectInEnum.SALARY_EXCLUSION);
                break;
            case IWaSchemeConstant.PROJECTIN_LABOUR:
                projectInList.removeAll(WaSchemeProjectInEnum.LABOUR_EXCLUSION);
                break;
            case IWaSchemeConstant.PROJECTIN_CONTRACT_TERMINATION:
                projectInList.removeAll(WaSchemeProjectInEnum.LABOR_BROKER_EXCLUSION);
                break;
            case IWaSchemeConstant.PROJECTIN_LABOR_BROKER:
                projectInList.removeAll(WaSchemeProjectInEnum.CONTRACT_TERMINATION_EXCLUSION);
                break;
            case IWaSchemeConstant.PROJECTIN_YEAREND:
                projectInList.removeAll(WaSchemeProjectInEnum.YEAR_END_EXCLUSION);
                break;
            case IWaSchemeConstant.PROJECTIN_SALARY_FOREIGN:
                projectInList.removeAll(WaSchemeProjectInEnum.SALARY_FOREIGN_EXCLUSION);
                break;
            case IWaSchemeConstant.PROJECTIN_LABOUR_FOREIGN:
                projectInList.removeAll(WaSchemeProjectInEnum.LABOUR_FOREIGN_EXCLUSION);
                break;
            default:
                throw new RuntimeException("所得项目不能为空");
        }
        String str = Joiner.on(",").join(projectInList);
        System.out.println(str);
        String descOfCode = getDescOfCode(str);
        System.out.println(descOfCode);
    }
}
