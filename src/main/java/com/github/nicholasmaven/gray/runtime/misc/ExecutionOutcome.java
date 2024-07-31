package com.github.nicholasmaven.gray.runtime.misc;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * @author mawen
 */
@Getter
@EqualsAndHashCode
@ToString
public class ExecutionOutcome<R> {
    private final R result;

    private final Exception exception;

    private ExecutionOutcome(R result, Exception exception) {
        this.result = result;
        this.exception = exception;
    }
    
    public static <R> ExecutionOutcome<R> normal(R result) {
        return new ExecutionOutcome<>(result, null);
    }

    public static <R> ExecutionOutcome<R> exception(Exception e) {
        return new ExecutionOutcome<>(null, e);
    }
}
