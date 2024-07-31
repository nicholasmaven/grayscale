package com.github.nicholasmaven.gray.config.properties;

import lombok.Data;

/**
 * @author mawen
 */
@Data
public class GrayRuleProperties {
    private String property;
    private String rule;
    private String adoptOnDiff;
}
