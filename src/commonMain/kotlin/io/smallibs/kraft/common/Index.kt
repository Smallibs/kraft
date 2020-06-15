package io.smallibs.kraft.common

import kotlin.math.max

class Index private constructor(val value: Int) {

    operator fun minus(v: Int) = this + (-v)
    operator fun plus(i: Int) = Index(value + i)
    operator fun compareTo(index: Index): Int = this.value.compareTo(index.value)

    companion object {
        fun Int.index() = Index(this)
        fun max(v: Index, w: Index) = max(v.value, w.value).index()
    }

}
