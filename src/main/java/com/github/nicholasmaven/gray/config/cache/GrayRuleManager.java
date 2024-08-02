package com.github.nicholasmaven.gray.config.cache;

import com.github.nicholasmaven.gray.config.properties.GrayRootProperties;
import com.github.nicholasmaven.gray.config.properties.GrayRuleProperties;
import com.github.nicholasmaven.gray.config.rule.condition.GrayCondition;
import com.github.nicholasmaven.gray.config.rule.parser.GrayRuleParser;
import com.github.nicholasmaven.gray.config.rule.parser.ListValueGrayRuleParser;
import com.github.nicholasmaven.gray.config.rule.parser.PercentGrayRuleParser;
import com.github.nicholasmaven.gray.config.rule.parser.RangeGrayRuleParser;
import com.github.nicholasmaven.gray.enums.AdoptOnDiffTypeEnum;
import com.github.nicholasmaven.gray.enums.GrayConditionTypeEnum;
import com.github.nicholasmaven.gray.event.GrayRuleChangeEvent;
import com.github.nicholasmaven.gray.runtime.diff.AdoptBreakOnDiff;
import com.github.nicholasmaven.gray.runtime.diff.AdoptGrayOnDiff;
import com.github.nicholasmaven.gray.runtime.diff.AdoptOnDiff;
import com.github.nicholasmaven.gray.runtime.diff.AdoptRegularOnDiff;
import com.github.nicholasmaven.gray.runtime.filter.GrayRuleFilter;
import com.github.nicholasmaven.gray.runtime.misc.GrayMetadata;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author mawen
 */
@Slf4j
public class GrayRuleManager implements ApplicationListener<GrayRuleChangeEvent> {

    private final static String GRAY_CONFIG_PREFIX = "gray.config";
    private static GrayRootProperties grayRootProperties;
    private final static String RULE_DELIMITER = "#";
    private final static String PROPERTY_REX = "[a-zA-Z_$][a-zA-Z0-9_$]*";
    private static volatile boolean dirty = false;

    private static volatile GrayRuleManager instance;

    // bizKey -> gray metadata map
    private static volatile Map<String, GrayMetadata> METADATA_CACHE = new HashMap<>();
    private static final Map<GrayConditionTypeEnum, GrayRuleParser> PARSER_MAP = new ConcurrentHashMap<>();
    private static final Map<AdoptOnDiffTypeEnum, AdoptOnDiff> ADOPT_ON_DIFF_MAP = new ConcurrentHashMap<>();
    private static final Map<String, AdoptOnDiff> CUSTOMIZE_ADOPT_DIFF_MAP = new ConcurrentHashMap<>();

