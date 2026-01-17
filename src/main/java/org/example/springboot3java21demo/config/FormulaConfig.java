package org.example.springboot3java21demo.config;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@XStreamAlias("item")
public class FormulaConfig {

    @XStreamAsAttribute()
    private String name;
    @XStreamAsAttribute
    private String resid;
    @XStreamAsAttribute
    private String itemflag;
    @XStreamAsAttribute
    private String itemscope;
    @XStreamAlias("variable")
    private FormulaVariable variable;
    @XStreamAlias("dataset")
    private FormulaDataSet dataset;

}
