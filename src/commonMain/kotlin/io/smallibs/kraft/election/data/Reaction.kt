package io.smallibs.kraft.election.data

import io.smallibs.kraft.common.Identifier
import io.smallibs.kraft.election.data.Action.AppendResponse
import io.smallibs.kraft.election.data.Action.RequestAppend

sealed class Reaction<A> {

    class ArmElectionTimeout<A> : Reaction<A>() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            return true
        }

        override fun hashCode(): Int {
            return this::class.hashCode()
        }
    }

    class ArmHeartbeatTimeout<A> : Reaction<A>() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            return true
        }

        override fun hashCode(): Int {
            return this::class.hashCode()
        }
    }

    class StartElection<A> : Reaction<A>() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            return true
        }

        override fun hashCode(): Int {
            return this::class.hashCode()
        }
    }

    data class AcceptVote<A>(
            val candidate: Identifier
    ) : Reaction<A>()

    class SynchroniseLog<A> : Reaction<A>() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            return true
        }

        override fun hashCode(): Int {
            return this::class.hashCode()
        }
    }

    data class AppendRequested<A>(
            val requestAppend: RequestAppend<A>
    ) : Reaction<A>()

    data class AppendAccepted<A>(
            val appendResponse: AppendResponse<A>
    ) : Reaction<A>()

}