    private GrayRuleManager(GrayRootProperties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("gray rule is not setting");
        }
        grayRootProperties = properties;
        initBuildInProcessor();
        refreshMetadata();
    }


    /**
     * initiate rule parser
     */
    private static void initBuildInProcessor() {
        PARSER_MAP.put(GrayConditionTypeEnum.RANGE, new RangeGrayRuleParser());
        PARSER_MAP.put(GrayConditionTypeEnum.LIST_MATCH, new ListValueGrayRuleParser());
        PARSER_MAP.put(GrayConditionTypeEnum.PERCENT, new PercentGrayRuleParser());

        ADOPT_ON_DIFF_MAP.put(AdoptOnDiffTypeEnum.GRAY, new AdoptGrayOnDiff());
        ADOPT_ON_DIFF_MAP.put(AdoptOnDiffTypeEnum.REGULAR, new AdoptRegularOnDiff());
        ADOPT_ON_DIFF_MAP.put(AdoptOnDiffTypeEnum.BREAK, new AdoptBreakOnDiff());
    }

    private static void refreshMetadata() {
        Map<String, GrayRuleProperties> config = grayRootProperties.getConfig();
        if (CollectionUtils.isEmpty(config)) {
            log.warn("empty metadata config!");
            return;
        }
        Map<String, GrayMetadata> map = new HashMap<>();
        for (Map.Entry<String, GrayRuleProperties> entry : config.entrySet()) {
            try {
                String bizKey = entry.getKey();
                GrayRuleProperties value = entry.getValue();
                if (value.getProperty()!= null && !value.getProperty().matches(PROPERTY_REX)) {
                    log.warn("bizType:{} gray rule property:{} is invalid. must be {}",
                            bizKey, value.getProperty(), PROPERTY_REX);
                    continue;
                }
                List<GrayCondition> conditions = parseRuleDetail(bizKey, value.getRule());
                GrayRuleFilter filter = new GrayRuleFilter(bizKey, value.getProperty(), conditions);
                AdoptOnDiff adopt = parseDiffStrategy(value.getAdoptOnDiff());
                GrayMetadata metadata = new GrayMetadata(bizKey, filter, adopt);
                map.put(bizKey, metadata);
                log.info("success refresh {} gray config:{}", bizKey, value);
            } catch (Exception e) {
                log.error("failed to parse {} gray rule :{}", entry.getKey(), entry.getValue());
            }
        }
        METADATA_CACHE = Collections.unmodifiableMap(map);
        log.info("finish refresh gray config, current config is:{}", config);
    }

    private static List<GrayCondition> parseRuleDetail(String bizKey, String ruleStr) {
        List<GrayCondition> conditions = new ArrayList<>();
        // {1,2,3}#[1,10]#10%#60%
        String[] ruleArray = ruleStr.split(RULE_DELIMITER);
        if (ruleArray.length > 3) {
            log.warn("bizType:{} gray rule {} count more than 3. pls check and update.",bizKey, ruleStr);
            return Lists.newArrayList();
        }
        for (String rule : ruleArray) {
            for (GrayRuleParser grayRuleParser : PARSER_MAP.values()) {
                if (grayRuleParser.matchFormatter(rule)) {
                    conditions.add(grayRuleParser.parse(rule));
                    break;
                }
            }
        }
        if (CollectionUtils.isEmpty(conditions)) {
            log.warn("bizType:{} gray rule:{} can not parse to GrayCondition.class. pls check and update.",bizKey, ruleStr);
            return Lists.newArrayList();
        }
        // 如果解析出来的规则不止一个，认为是异常解析
        Map<? extends Class<?>, List<GrayCondition>> conditionMap = conditions.stream()
                .collect(Collectors.groupingBy(Object::getClass));
        for (Map.Entry<? extends Class<?>, List<GrayCondition>> listEntry : conditionMap.entrySet()) {
            // 每种规则只能单一
            if (listEntry.getValue().size() > 1) {
                log.warn("bizType:{} gray rule type:{} count more than {}. pls check and update.",bizKey,
                        listEntry.getKey().getSimpleName(), listEntry.getValue().size());
                return Lists.newArrayList();
            }
        }
        return conditions;
    }

    private static AdoptOnDiff parseDiffStrategy(String name) {
        AdoptOnDiff adoptOnDiff = null;
        if (StringUtils.isBlank(name)) {
            return null;
        }
        // 先检测是否存在自定义类型，再检查是否内置类型
        if (CUSTOMIZE_ADOPT_DIFF_MAP.containsKey(name)) {
            adoptOnDiff = CUSTOMIZE_ADOPT_DIFF_MAP.get(name);
        } else {
            try {
                AdoptOnDiffTypeEnum adoptOnDiffTypeEnum = AdoptOnDiffTypeEnum.valueOf(name.toUpperCase());
                adoptOnDiff = ADOPT_ON_DIFF_MAP.get(adoptOnDiffTypeEnum);
            } catch(IllegalArgumentException e) {
                // do nothing
            }
        }
        return adoptOnDiff;
    }

    public static GrayMetadata getMetadata(String bizKey) {
        refreshWhenConfigUpdate();
        return METADATA_CACHE.get(bizKey);
    }

    public static GrayRuleManager newInstance(GrayRootProperties grayRootProperties) {
        if (instance == null) {
            synchronized (GrayRuleManager.class) {
                if (instance == null) {
                    Assert.notNull(grayRootProperties, "please check the gray rule.");
                    instance = new GrayRuleManager(grayRootProperties);
                    return instance;
                }
            }
        }
        return instance;
    }

    private static void refreshWhenConfigUpdate() {
        if (dirty) {
            synchronized (GrayRuleManager.class) {
                if (dirty) {
                    refreshMetadata();
                    dirty = false;
                }
            }
        }
    }

    public void registerAdoptOnDiff(Map<String, AdoptOnDiff> customizeAdoptDiffMap) {
        if (customizeAdoptDiffMap == null || customizeAdoptDiffMap.isEmpty()) {
            return;
        }
        customizeAdoptDiffMap.forEach(this::registerAdoptOnDiff);
        // set up refresh flag
        dirty = true;
    }

    private void registerAdoptOnDiff(String name, AdoptOnDiff adoptOnDiff) {
        if (AdoptOnDiffTypeEnum.invalid(name)) {
            log.info("AdoptOnDiff of {} is an build in type, will not allow cover.", name);
            return;
        }
        if (CUSTOMIZE_ADOPT_DIFF_MAP.containsKey(name)) {
            log.info("Customize AdoptOnDiff of {} already exist, will cover it.", name);
        }
        CUSTOMIZE_ADOPT_DIFF_MAP.put(name, adoptOnDiff);
    }

   public void onApplicationEvent(GrayRuleChangeEvent event) {
        log.info("listen to refresh gray config:{}",event);
        Set<String> keys = event.getKeys();
        if (CollectionUtils.isEmpty(keys)) {
            return;
        }
        //触发重新解析
        for (String key : keys) {
            if (StringUtils.isNotBlank(key) && key.startsWith(GRAY_CONFIG_PREFIX)) {
                dirty = true;
                return;
            }
        }
    }

}
