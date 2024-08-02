package com.github.nicholasmaven.gray.event;

import org.springframework.context.ApplicationEvent;

import java.util.Collections;
import java.util.Set;

/**
 * @author mawen
 */
public class GrayRuleChangeEvent extends ApplicationEvent {

    private final Set<String> keys;

    public GrayRuleChangeEvent(Set<String> keys) {
        this(keys, keys);
    }

    public GrayRuleChangeEvent(Object context, Set<String> keys) {
        super(context);
        this.keys = keys;
    }

    /**
     * @return The keys.
     */
    public Set<String> getKeys() {
        return Collections.unmodifiableSet(this.keys);
    }
}
