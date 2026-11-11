package org.example.practice.extensions.formula.config;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@XStreamAlias("variable")
public class FormulaVariable {

    @XStreamAlias("content-creator-class")
    private ContentCreatorClass contentCreatorClass;

}
