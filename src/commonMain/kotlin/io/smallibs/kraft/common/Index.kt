package io.smallibs.kraft.common

import kotlin.math.max
import kotlin.math.min

data class Index internal constructor(val value: Int) {

    operator fun minus(v: Int) = this + (-v)
    operator fun plus(i: Int) = Index(value + i)
    operator fun compareTo(index: Index): Int = this.value.compareTo(index.value)

    companion object {
        val Int.index get() = Index(this)
        fun max(v: Index, w: Index) = max(v.value, w.value).index
        fun min(v: Index, w: Index) = min(v.value, w.value).index
    }
}
