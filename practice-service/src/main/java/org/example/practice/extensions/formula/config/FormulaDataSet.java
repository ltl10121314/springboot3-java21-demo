package org.example.practice.extensions.formula.config;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@XStreamAlias("dataset")
public class FormulaDataSet {

    @XStreamAlias("content-creator-class")
    private ContentCreatorClass contentCreatorClass;

    @XStreamAlias("parameters")
    List<String> parameters;


}
