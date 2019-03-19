package com.ixyxj.io.filecache;

import java.util.HashMap;
import java.util.Map;

/**
 * For more information, you can visit https://github.com/xieyangxuejun,
 * or contact me by xieyangxuejun@gmail.com
 *
 * @author silen on 2019/3/20 0:57
 * Copyright (c) 2019 in FORETREE
 */
public class Cache<K, V> {
    private final Map<K, V> mCacheMap = new HashMap<>();
    private Builder<K, V> mBuilder;

    private Cache(Builder<K, V> builder) {
        this.mBuilder = builder;
    }

    interface Builder<K, V> {
        V build(K key);
    }

    public static <K, V>Cache<K, V> create(Builder<K, V> builder) {
        return new Cache<>(builder);
    }

    public V get(K key) {
        synchronized (mCacheMap) {
            if (mCacheMap.containsKey(key)) {
                return mCacheMap.get(key);
            }
        }
        V value = mBuilder.build(key);
        synchronized (mCacheMap) {
            mCacheMap.put(key, value);
        }
        return value;
    }
}
