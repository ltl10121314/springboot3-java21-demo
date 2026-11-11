package org.example.practice.spring.context;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ApplicationContextAwareImpl implements ApplicationContextAware {
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        log.info("practice applicationContext={}, beanDefinitionCount={}",
                applicationContext.getApplicationName(), applicationContext.getBeanDefinitionCount());
    }
}
