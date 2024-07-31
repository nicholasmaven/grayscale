package com.github.nicholasmaven.gray.config.rule.condition;

import com.github.nicholasmaven.gray.enums.ConditionTypeEnum;
import com.github.nicholasmaven.gray.enums.OperatorRelationEnum;
import lombok.Data;

/**
 * @author mawen
 */
@Data
public class RangeGrayCondition implements GrayCondition {
    private final String[] VALID_CHAR = new String[]{"'", "\""};

    private GrayConditionItem left;
    private GrayConditionItem right;

    public RangeGrayCondition(GrayConditionItem left, GrayConditionItem right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean execute(String value) {
        return checkIfBetweenRange(left, value) && checkIfBetweenRange(right, value);
    }

    @Override
    public ConditionTypeEnum getType() {
        return ConditionTypeEnum.RANGE;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        if (left == null) {
            buf.append("(Long.MIN_VALUE, ");
        } else {
            buf.append(left.operator.getSymbol());
            buf.append(left.getValue());
            buf.append(",");
        }

        if (right == null) {
            buf.append("Long.MAX_VALUE)");
        } else {
            buf.append(right.operator.getSymbol());
            buf.append(right.getValue());
        }
        return buf.toString();
    }

    private boolean checkIfBetweenRange(GrayConditionItem item,String value) {
        value = replaceValidCharBeforeParse(value);
        Long compareValue = Long.parseLong(value);
        // < -1 = 0 > 1
        switch (item.operator) {
            case LARGE:
                return compareValue.compareTo(item.value) > 0;
            case LARGE_EQ:
                return compareValue.compareTo(item.value) >= 0;
            case LESS:
                return compareValue.compareTo(item.value) < 0;
            case LESS_EQ:
                return compareValue.compareTo(item.value) <= 0;
            default:
                return false;
        }
    }

    private String replaceValidCharBeforeParse(String s) {
        for (String str : VALID_CHAR) {
            s = s.replace(str,"");
        }
        return s;
    }

    @Data
    public static class GrayConditionItem {
        private OperatorRelationEnum operator;
        private Long value;

        public GrayConditionItem(String symbol, Long value) {
            this.operator = OperatorRelationEnum.transferSymbol(symbol);
            this.value = value;
        }
    }

}
