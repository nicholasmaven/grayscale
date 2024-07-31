package com.github.nicholasmaven.gray.runtime.misc;

import org.apache.commons.codec.digest.MurmurHash3;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author mawen
 */
public class PercentUtils {

    /**
     * 计算百分比, 判断是否落在比例内
     * @param value 属性值
     * @param numerator 分子
     * @param denominator 分母
     */
    public static boolean matchByHash(String value, int numerator, int denominator) {
        if (numerator >= denominator) {
            return true;
        }
        if (numerator <= 0) {
            return false;
        }
        if (StringUtils.isBlank(value)) {
            return false;
        }
        int val = MurmurHash3.hash32x86(value.getBytes());
        return (Math.abs(val) % denominator) <= numerator;
    }

    public static boolean matchByRandomAlg(int numerator, int denominator) {
        if (numerator >= denominator) {
            return true;
        }
        if (numerator <= 0) {
            return false;
        }
        return ThreadLocalRandom.current().nextInt(denominator) <= numerator;
    }

}
