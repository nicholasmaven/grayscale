package com.github.nicholasmaven.gray.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * @author mawen
 */
@Getter
public enum OperatorRelationEnum {
    LARGE("(",">"),
    LARGE_EQ("[",">="),
    LESS(")","<"),
    LESS_EQ("]","<="),
    ;

    OperatorRelationEnum(String symbol, String operator) {
        this.operator = operator;
        this.symbol = symbol;
    }

    private String operator;
    private String symbol;


    public static OperatorRelationEnum transferSymbol(String symbol) {
        return Arrays.stream(values())
                .filter(operatorRelationEnum ->
                        StringUtils.isNotBlank(symbol)
                                && symbol.equals(operatorRelationEnum.getSymbol()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("invalid symbol " + symbol));
    }
}
