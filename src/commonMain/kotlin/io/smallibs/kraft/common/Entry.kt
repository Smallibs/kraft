package io.smallibs.kraft.common

import io.smallibs.kraft.common.Insert.Mark

class Entry<A>(val term: Term, val value: Insert<A> = Mark())