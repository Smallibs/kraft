package io.smallibs.kraft.election

import io.smallibs.kraft.common.Identifier.Companion.id
import io.smallibs.kraft.common.Index.Companion.index
import io.smallibs.kraft.common.Term.Companion.term
import io.smallibs.kraft.election.data.Action.AppendResponse
import io.smallibs.kraft.election.data.Action.RequestAppend
import io.smallibs.kraft.election.data.Action.RequestVote
import io.smallibs.kraft.election.data.Action.TimeOut
import io.smallibs.kraft.election.data.Action.Voted
import io.smallibs.kraft.election.data.NodeKind.Elector
import io.smallibs.kraft.election.data.NodeKind.Follower
import io.smallibs.kraft.election.data.Reaction.AppendRequested
import io.smallibs.kraft.election.data.Reaction.ArmTimeout
import io.smallibs.kraft.election.data.TimoutType.Election
import kotlin.test.Test
import kotlin.test.assertEquals

class FollowerTransitionTest {

    @Test
    fun should_become_an_Elector_on_timeout() {
        Transition.run {
            Follower("A".id, 1.term, listOf("A".id, "B".id, "C".id), "B".id)
                .perform({ true }, TimeOut<Unit>(Election, 1.term))
        }.let {
            assertEquals(Elector("A".id, 1.term, listOf("A".id, "B".id, "C".id)), it.first)
            assertEquals(listOf(ArmTimeout(Election)), it.second)
        }
    }

    @Test
    fun should_stay_a_on_RequestVote() {
        Transition.run {
            Follower("A".id, 1.term, listOf("A".id, "B".id, "C".id), "B".id)
                .perform({ true }, RequestVote<Unit>("C".id, 1.term, 0.index to 0.term))
        }.let {
            assertEquals(Follower("A".id, 1.term, listOf("A".id, "B".id, "C".id), "B".id), it.first)
            assertEquals(listOf(), it.second)
        }
    }

    @Test
    fun should_become_an_Elector_on_Action_with_Younger_Term() {
        Transition.run {
            Follower("A".id, 1.term, listOf("A".id, "B".id), "B".id)
                .perform({ true }, RequestVote<Unit>("A".id, 2.term, 0.index to 1.term))
        }.let {
            assertEquals(Elector("A".id, 2.term, listOf("A".id, "B".id)), it.first)
            assertEquals(listOf(), it.second)
        }
    }

    @Test
    fun should_no_change_on_Action_with_Older_Term() {
        Transition.run {
            Follower("A".id, 1.term, listOf("A".id, "B".id), "B".id)
                .perform({ true }, RequestVote<Unit>("A".id, 1.term, 0.index to 1.term))
        }.let {
            assertEquals(Follower("A".id, 1.term, listOf("A".id, "B".id), "B".id), it.first)
            assertEquals(listOf(), it.second)
        }
    }

    @Test
    fun should_no_change_on_Action_with_Leader_Completeness_is_not_verified() {
        Transition.run {
            Follower("A".id, 1.term, listOf("A".id, "B".id), "B".id)
                .perform({ false }, Voted<Unit>("A".id, 1.term))
        }.let {
            assertEquals(Follower("A".id, 1.term, listOf("A".id, "B".id), "B".id), it.first)
            assertEquals(listOf(), it.second)
        }
    }

    @Test
    fun should_no_change_on_Action_with_RequestAppend_but_Accept_the_request() {
        Transition.run {
            Follower("A".id, 1.term, listOf("A".id, "B".id), "B".id)
                .perform({ true }, RequestAppend<Unit>("B".id, 1.term, 0.index to 1.term, 0.index))
        }.let {
            assertEquals(Follower("A".id, 1.term, listOf("A".id, "B".id), "B".id, true), it.first)
            assertEquals(
                listOf(AppendRequested(RequestAppend<Unit>("B".id, 1.term, 0.index to 1.term, 0.index))),
                it.second
            )
        }
    }

    @Test
    fun should_no_change_on_Action_with_AppendResponse() {
        Transition.run {
            Follower("A".id, 1.term, listOf("A".id, "B".id, "C".id), "C".id)
                .perform({ true }, AppendResponse<Unit>("B".id, 1.term, true, 1.index))
        }.let {
            assertEquals(Follower("A".id, 1.term, listOf("A".id, "B".id, "C".id), "C".id), it.first)
            assertEquals(listOf(), it.second)
        }
    }

    @Test
    fun should_become_a_new_follower_with_a_RequestAppend_of_younger_term() {
        Transition.run {
            Follower("A".id, 1.term, listOf("A".id, "B".id, "C".id), "C".id)
                .perform({ true }, RequestAppend<Unit>("B".id, 2.term, 0.index to 1.term, 0.index))
        }.let {
            assertEquals(Follower("A".id, 2.term, listOf("A".id, "B".id, "C".id), "B".id), it.first)
            assertEquals(listOf(), it.second)
        }
    }
}
