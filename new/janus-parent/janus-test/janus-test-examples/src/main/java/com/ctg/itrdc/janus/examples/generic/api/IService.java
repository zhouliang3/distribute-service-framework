package com.ctg.itrdc.janus.examples.generic.api;

public interface IService <P, V> {
    V get(P params);
}
