package com.github.nicholasmaven.gray.runtime.diff;

import com.github.nicholasmaven.gray.runtime.misc.ExecutionOutcome;

/**
 * @author mawen
 */
public interface AdoptOnDiff {

    /**
     * <pre>
     * ExecutionOutcome包含正常执行的返回结果和异常
     * 当不能确定返回时, 抛CompareDiffException
     * 注意: 方法返回非空!
     */
    <R> ExecutionOutcome<R> take(ExecutionOutcome<R> regularOutcome, ExecutionOutcome<R> grayOutcome) throws CompareDiffException;
}
