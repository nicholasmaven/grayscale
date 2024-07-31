package com.github.nicholasmaven.gray.config.rule.parser;

import com.github.nicholasmaven.gray.config.rule.condition.GrayCondition;

/**
 * @author mawen
 */
public interface GrayRuleParser {
    boolean matchFormatter(String rule);
    GrayCondition parse(String rule);

    /**
     * 替换指定的字符，最后再前后去空
     * @param s
     * @param validSymbolArr
     * @return {@link String}
     */
    default String replaceValidCharBeforeParse(String s, String[] validSymbolArr) {
        for (String str : validSymbolArr) {
            s = s.replace(str,"");
        }
        return s.trim();
    }
}
