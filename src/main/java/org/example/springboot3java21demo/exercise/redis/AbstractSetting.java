package org.example.springboot3java21demo.exercise.redis;

public abstract class AbstractSetting {

    protected abstract String lookupCache(String key);

    protected abstract void putIntoCache(String key, String value);
}

