package com.github.nicholasmaven.gray.runtime.misc;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Summary;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

/**
 * @author mawen
 */
public class GrayMonitor {
    private CollectorRegistry collectorRegistry;

    private Summary graySummary;

    private Summary compareSummary;

    @Autowired
    public GrayMonitor(CollectorRegistry collectorRegistry) {
        this.collectorRegistry = collectorRegistry;
    }

    @PostConstruct
    public void init() {
        graySummary = Summary.build()
                .name("gray_execute")
                .labelNames("bizKey", "cmdType")
                .help("gray execution summary")
                .register(collectorRegistry);

        compareSummary = Summary.build()
                .name("gray_compare")
                .labelNames("bizKey", "hasDiff")
                .help("gray comparison summary")
                .register(collectorRegistry);
    }

    public void reportGraySummary(String bizKey, boolean isGray, long ms) {
        graySummary.labels(bizKey, isGray ? "gray" : "regular").observe(toSecond(ms));
    }

    public void reportCompareSummary(String bizKey, boolean identical, long ms) {
        compareSummary.labels(bizKey, identical ? "identical" : "diff").observe(toSecond(ms));
    }

    public double toSecond(long millisecond) {
        return millisecond / 1000.0d;
    }

}
