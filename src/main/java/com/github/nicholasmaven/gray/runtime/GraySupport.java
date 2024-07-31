package com.github.nicholasmaven.gray.runtime;

import com.github.nicholasmaven.gray.config.cache.GrayRuleManager;
import com.github.nicholasmaven.gray.runtime.misc.GrayMetadata;
import com.github.nicholasmaven.gray.runtime.misc.GrayMonitor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>灰度断言, shouldGray返回true/false
 * <p>E - 灰度属性实体类型
 * @author mawen
 */
@Getter
@Slf4j
public class GraySupport {

    private GrayMonitor monitor;

    @Autowired
    public GraySupport(GrayMonitor monitor) {
        this.monitor = monitor;
    }

    /**
     * <p>灰度断言
     * <p>注意: 该方法不能抛异常! 否则代理模式中将返回false(视为未命中灰度)
     * @param propertyHolder 灰度属性实体类
     * @return true/false
     */
    public <E> boolean shouldGray(String bizKey, E propertyHolder) {
        long start = System.currentTimeMillis();
        GrayMetadata metadata = GrayRuleManager.getMetadata(bizKey);
        if (metadata == null) {
            log.error("no config for bizKey {}, gray prediction false", bizKey);
            return false;
        }
        boolean result = metadata.getFilter().apply(propertyHolder);
        if (monitor != null) {
            monitor.reportGraySummary(bizKey, result, System.currentTimeMillis() - start);
        }
        return result;
    }

}
