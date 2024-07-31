package com.github.nicholasmaven.gray.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * @author mawen
 */
@Getter
public enum AdoptOnDiffTypeEnum {
    GRAY,
    REGULAR,
    BREAK;

    public static boolean invalid(String type) {
        if (StringUtils.isBlank(type)) {
            return false;
        }
        return Arrays.stream(values())
                .anyMatch(adoptOnDiffTypeEnum ->
                        type.toUpperCase().equals(adoptOnDiffTypeEnum.name()));
    }
}
