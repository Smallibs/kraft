package io.smallibs.kraft.election.data

import io.smallibs.kraft.common.Identifier
import io.smallibs.kraft.election.data.Action.AppendResponse
import io.smallibs.kraft.election.data.Action.RequestAppend

sealed class Reaction<Command> {

    // ----------------------------------------------------------------------------
    // Reactions related to vote management
    // ----------------------------------------------------------------------------

    class ArmElectionTimeout<Command> : Reaction<Command>() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            return true
        }

        override fun hashCode(): Int {
            return this::class.hashCode()
        }
    }

    class ArmHeartbeatTimeout<Command> : Reaction<Command>() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            return true
        }

        override fun hashCode(): Int {
            return this::class.hashCode()
        }
    }

    class StartElection<Command> : Reaction<Command>() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            return true
        }

        override fun hashCode(): Int {
            return this::class.hashCode()
        }
    }

    data class AcceptVote<Command>(val candidate: Identifier) : Reaction<Command>()

    // ----------------------------------------------------------------------------
    // Reactions related to log management
    // ----------------------------------------------------------------------------

    class InsertMarkInLog<Command> : Reaction<Command>() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            return true
        }

        override fun hashCode(): Int {
            return this::class.hashCode()
        }
    }

    class SynchroniseLog<Command> : Reaction<Command>() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            return true
        }

        override fun hashCode(): Int {
            return this::class.hashCode()
        }
    }

    data class AppendRequested<Command>(val requestAppend: RequestAppend<Command>) : Reaction<Command>()

    data class AppendAccepted<Command>(val appendResponse: AppendResponse<Command>) : Reaction<Command>()

}