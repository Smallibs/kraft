package io.smallibs.kraft.coordination.service

import io.smallibs.kraft.common.Identifier
import io.smallibs.kraft.common.Index
import io.smallibs.kraft.common.Term
import io.smallibs.kraft.election.data.Action
import io.smallibs.kraft.election.data.Action.AppendResponse
import io.smallibs.kraft.election.data.TimoutType

/**
 * A connector provides functionalities linked with the external world.
 */
interface Connector< Command> {

    fun insert(leader: Identifier, action: Command)

    fun scheduleTimeOut(timoutType: TimoutType)

    fun requestVote(follower: Identifier, term: Term, lastLog: Pair<Index, Term>)

    fun acceptVote(candidate: Identifier, term: Term)

    fun appendEntries(follower: Identifier, append: Action.RequestAppend<Command>)

    fun appendResult(leader: Identifier, appended: AppendResponse<Command>)

}
