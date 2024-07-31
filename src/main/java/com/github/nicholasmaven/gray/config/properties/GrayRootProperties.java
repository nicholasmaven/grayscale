package com.github.nicholasmaven.gray.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * @author mawen
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "gray")
public class GrayRootProperties {
    private Map<String, GrayRuleProperties> config;
}
