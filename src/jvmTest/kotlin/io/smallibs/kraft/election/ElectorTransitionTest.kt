package io.smallibs.kraft.election

import io.smallibs.kraft.common.Identifier.Companion.id
import io.smallibs.kraft.common.Index.Companion.index
import io.smallibs.kraft.common.Term.Companion.term
import io.smallibs.kraft.election.data.Action.AppendResponse
import io.smallibs.kraft.election.data.Action.RequestAppend
import io.smallibs.kraft.election.data.Action.RequestVote
import io.smallibs.kraft.election.data.Action.TimeOut
import io.smallibs.kraft.election.data.Action.Voted
import io.smallibs.kraft.election.data.NodeKind.Candidate
import io.smallibs.kraft.election.data.NodeKind.Elector
import io.smallibs.kraft.election.data.NodeKind.Follower
import io.smallibs.kraft.election.data.Reaction.AcceptVote
import io.smallibs.kraft.election.data.Reaction.ArmTimeout
import io.smallibs.kraft.election.data.Reaction.StartElection
import io.smallibs.kraft.election.data.TimoutType.Election
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ElectorTransitionTest {

    @Test
    fun `Elector become a Candidate on timeout`() {
        Transition.run {
            Elector("A".id, 1.term, listOf("A".id, "B".id))
                .perform({ true }, TimeOut<Unit>(Election, 1.term))
        }.let {
            assertEquals(Candidate("A".id, 2.term, listOf("A".id, "B".id), listOf()), it.first)
            assertEquals(listOf(AcceptVote("A".id), StartElection(), ArmTimeout(Election)), it.second)
        }
    }

    @Test
    fun `Elector should become a Follower on RequestVote`() {
        Transition.run {
            Elector("A".id, 1.term, listOf("A".id, "B".id, "C".id))
                .perform({ true }, RequestVote<Unit>("B".id, 1.term, 0.index to 0.term))
        }.let {
            assertEquals(Follower("A".id, 1.term, listOf("A".id, "B".id, "C".id), "B".id, true), it.first)
            assertEquals(listOf(AcceptVote("B".id)), it.second)
        }
    }

    @Test
    fun `Elector should stay an Elector on Action with Younger Term`() {
        Transition.run {
            Elector("A".id, 1.term, listOf("A".id, "B".id, "C".id))
                .perform({ true }, RequestVote<Unit>("A".id, 2.term, 0.index to 1.term))
        }.let {
            assertEquals(Elector("A".id, 2.term, listOf("A".id, "B".id, "C".id)), it.first)
            assertEquals(listOf(), it.second)
        }
    }

    @Test
    fun `Elector should no change on Action with Older Term`() {
        Transition.run {
            Elector("A".id, 2.term, listOf("A".id, "B".id, "C".id))
                .perform({ true }, RequestVote<Unit>("A".id, 1.term, 0.index to 1.term))
        }.let {
            assertEquals(Elector("A".id, 2.term, listOf("A".id, "B".id, "C".id)), it.first)
            assertEquals(listOf(), it.second)
        }
    }

    @Test
    fun `Elector should no change on Action with Leader Completeness is not verified`() {
        Transition.run {
            Elector("A".id, 1.term, listOf("A".id))
                .perform({ false }, Voted<Unit>("A".id, 1.term))
        }.let {
            assertEquals(Elector("A".id, 1.term, listOf("A".id)), it.first)
            assertEquals(listOf(), it.second)
        }
    }

    @Test
    fun `Elector should no change on Action with RequestAppend`() {
        Transition.run {
            Elector("A".id, 1.term, listOf("A".id, "B".id, "C".id))
                .perform({ true }, RequestAppend<Unit>("B".id, 1.term, 0.index to 1.term, 0.index))
        }.let {
            assertEquals(Elector("A".id, 1.term, listOf("A".id, "B".id, "C".id)), it.first)
            assertEquals(listOf(), it.second)
        }
    }

    @Test
    fun `Elector should no change on Action with AppendResponse`() {
        Transition.run {
            Elector("A".id, 1.term, listOf("A".id, "B".id, "C".id))
                .perform({ true }, AppendResponse<Unit>("B".id, 1.term, true, 1.index))
        }.let {
            assertEquals(Elector("A".id, 1.term, listOf("A".id, "B".id, "C".id)), it.first)
            assertEquals(listOf(), it.second)
        }
    }
}
