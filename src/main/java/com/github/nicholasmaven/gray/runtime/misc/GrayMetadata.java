package com.github.nicholasmaven.gray.runtime.misc;

import com.github.nicholasmaven.gray.runtime.diff.AdoptOnDiff;
import com.github.nicholasmaven.gray.runtime.filter.GrayFilter;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * @author mawen
 */
@Getter
@EqualsAndHashCode
public class GrayMetadata {
    private final String bizKey;

    private final GrayFilter filter;

    private final AdoptOnDiff adopt;

    public GrayMetadata(String bizKey, GrayFilter filter, AdoptOnDiff adopt) {
        this.bizKey = bizKey;
        this.filter = filter;
        this.adopt = adopt;
    }

}
