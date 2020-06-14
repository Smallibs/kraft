package io.smallibs.kraft.common

data class Term(private val value: Int = 0) {
    fun next() = Term(value + 1)
    operator fun compareTo(term: Term): Int = this.value.compareTo(term.value)
}