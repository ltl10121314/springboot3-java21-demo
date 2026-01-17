package org.example.springboot3java21demo.service.impl;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextAwareImpl implements ApplicationContextAware {
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        StudentServiceImpl bean = applicationContext.getBean(StudentServiceImpl.class);
        System.out.println("applicationContext:" + applicationContext);
    }
}
