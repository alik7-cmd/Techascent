package org.techascent.shared.data.cache

interface CacheService<K, V> {
    fun get(key: K): V?
    fun put(key: K, value: V)
    fun containsKey(key: K): Boolean
    fun containsValue(value: V): Boolean
    fun remove(key: K): V?
    fun clear()
    fun size(): Int
}
