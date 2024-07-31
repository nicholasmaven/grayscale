package com.github.nicholasmaven.gray.config.properties;

import com.github.nicholasmaven.gray.annotations.GrayDiff;
import com.github.nicholasmaven.gray.config.cache.GrayRuleManager;
import com.github.nicholasmaven.gray.runtime.diff.AdoptOnDiff;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mawen
 */
@Configuration
public class GrayRuleAutoConfiguration {

    @Bean
    public GrayRuleManager grayRuleManager(GrayRootProperties grayRootProperties, List<AdoptOnDiff> customizeAdoptOnDiff) {
        GrayRuleManager grayRuleManager = GrayRuleManager.newInstance(grayRootProperties);
        Map<String, AdoptOnDiff> customizeAdoptOnDiffMap = new HashMap<>();
        if (!customizeAdoptOnDiff.isEmpty()) {
            for (AdoptOnDiff adoptOnDiff : customizeAdoptOnDiff) {
                GrayDiff grayDiff = adoptOnDiff.getClass().getAnnotation(GrayDiff.class);
                customizeAdoptOnDiffMap.put(grayDiff.name(), adoptOnDiff);
            }
            grayRuleManager.registerAdoptOnDiff(customizeAdoptOnDiffMap);
        }
        return grayRuleManager;
    }
}
