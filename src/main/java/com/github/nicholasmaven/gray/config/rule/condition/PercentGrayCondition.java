package com.github.nicholasmaven.gray.config.rule.condition;

import com.github.nicholasmaven.gray.enums.ConditionTypeEnum;
import com.github.nicholasmaven.gray.runtime.misc.PercentUtils;

/**
 * @author mawen
 */
public class PercentGrayCondition implements GrayCondition {
    public static final int DENOMINATOR = 10000;

    /**
     * 万分数的分子值
     */
    private final int numerator;

    public PercentGrayCondition(Float rawNumerator) {
        //百分数分子转为万分数分子
        numerator = (int) (rawNumerator * 100);
    }

    @Override
    public boolean execute(String value) {
        return PercentUtils.matchByHash(value, numerator, DENOMINATOR);
    }

    public boolean executeWithoutProperty() {
        return PercentUtils.matchByRandomAlg(numerator, DENOMINATOR);
    }

    @Override
    public ConditionTypeEnum getType() {
        return ConditionTypeEnum.PERCENT;
    }

    @Override
    public String toString() {
        return numerator + " / " + DENOMINATOR;
    }
}
