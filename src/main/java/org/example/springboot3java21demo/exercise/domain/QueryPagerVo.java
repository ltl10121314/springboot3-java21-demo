package org.example.springboot3java21demo.exercise.domain;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class QueryPagerVo implements Serializable {
    private static final long serialVersionUID = 389876039032716064L;
    private int pageIndex;
    private int pageSize = 10;
    private int totalCount;
    private String pubts;

    public QueryPagerVo() {
    }

    public QueryPagerVo(int pageIndex, int pageSize) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
    }

    public int pageIndex() {
        return this.pageIndex;
    }

    public int pageSize() {
        return this.pageSize;
    }

}
