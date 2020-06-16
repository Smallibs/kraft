package io.smallibs.kraft.coordination.service

import io.smallibs.kraft.common.Identifier
import io.smallibs.kraft.common.Index
import io.smallibs.kraft.common.Term
import io.smallibs.kraft.election.data.Action
import io.smallibs.kraft.election.data.Action.AppendResponse
import io.smallibs.kraft.election.data.Timer

interface Connector<A> {

    fun insert(leader: Identifier, action: A)

    fun scheduleTimeOut(timer: Timer)

    fun requestVote(follower: Identifier, term: Term, lastLog: Pair<Index, Term>)

    fun acceptVote(candidate: Identifier, term: Term)

    fun appendEntries(follower: Identifier, append: Action.RequestAppend<A>)

    fun appendResult(leader: Identifier, appended: AppendResponse<A>)

}
