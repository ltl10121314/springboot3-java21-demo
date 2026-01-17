package org.example.springboot3java21demo.config;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@XStreamAlias("content-creator-class")
public class ContentCreatorClass {
    @XStreamAsAttribute
    private String name;
    @XStreamAsAttribute
    private String beanId;

}
