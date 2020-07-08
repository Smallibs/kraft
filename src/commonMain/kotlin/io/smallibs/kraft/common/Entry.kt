package io.smallibs.kraft.common

import io.smallibs.kraft.common.Insert.Mark

class Entry<Command>(val term: Term, val value: Insert<Command> = Mark())