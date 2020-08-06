package io.smallibs.kraft.common

sealed class Insert<Command> {

    class Mark<Command> : Insert<Command>() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            return true
        }

        override fun hashCode(): Int {
            return this::class.hashCode()
        }
    }

    data class Item<Command>(val value: Command) : Insert<Command>()
}
