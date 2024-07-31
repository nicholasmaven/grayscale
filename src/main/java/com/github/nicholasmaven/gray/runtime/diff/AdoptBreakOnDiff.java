package com.github.nicholasmaven.gray.runtime.diff;

import com.github.nicholasmaven.gray.runtime.misc.ExecutionOutcome;
import lombok.extern.slf4j.Slf4j;

/**
 * @author mawen
 */
@Slf4j
public class AdoptBreakOnDiff implements AdoptOnDiff {
    @Override
    public <R> ExecutionOutcome<R> take(ExecutionOutcome<R> regularOutcome, ExecutionOutcome<R> grayOutcome) throws CompareDiffException {
        log.info("adopt break");
        throw new CompareDiffException();
    }
}
