package io.smallibs.kraft.election.data

import io.smallibs.kraft.common.Identifier
import io.smallibs.kraft.election.data.Action.AppendResponse
import io.smallibs.kraft.election.data.Action.RequestAppend

sealed class Reaction<A> {

    class ArmElectionTimeout<A> : Reaction<A>()

    class ArmHeartbeatTimeout<A> : Reaction<A>()

    class StartElection<A> : Reaction<A>()

    class AcceptVote<A>(
        val candidate: Identifier
    ) : Reaction<A>()

    class SynchroniseLog<A> : Reaction<A>()

    class AppendRequested<A>(
        val requestAppend: RequestAppend<A>
    ) : Reaction<A>()

    class AppendAccepted<A>(
        val appendResponse: AppendResponse<A>
    ) : Reaction<A>()

}