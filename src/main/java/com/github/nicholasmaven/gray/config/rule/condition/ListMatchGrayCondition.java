package com.github.nicholasmaven.gray.config.rule.condition;

import cn.hutool.core.text.CharSequenceUtil;
import com.github.nicholasmaven.gray.enums.ConditionTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author mawen
 */
@Data
@AllArgsConstructor
public class ListMatchGrayCondition implements GrayCondition {
    private List<String> values;

    @Override
    public boolean execute(String value) {
        value = CharSequenceUtil.trimEnd(CharSequenceUtil.trimStart(value));
        return StringUtils.isNotBlank(value)
                && !CollectionUtils.isEmpty(values)
                && values.contains(value);
    }

    @Override
    public ConditionTypeEnum getType() {
        return ConditionTypeEnum.LIST;
    }

    @Override
    public String toString() {
        return values.toString();
    }
}
