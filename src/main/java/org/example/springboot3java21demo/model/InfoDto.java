package org.example.springboot3java21demo.model;

import lombok.Data;

/**
 * 
 */
@Data
public class InfoDto {

    /**
     * 异步(唯一标志)
     */
    private String asyncKey;

    /**
     * url
     */
    private String url;
}
