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
import io.smallibs.kraft.election.data.NodeKind.Leader
import io.smallibs.kraft.election.data.Reaction
import io.smallibs.kraft.election.data.Reaction.ArmTimeout
import io.smallibs.kraft.election.data.Reaction.InsertMarkInLog
import io.smallibs.kraft.election.data.Reaction.StartElection
import io.smallibs.kraft.election.data.Reaction.SynchroniseLog
import io.smallibs.kraft.election.data.TimoutType.Election
import io.smallibs.kraft.election.data.TimoutType.Heartbeat
import kotlin.test.Test
import kotlin.test.assertEquals

class CandidateTransitionTest {

    @Test
    fun candidate_should_stay_a_on_timeout() {
        Transition.run {
            Candidate("A".id, 1.term, listOf("A".id, "B".id))
                .perform({ true }, TimeOut<Unit>(Election, 1.term))
        }.let {
            assertEquals(Candidate("A".id, 2.term, listOf("A".id, "B".id), listOf()), it.first)
            assertEquals(listOf(StartElection(), ArmTimeout(Election)), it.second)
        }
    }

    @Test
    fun should_become_a_Leader_on_Vote_when_one_Node() {
        Transition.run {
            Candidate("A".id, 1.term, listOf("A".id))
                .perform({ true }, Voted<Unit>("A".id, 1.term))
        }.let {
            assertEquals(Leader("A".id, 1.term, listOf("A".id)), it.first)
            assertEquals(listOf(InsertMarkInLog(), SynchroniseLog(), ArmTimeout(Heartbeat)), it.second)
        }
    }

    @Test
    fun should_stay_a_on_vote_when_two_Nodes() {
        Transition.run {
            Candidate("A".id, 1.term, listOf("A".id, "B".id))
                .perform({ true }, Voted<Unit>("A".id, 1.term))
        }.let {
            assertEquals(Candidate("A".id, 1.term, listOf("A".id, "B".id), listOf("A".id)), it.first)
            assertEquals(listOf(), it.second)
        }
    }

    @Test
    fun should_become_a_Leader_on_two_Votes_when_three_Nodes() {
        Transition.run {
            Candidate("A".id, 1.term, listOf("A".id, "B".id, "C".id))
                .perform({ true }, Voted<Unit>("A".id, 1.term)).first
                .perform({ true }, Voted<Unit>("B".id, 1.term))
        }.let {
            assertEquals(Leader("A".id, 1.term, listOf("A".id, "B".id, "C".id)), it.first)
            assertEquals(listOf(InsertMarkInLog(), SynchroniseLog(), ArmTimeout(Heartbeat)), it.second)
        }
    }

    @Test
    fun should_become_an_Elector_on_Action_with_younger_Term() {
        Transition.run {
            Candidate("A".id, 1.term, listOf("A".id, "B".id, "C".id))
                .perform({ true }, RequestVote<Unit>("A".id, 2.term, 0.index to 1.term))
        }.let {
            assertEquals(Elector("A".id, 2.term, listOf("A".id, "B".id, "C".id)), it.first)
            assertEquals(listOf(), it.second)
        }
    }

    @Test
    fun should_no_change_on_Action_with_older_Term() {
        Transition.run {
            Candidate("A".id, 2.term, listOf("A".id, "B".id, "C".id))
                .perform({ true }, RequestVote<Unit>("A".id, 1.term, 0.index to 1.term))
        }.let {
            assertEquals(Candidate("A".id, 2.term, listOf("A".id, "B".id, "C".id)), it.first)
            assertEquals(listOf(), it.second)
        }
    }

    @Test
    fun should_no_change_on_Action_with_Leader_Completeness_is_not_verified() {
        Transition.run {
            Candidate("A".id, 1.term, listOf("A".id))
                .perform({ false }, Voted<Unit>("A".id, 1.term))
        }.let {
            assertEquals(Candidate("A".id, 1.term, listOf("A".id)), it.first)
            assertEquals(listOf(), it.second)
        }
    }

    @Test
    fun should_no_change_on_Action_with_RequestAppend_and_older_term() {
        Transition.run {
            Candidate("A".id, 1.term, listOf("A".id, "B".id, "C".id))
                .perform({ true }, RequestAppend<Unit>("B".id, 0.term, 0.index to 1.term, 0.index))
        }.let {
            assertEquals(Candidate("A".id, 1.term, listOf("A".id, "B".id, "C".id)), it.first)
            assertEquals(listOf(), it.second)
        }
    }

    @Test
    fun should_no_change_on_Action_with_AppendResponse() {
        Transition.run {
            Candidate("A".id, 1.term, listOf("A".id, "B".id, "C".id))
                .perform({ true }, AppendResponse<Unit>("B".id, 1.term, true, 1.index))
        }.let {
            assertEquals(Candidate("A".id, 1.term, listOf("A".id, "B".id, "C".id)), it.first)
            assertEquals(listOf(), it.second)
        }
    }

    @Test
    fun should_become_a_follower_with_a_RequestAppend_of_younger_term() {
        Transition.run {
            Candidate("A".id, 1.term, listOf("A".id, "B".id, "C".id))
                .perform({ true }, RequestAppend<Unit>("B".id, 2.term, 0.index to 1.term, 0.index))
        }.let {
            assertEquals(Follower("A".id, 2.term, listOf("A".id, "B".id, "C".id), "B".id), it.first)
            assertEquals(listOf(), it.second)
        }
    }

    @Test
    fun should_become_a_follower_with_a_RequestAppend_of_equivalent_term() {
        Transition.run {
            Candidate("A".id, 1.term, listOf("A".id, "B".id, "C".id))
                .perform({ true }, RequestAppend<Unit>("B".id, 1.term, 0.index to 1.term, 0.index))
        }.let {
            assertEquals(Follower("A".id, 1.term, listOf("A".id, "B".id, "C".id), "B".id, true), it.first)
            assertEquals(
                listOf(
                    Reaction.AppendRequested(
                        RequestAppend(
                            "B".id,
                            1.term,
                            0.index to 1.term,
                            0.index,
                            listOf()
                        )
                    )
                ),
                it.second
            )
        }
    }
}
