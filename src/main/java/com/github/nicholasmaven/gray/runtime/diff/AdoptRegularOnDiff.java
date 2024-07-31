package com.github.nicholasmaven.gray.runtime.diff;

import com.github.nicholasmaven.gray.runtime.misc.ExecutionOutcome;
import lombok.extern.slf4j.Slf4j;

/**
 * @author mawen
 */
@Slf4j
public class AdoptRegularOnDiff implements AdoptOnDiff {
    @Override
    public <R> ExecutionOutcome<R> take(ExecutionOutcome<R> regularOutcome, ExecutionOutcome<R> grayOutcome) {
        log.info("adopt regular outcome: {}", regularOutcome);
        return regularOutcome;
    }
}
