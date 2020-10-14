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
import kotlin.test.Test
import kotlin.test.assertEquals

class ElectorTransitionTest {

    @Test
    fun should_become_a_Candidate_on_timeout() {
        Transition.run {
            Elector("A".id, 1.term, listOf("A".id, "B".id))
                .perform({ true }, TimeOut<Unit>(Election, 1.term))
        }.let {
            assertEquals(Candidate("A".id, 2.term, listOf("A".id, "B".id), listOf()), it.first)
            assertEquals(listOf(AcceptVote("A".id), StartElection(), ArmTimeout(Election)), it.second)
        }
    }

    @Test
    fun should_become_a_Follower_on_RequestVote() {
        Transition.run {
            Elector("A".id, 1.term, listOf("A".id, "B".id, "C".id))
                .perform({ true }, RequestVote<Unit>("B".id, 1.term, 0.index to 0.term))
        }.let {
            assertEquals(Follower("A".id, 1.term, listOf("A".id, "B".id, "C".id), "B".id, true), it.first)
            assertEquals(listOf(AcceptVote("B".id)), it.second)
        }
    }

    @Test
    fun should_stay_an_on_Action_with_Younger_Term() {
        Transition.run {
            Elector("A".id, 1.term, listOf("A".id, "B".id, "C".id))
                .perform({ true }, RequestVote<Unit>("A".id, 2.term, 0.index to 1.term))
        }.let {
            assertEquals(Elector("A".id, 2.term, listOf("A".id, "B".id, "C".id)), it.first)
            assertEquals(listOf(), it.second)
        }
    }

    @Test
    fun should_no_change_on_Action_with_Older_Term() {
        Transition.run {
            Elector("A".id, 2.term, listOf("A".id, "B".id, "C".id))
                .perform({ true }, RequestVote<Unit>("A".id, 1.term, 0.index to 1.term))
        }.let {
            assertEquals(Elector("A".id, 2.term, listOf("A".id, "B".id, "C".id)), it.first)
            assertEquals(listOf(), it.second)
        }
    }

    @Test
    fun should_no_change_on_Action_with_Leader_Completeness_is_not_verified() {
        Transition.run {
            Elector("A".id, 1.term, listOf("A".id))
                .perform({ false }, Voted<Unit>("A".id, 1.term))
        }.let {
            assertEquals(Elector("A".id, 1.term, listOf("A".id)), it.first)
            assertEquals(listOf(), it.second)
        }
    }

    @Test
    fun should_no_change_on_Action_with_RequestAppend() {
        Transition.run {
            Elector("A".id, 1.term, listOf("A".id, "B".id, "C".id))
                .perform({ true }, RequestAppend<Unit>("B".id, 1.term, 0.index to 1.term, 0.index))
        }.let {
            assertEquals(Elector("A".id, 1.term, listOf("A".id, "B".id, "C".id)), it.first)
            assertEquals(listOf(), it.second)
        }
    }

    @Test
    fun should_no_change_on_Action_with_AppendResponse() {
        Transition.run {
            Elector("A".id, 1.term, listOf("A".id, "B".id, "C".id))
                .perform({ true }, AppendResponse<Unit>("B".id, 1.term, true, 1.index))
        }.let {
            assertEquals(Elector("A".id, 1.term, listOf("A".id, "B".id, "C".id)), it.first)
            assertEquals(listOf(), it.second)
        }
    }

    @Test
    fun should_become_a_follower_with_a_RequestAppend_of_younger_term() {
        Transition.run {
            Elector("A".id, 1.term, listOf("A".id, "B".id, "C".id))
                .perform({ true }, RequestAppend<Unit>("B".id, 2.term, 0.index to 1.term, 0.index))
        }.let {
            assertEquals(Follower("A".id, 2.term, listOf("A".id, "B".id, "C".id), "B".id), it.first)
            assertEquals(listOf(), it.second)
        }
    }
}
