package org.example.springboot3java21demo.model;

import lombok.Data;

/**
 * 
 */
@Data
public class PollSelectResDto {

    private int flag;

    /**
     * 返回状态码
     */
    private int code;

    private ProcessBallResDto data;

    /**
     * 失败数量
     */
    private int failCount;

    private int count;

    /**
     * 总数
     */
    private int totalCount;

    /**
     * 返回进度百分比
     */
    private int percentage;

    /**
     * 成功数量
     */
    private int successCount;
}
