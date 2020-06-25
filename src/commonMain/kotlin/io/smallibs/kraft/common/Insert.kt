package io.smallibs.kraft.common

sealed class Insert<A> {

    class Mark<A> : Insert<A>() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            return true
        }

        override fun hashCode(): Int {
            return this::class.hashCode()
        }
    }

    data class Item<A>(val value: A) : Insert<A>()

}