package com.github.nicholasmaven.gray.runtime;

import com.github.nicholasmaven.gray.config.cache.GrayRuleManager;
import com.github.nicholasmaven.gray.runtime.diff.AdoptOnDiff;
import com.github.nicholasmaven.gray.runtime.diff.CompareDiffException;
import com.github.nicholasmaven.gray.runtime.misc.ExecutionOutcome;
import com.github.nicholasmaven.gray.runtime.misc.GrayMonitor;
import lombok.extern.slf4j.Slf4j;

/**
 * <pre>
 * 灰度代理命令封装, 定制对照逻辑有2种方式
 * 1. 重写{@link AbstractGrayCommand#compareResult(Object, Object)}
 * 2. 结果实体类R重写equals
 *
 * 泛型说明
 * E - 灰度属性实体类型
 * P - 请求类型
 * R - 返回类型
 * </pre>
 *
 * @author mawen
 */
@Slf4j
public abstract class AbstractGrayCommand<P, R> extends GraySupport {

    public AbstractGrayCommand(GrayMonitor monitor) {
        super(monitor);
    }

    public <E> R execute(String bizKey, E propertyHolder, P request) throws Exception {
        boolean hitGray = shouldGray(bizKey, propertyHolder);
        //run logic to get outcome
        ExecutionOutcome<R> outcome;
        try {
            log.debug("bizKey {}: run {} logic", bizKey, hitGray ? "gray" : "regular");
            R result = hitGray ? runGrayLogic(request) : runRegularLogic(request);
            outcome = ExecutionOutcome.normal(result);
        } catch(Exception e) {
            outcome = ExecutionOutcome.exception(e);
        }

        //compare
        AdoptOnDiff adopt = GrayRuleManager.getMetadata(bizKey).getAdopt();
        if (hitGray && adopt != null) {
            log.debug("bizKey {}: after hit gray, compare outcome using strategy {}", bizKey, adopt.getClass());
            outcome = compare(bizKey, request, outcome);
        }

        if (outcome.getException() != null) {
            throw outcome.getException();
        }
        return outcome.getResult();
    }

    /**
     * 对照开关: adopt不为null表示启用对照, 否则禁用
     *
     * @param request 请求
     * @param grayOutcome 灰度输出
     * @return 输出
     */
    private ExecutionOutcome<R> compare(String bizKey, P request, ExecutionOutcome<R> grayOutcome) throws CompareDiffException {
        long start = System.currentTimeMillis();
        log.info("bizKey {}: comparison enabled", bizKey);

        ExecutionOutcome<R> regularOutcome;
        try {
            log.debug("bizKey {}: run regular logic", bizKey);
            R result = runRegularLogic(request);
            regularOutcome = ExecutionOutcome.normal(result);
        } catch(Exception e) {
            regularOutcome = ExecutionOutcome.exception(e);
        }

        boolean identical;
        try {
            log.debug("bizKey {}: compare regular and gray outcome", bizKey);
            identical = compareOutcome(regularOutcome, grayOutcome);
        } catch(Exception e) {
            log.error("bizKey {}: error occurs when comparing, fallback to regular outcome", bizKey, e);
            if (getMonitor() != null) {
                getMonitor().reportCompareSummary(bizKey, false, System.currentTimeMillis() - start);
            }
            return regularOutcome;
        }
        if (identical) {
            log.debug("bizKey {}: gray and regular outcome are identical, use gray outcome", bizKey);
            if (getMonitor() != null) {
                getMonitor().reportCompareSummary(bizKey, true, System.currentTimeMillis() - start);
            }
            return grayOutcome;
        }
        try {
            AdoptOnDiff adopt = GrayRuleManager.getMetadata(bizKey).getAdopt();
            ExecutionOutcome<R> outcome = adopt.take(regularOutcome, grayOutcome);
            if (outcome == null) {
                log.error("bizKey {}: adopt strategy return null! fallback to regular outcome", bizKey);
                return regularOutcome;
            }
            log.warn("bizKey {}: gray and regular outcome are not identical, request={}", bizKey, request);
            return outcome;
        } finally {
            if (getMonitor() != null) {
                getMonitor().reportCompareSummary(bizKey, false, System.currentTimeMillis() - start);
            }
        }
    }

    /**
     * 执行常规逻辑
     */
    protected abstract R runRegularLogic(P request) throws Exception;

    /**
     * 执行灰度逻辑
     */
    protected abstract R runGrayLogic(P request) throws Exception;

    /**
     * 比较灰度与常规逻辑执行输出
     */
    protected boolean compareOutcome(ExecutionOutcome<R> regularOutcome, ExecutionOutcome<R> grayOutcome) {
        if (regularOutcome.getException() != null) {
            return grayOutcome.getException() != null && regularOutcome.getException().getClass().equals(
                    grayOutcome.getException().getClass());
        }
        if (regularOutcome.getResult() != null) {
            return grayOutcome.getResult() != null && compareResult(regularOutcome.getResult(), grayOutcome.getResult());
        }
        return grayOutcome.getResult() == null && grayOutcome.getException() == null;
    }

    /**
     * <p>比较灰度与常规逻辑的返回结果
     * <p>注意: 该方法不能抛异常! 有异常则后续以常规逻辑的输出为准
     *
     * @param regularResult not null
     * @param grayResult    not null
     * @return true or false
     */
    protected boolean compareResult(R regularResult, R grayResult) {
        return regularResult.equals(grayResult);
    }
}
