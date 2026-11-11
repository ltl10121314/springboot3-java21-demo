package org.example.practice.integration.redis.examples;

public abstract class AbstractSetting {

    protected abstract String lookupCache(String key);

    protected abstract void putIntoCache(String key, String value);
}

