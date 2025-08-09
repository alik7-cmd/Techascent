package org.techascent.shared.data.cache

class DefaultCacheService<K, V>(
    maxSize: Int,
) : CacheService<K, V> {

    private val cache = LinkedHashMap<K, V>(maxSize, 0.75f)
    override fun get(key: K): V? = cache[key]

    override fun put(key: K, value: V) {
        cache[key] = value
    }

    override fun remove(key: K): V? = cache.remove(key)
    override fun clear() = cache.clear()
    override fun size(): Int = cache.size
    override fun containsKey(key: K) = cache.containsKey(key)
    override fun containsValue(value: V) = cache.containsValue(value)
}