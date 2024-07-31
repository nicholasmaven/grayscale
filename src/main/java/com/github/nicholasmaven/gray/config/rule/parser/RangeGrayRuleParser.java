package com.github.nicholasmaven.gray.config.rule.parser;

import cn.hutool.core.text.CharSequenceUtil;
import com.github.nicholasmaven.gray.config.rule.condition.RangeGrayCondition;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * @author mawen
 */
@Slf4j
public class RangeGrayRuleParser implements GrayRuleParser {
    private final String VALUE_DELIMITER = ",";
    private final String[] RANGE_PREFIX = new String[]{"(", "["};
    private final String[] RANGE_SUFFIX = new String[]{")", "]"};
    @Override
    public boolean matchFormatter(String rule) {
        if (StringUtils.isBlank(rule)) {
            return false;
        }
        if(!CharSequenceUtil.startWithAny(rule, RANGE_PREFIX)
                && !CharSequenceUtil.endWithAny(rule, RANGE_SUFFIX)) {
            return false;
        }
        if (!validDataCheck(rule)) {
            return false;
        }
        return checkRangeValue(rule);
    }

    /**
     * 检查范围值。左边界必须 < 右边界
     * @param rule
     * @return boolean
     */
    private boolean checkRangeValue(String rule) {
        Long[] rangeData = getRangeData(rule);
        boolean leftLessThanRight = rangeData[0].compareTo(rangeData[1]) < 0;
        if (!leftLessThanRight) {
            log.warn("RangeGrayRule: {} should ensure left less than right", rule);
        }
        return leftLessThanRight;
    }

    /**
     * 解析范围边界
     * @param rule
     * @return {@link Long[]}
     */
    private Long[] getRangeData(String rule) {
        String rangeData = rule.substring(1, rule.length() - 1);
        String[] split = rangeData.split(VALUE_DELIMITER);
        return Arrays.stream(split)
                .map(String::trim)
                .map(Long::valueOf)
                .toArray(Long[]::new);
    }


    /**
     * 有效数据校验,必须可转化为数值类型
     * @param rule
     * @return boolean
     */
    private boolean validDataCheck(String rule) {
        String rangeData = rule.substring(1, rule.length() - 1);
        String[] split = rangeData.split(VALUE_DELIMITER);
        if (split.length != 2) {
            return false;
        }
        for (String s : split) {
            try {
                s = s.trim();
                Long value = Long.valueOf(s);
            } catch (NumberFormatException e) {
                log.error("failed to parse range data: {}", rule);
                return false;
            }
        }
        return true;
    }

    @Override
    public RangeGrayCondition parse(String rule) {
        String beginSymbol = rule.substring(0, 1);
        String endSymbol = rule.substring(rule.length()-1);
        Long[] rangeData = getRangeData(rule);
        RangeGrayCondition.GrayConditionItem left = new RangeGrayCondition.GrayConditionItem(beginSymbol, rangeData[0]);
        RangeGrayCondition.GrayConditionItem right = new RangeGrayCondition.GrayConditionItem(endSymbol, rangeData[1]);
        return new RangeGrayCondition(left, right);
    }

}
