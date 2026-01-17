package org.example.springboot3java21demo.model;

import lombok.Data;

import java.util.List;

/**
 * 
 */
@Data
public class ProcessBallResDto {

    /**
     * 总数
     */
    private int count;

    /**
     * 成功的数量
     */
    private int successCount;

    /**
     * 失败的数量
     */
    private int failCount;

    /**
     * 返回错误信息
     */
    private List<String> messages;

    /**
     * 返回数据
     */
    private List<InfoDto> infos;

    /**
     * 返回错误信息
     */
    private List<String> failInfos;

}
