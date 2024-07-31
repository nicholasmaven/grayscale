package com.github.nicholasmaven.gray.config.rule.parser;

import com.github.nicholasmaven.gray.config.rule.condition.PercentGrayCondition;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * @author mawen
 */
@Data
public class PercentGrayRuleParser implements GrayRuleParser {

    private final String PERCENT_DELIMITER = "%";
    // 小数点后超过5位，转化的时候会有精度丢失
    private final String PERCENT_FORMAT = "^(100(\\.0{1,5})?|([1-9]?\\d(\\.\\d{1,5})?))%$";

    @Override
    public boolean matchFormatter(String rule) {
        return StringUtils.isNotBlank(rule)
                && ruleFormatCheck(rule);
    }

    private boolean ruleFormatCheck(String rule) {
        return rule.matches(PERCENT_FORMAT);
    }

    @Override
    public PercentGrayCondition parse(String rule) {
        String percentStr = rule.replace(PERCENT_DELIMITER, "");
        return new PercentGrayCondition(Float.parseFloat(percentStr));
    }
}
