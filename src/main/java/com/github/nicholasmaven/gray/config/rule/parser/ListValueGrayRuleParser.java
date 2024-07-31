package com.github.nicholasmaven.gray.config.rule.parser;
import com.github.nicholasmaven.gray.config.rule.condition.ListMatchGrayCondition;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author mawen
 */
public class ListValueGrayRuleParser implements GrayRuleParser {
    private final String VALUE_DELIMITER = ",";
    private final String LIST_LEFT = "{";
    private final String LIST_RIGHT = "}";
    @Override
    public boolean matchFormatter(String rule) {
        return !StringUtils.isBlank(rule)
                && (rule.startsWith(LIST_LEFT) && rule.endsWith(LIST_RIGHT));
    }

    @Override
    public ListMatchGrayCondition parse(String rule) {
        String dataStr = rule.substring(LIST_LEFT.length(), rule.length() - LIST_RIGHT.length());
        return new ListMatchGrayCondition(
                Arrays.stream(dataStr.split(VALUE_DELIMITER))
                        .map(String::trim)
                .collect(Collectors.toList()));
    }
}
