package com.github.nicholasmaven.gray.runtime.filter;

/**
 * @author mawen
 */
public interface GrayFilter {
    <P> boolean apply(P propertyHolder);
}
