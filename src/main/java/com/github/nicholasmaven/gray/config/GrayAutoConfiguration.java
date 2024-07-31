package com.github.nicholasmaven.gray.config;

import com.github.nicholasmaven.gray.config.properties.GrayRootProperties;
import com.github.nicholasmaven.gray.config.properties.GrayRuleAutoConfiguration;
import com.github.nicholasmaven.gray.runtime.GraySupport;
import com.github.nicholasmaven.gray.runtime.misc.GrayMonitor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author mawen
 */
@Configuration
@Import({GrayRuleAutoConfiguration.class, GrayRuleApolloRefresher.class, GrayMonitor.class})
@EnableConfigurationProperties({GrayRootProperties.class})
public class GrayAutoConfiguration {

    @Bean
    public GraySupport graySupport(GrayMonitor monitor) {
        return new GraySupport(monitor);
    }
}
