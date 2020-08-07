package io.smallibs.kraft.common

data class Identifier(val name: String) {

    companion object {
        val String.id get() = Identifier(this)
    }
}
