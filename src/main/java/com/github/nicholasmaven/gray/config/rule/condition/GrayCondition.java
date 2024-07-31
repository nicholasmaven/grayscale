package com.github.nicholasmaven.gray.config.rule.condition;

import com.github.nicholasmaven.gray.enums.ConditionTypeEnum;

/**
 * @author mawen
 */
public interface GrayCondition {
    boolean execute(String value);

    ConditionTypeEnum getType();
}
