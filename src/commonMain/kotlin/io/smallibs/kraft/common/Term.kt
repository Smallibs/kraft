package io.smallibs.kraft.common

class Term private constructor(private val value: Int = 0) {
    fun next() = Term(value + 1)
    operator fun compareTo(term: Term): Int = this.value.compareTo(term.value)

    companion object {
        fun Int.term() = Term(this)
    }
}
