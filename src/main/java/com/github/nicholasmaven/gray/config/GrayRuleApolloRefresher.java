package com.github.nicholasmaven.gray.config;

import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
import com.github.nicholasmaven.gray.event.GrayRuleChangeEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author mawen
 */
@Slf4j
public class GrayRuleApolloRefresher implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    @ApolloConfigChangeListener(value = "grayscale", interestedKeyPrefixes = {"gray.config"})
    private void onChange(ConfigChangeEvent changeEvent) {
        log.info("accept apollo change:{}", changeEvent);
        this.applicationContext.publishEvent(new EnvironmentChangeEvent(changeEvent.changedKeys()));
        this.applicationContext.publishEvent(new GrayRuleChangeEvent(changeEvent.changedKeys()));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
