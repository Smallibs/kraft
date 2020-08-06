package io.smallibs.kraft.common

class Entry<Command>(val term: Term, val value: Insert<Command>)
