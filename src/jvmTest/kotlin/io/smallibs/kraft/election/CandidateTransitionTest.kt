package io.smallibs.kraft.election

import io.smallibs.kraft.common.Identifier.Companion.id
import io.smallibs.kraft.common.Index.Companion.index
import io.smallibs.kraft.common.Term.Companion.term
import io.smallibs.kraft.election.data.Action.*
import io.smallibs.kraft.election.data.NodeKind.*
import io.smallibs.kraft.election.data.Reaction.*
import io.smallibs.kraft.election.data.TimoutType
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class CandidateTransitionTest {

    @Test
    fun `Candidate should stay a Candidate on timeout`() {
        Transition.run {
            Candidate("A".id, 1.term, listOf("A".id, "B".id))
                .perform({ true }, TimeOut<Unit>(TimoutType.Election, 1.term))
        }.let {
            assertEquals(Candidate("A".id, 2.term, listOf("A".id, "B".id), listOf()), it.first)
            assertEquals(listOf(StartElection(), ArmElectionTimeout()), it.second)
        }
    }

    @Test
    fun `Candidate should become a Leader on Vote when one Node`() {
        Transition.run {
            Candidate("A".id, 1.term, listOf("A".id))
                .perform({ true }, Voted<Unit>("A".id, 1.term))
        }.let {
            assertEquals(Leader("A".id, 1.term, listOf("A".id)), it.first)
            assertEquals(listOf(InsertMarkInLog(), SynchroniseLog(), ArmHeartbeatTimeout()), it.second)
        }
    }

    @Test
    fun `Candidate should stay a Candidate on vote when two Nodes`() {
        Transition.run {
            Candidate("A".id, 1.term, listOf("A".id, "B".id))
                .perform({ true }, Voted<Unit>("A".id, 1.term))
        }.let {
            assertEquals(Candidate("A".id, 1.term, listOf("A".id, "B".id), listOf("A".id)), it.first)
            assertEquals(listOf(), it.second)
        }
    }

    @Test
    fun `Candidate should become a Leader on two Votes when three Nodes`() {
        Transition.run {
            Candidate("A".id, 1.term, listOf("A".id, "B".id, "C".id))
                .perform({ true }, Voted<Unit>("A".id, 1.term)).first
                .perform({ true }, Voted<Unit>("B".id, 1.term))
        }.let {
            assertEquals(Leader("A".id, 1.term, listOf("A".id, "B".id, "C".id)), it.first)
            assertEquals(listOf(InsertMarkInLog(), SynchroniseLog(), ArmHeartbeatTimeout()), it.second)
        }
    }

    @Test
    fun `Candidate should become an Elector on Action with Younger Term`() {
        Transition.run {
            Candidate("A".id, 1.term, listOf("A".id, "B".id, "C".id))
                .perform({ true }, RequestVote<Unit>("A".id, 2.term, 0.index to 1.term))
        }.let {
            assertEquals(Elector("A".id, 2.term, listOf("A".id, "B".id, "C".id)), it.first)
            assertEquals(listOf(), it.second)
        }
    }

    @Test
    fun `Candidate should no change on Action with Older Term`() {
        Transition.run {
            Candidate("A".id, 2.term, listOf("A".id, "B".id, "C".id))
                .perform({ true }, RequestVote<Unit>("A".id, 1.term, 0.index to 1.term))
        }.let {
            assertEquals(Candidate("A".id, 2.term, listOf("A".id, "B".id, "C".id)), it.first)
            assertEquals(listOf(), it.second)
        }
    }

    @Test
    fun `Candidate should no change on Action with Leader Completeness is not verified`() {
        Transition.run {
            Candidate("A".id, 1.term, listOf("A".id))
                .perform({ false }, Voted<Unit>("A".id, 1.term))
        }.let {
            assertEquals(Candidate("A".id, 1.term, listOf("A".id)), it.first)
            assertEquals(listOf(), it.second)
        }
    }

    @Test
    fun `Candidate should no change on Action with RequestAppend`() {
        Transition.run {
            Candidate("A".id, 1.term, listOf("A".id, "B".id, "C".id))
                .perform({ true }, RequestAppend<Unit>("B".id, 1.term, 0.index to 1.term, 0.index))
        }.let {
            assertEquals(Candidate("A".id, 1.term, listOf("A".id, "B".id, "C".id)), it.first)
            assertEquals(listOf(), it.second)
        }
    }

    @Test
    fun `Candidate should no change on Action with AppendResponse`() {
        Transition.run {
            Candidate("A".id, 1.term, listOf("A".id, "B".id, "C".id))
                .perform({ true }, AppendResponse<Unit>("B".id, 1.term, true, 1.index))
        }.let {
            assertEquals(Candidate("A".id, 1.term, listOf("A".id, "B".id, "C".id)), it.first)
            assertEquals(listOf(), it.second)
        }
    }

}