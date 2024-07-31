package com.github.nicholasmaven.gray.runtime.filter;

import com.github.nicholasmaven.gray.config.rule.condition.GrayCondition;
import com.github.nicholasmaven.gray.config.rule.condition.PercentGrayCondition;
import com.github.nicholasmaven.gray.enums.ConditionTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

/**
 * @author mawen
 */
@Slf4j
public class GrayRuleFilter implements GrayFilter {
    private final String bizKey;

    private final String property;

    private final List<GrayCondition> conditions;

    private PercentGrayCondition percentCondition;

    public GrayRuleFilter(String bizKey, String property, List<GrayCondition> conditions) {
        Assert.notNull(bizKey, "bizKey is null");

        this.bizKey = bizKey;
        this.property = property;
        if (conditions == null) {
            this.conditions = Collections.emptyList();
            percentCondition = null;
        } else {
            this.conditions = Collections.unmodifiableList(conditions);
            log.info("parsed conditions: {}", conditions);
            for (GrayCondition c : conditions) {
                if (c.getType() == ConditionTypeEnum.PERCENT) {
                    percentCondition = (PercentGrayCondition) c;
                    break;
                }
            }
        }
    }

    @Override
    public <P> boolean apply(P propertyHolder) {
        if (CollectionUtils.isEmpty(conditions)) {
            log.warn("bizKey {}: no available rule condition, gray prediction false", bizKey);
            return false;
        }

        if (propertyHolder == null) {
            if (percentCondition == null) {
                log.error("bizKey {}: neither propertyHolder nor percent gray condition is null, gray prediction false", bizKey);
                return false;
            } else {
                boolean result = percentCondition.executeWithoutProperty();
                log.info("bizKey {}: pure traffic gray result {}", bizKey, result);
                return result;
            }
        }

        String value;
        try {
            value = resolveProperty(propertyHolder);
        } catch(Exception e) {
            log.error("bizKey {}: error occurs when resolving {} in {}, gray prediction false", bizKey, property,
                    propertyHolder.getClass(), e);
            return false;
        }

        boolean result;
        try {
            result = conditions.stream().allMatch(e -> e.execute(value));
        } catch(Exception e) {
            log.error("bizKey {}: error occurs when filter condition evaluating, fallback to false", bizKey, e);
            return false;
        }
        if (result) {
            log.debug("bizKey {}: property {} value {} hit gray", bizKey, property, value);
        }
        return result;
    }

    /**
     * chaining property is not supported
     */
    protected <P> String resolveProperty(P propertyHolder) throws ReflectiveOperationException {
        Class<?> pClass = propertyHolder.getClass();
        if (pClass.equals(Integer.class) || pClass.equals(String.class) || pClass.equals(Long.class)
                || pClass.equals(Character.class) || pClass.equals(Double.class) || pClass.equals(Float.class)) {
            return propertyHolder.toString();
        }

        Field field = pClass.getDeclaredField(property);
        field.setAccessible(true);
        Object value = field.get(propertyHolder);
        return value.toString();
    }
}
