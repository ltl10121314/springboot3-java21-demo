package org.example.springboot3java21demo.constant;

public class IWaSchemeConstant {

    public static final int BOOLEAN_FALSE = 0;
    public static final int BOOLEAN_TRUE = 1;

    //多所得项目
    public static final int MULTI_PROJECTIN = 9;

    /*所得项目*/
    public static final int PROJECTIN_SALARY = 0;//工资薪金所得（居民）
    public static final int PROJECTIN_LABOUR = 1; //劳务报酬所得（居民）
    //public static final int PROJECTIN_REMUN = 2;//稿酬所得
    public static final int PROJECTIN_FRAN = 3;//特许权使用费所得
    public static final int PROJECTIN_YEAREND = 4;//全年一次性奖金所得
    public static final int PROJECTIN_SALARY_FOREIGN = 5;//工资薪金所得（非居民）
    public static final int PROJECTIN_LABOUR_FOREIGN = 2; //劳务报酬所得（非居民）
    public static final int PROJECTIN_CONTRACT_TERMINATION = 16; //解除劳动合同（居民）
    public static final int PROJECTIN_LABOR_BROKER = 17; //劳务报酬（保险营销员/证券经纪人/其他连续劳务）居民

    public static final String SALARY = "SALARY_";//工资薪金所得（居民）
    public static final String LABOUR = "LABOUR_"; //劳务报酬所得（居民）
    public static final String SALARY_FOREIGN = "SALARY_FOREIGN_";//工资薪金所得（非居民）
    public static final String LABOUR_FOREIGN = "LABOUR_FOREIGN_"; //劳务报酬所得（非居民）
    public static final String YEAREND = "YEAREND_"; //全年一次性奖金所得
    public static final String dissolveLabor = "DissolveLabor_"; //解除劳动合同（居民）
    public static final String labourRemuneration = "LabourRemuneration_"; //劳务报酬（保险营销员/证券经纪人/其他连续劳务）居民


    public static final String GLOBAL_ORG_NAME = "P_YS_OA_LCGL_0001035404" /* "全局" */;
    /*--核算范围类型--*/

    /*组织*/
    public static final String ORG = "org";
    /*人员类别*/
    public static final String STAFF_CATEGORY = "pcategory";
    /*职级*/
    public static final String JOB_GRADE = "jobgrade";
    /*职等*/
    public static final String JOB_RANK = "jobrank";
    /*人员状态  0：全部 1：在职 2：非在职*/
    public static final String STAFF_STATE = "staffstate";
    /*变动类型*/
    public static final String CHANGE_TYPE = "changetype";
    /*职级*/
    public static final String JOB_TYPE = "jobtype";

    /**
     * 薪资组
     */
    public static final String WAGE_GROUP = "wagegroup";

    /**
     * 职位
     */
    public static final String NEWPOSTID = "newpostid";

    /**
     * 任职类型
     */
    public static final String PARTTYPE = "parttype";

    /*人民币*/
    public static final String CNY_CURRENCY_CODE = "G001ZM0000DEFAULTCURRENCT00000000001";
    public static final String CNY_CURRENCY_NAME = "P_YS_FI_CM_0000029897" /* "人民币" */;

    /*退休标识*/
    public static final int RETIRE_ENABLE = 1;
    public static final int RETIRE_UNABLE = 0;

    //不扣税
    public static final int NO_TAX_DEDUCTION = 0;
    public static final String NO_TAX_DEDUCTION_STR = "0";
    //代扣税
    public static final int WITHHOLDING_TAX = 1;
    //代付税
    public static final int PAY_TAX = 2;

}
