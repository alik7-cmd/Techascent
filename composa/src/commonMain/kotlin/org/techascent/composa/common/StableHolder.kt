package org.techascent.composa.common

class StableHolder<T>(private val item: T){
    public operator fun component1(): T = item
}
