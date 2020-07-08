package io.smallibs.kraft.election

import io.smallibs.kraft.common.Identifier.Companion.id
import io.smallibs.kraft.common.Index.Companion.index
import io.smallibs.kraft.common.Term.Companion.term
import io.smallibs.kraft.election.data.Action.*
import io.smallibs.kraft.election.data.NodeKind.Elector
import io.smallibs.kraft.election.data.NodeKind.Follower
import io.smallibs.kraft.election.data.Reaction.AppendRequested
import io.smallibs.kraft.election.data.Reaction.ArmElectionTimeout
import io.smallibs.kraft.election.data.TimoutType.Election
import org.junit.Test
import kotlin.test.assertEquals

class FollowerTransitionTest {

    @Test
    fun `Follower should become an Elector on timeout`() {
        Transition.run {
            Follower("A".id, 1.term, listOf("A".id, "B".id, "C".id), "B".id)
                .perform({ true }, TimeOut<Unit>(Election, 1.term))
        }.let {
            assertEquals(Elector("A".id, 1.term, listOf("A".id, "B".id, "C".id)), it.first)
            assertEquals(listOf(ArmElectionTimeout()), it.second)
        }
    }

    @Test
    fun `Follower should stay a Follower on RequestVote`() {
        Transition.run {
            Follower("A".id, 1.term, listOf("A".id, "B".id, "C".id), "B".id)
                .perform({ true }, RequestVote<Unit>("C".id, 1.term, 0.index to 0.term))
        }.let {
            assertEquals(Follower("A".id, 1.term, listOf("A".id, "B".id, "C".id), "B".id), it.first)
            assertEquals(listOf(), it.second)
        }
    }

    @Test
    fun `Follower should become an Elector on Action with Younger Term`() {
        Transition.run {
            Follower("A".id, 1.term, listOf("A".id, "B".id), "B".id)
                .perform({ true }, RequestVote<Unit>("A".id, 2.term, 0.index to 1.term))
        }.let {
            assertEquals(Elector("A".id, 2.term, listOf("A".id, "B".id)), it.first)
            assertEquals(listOf(), it.second)
        }
    }

    @Test
    fun `Follower should no change on Action with Older Term`() {
        Transition.run {
            Follower("A".id, 1.term, listOf("A".id, "B".id), "B".id)
                .perform({ true }, RequestVote<Unit>("A".id, 1.term, 0.index to 1.term))
        }.let {
            assertEquals(Follower("A".id, 1.term, listOf("A".id, "B".id), "B".id), it.first)
            assertEquals(listOf(), it.second)
        }
    }

    @Test
    fun `Follower should no change on Action with Leader Completeness is not verified`() {
        Transition.run {
            Follower("A".id, 1.term, listOf("A".id, "B".id), "B".id)
                .perform({ false }, Voted<Unit>("A".id, 1.term))
        }.let {
            assertEquals(Follower("A".id, 1.term, listOf("A".id, "B".id), "B".id), it.first)
            assertEquals(listOf(), it.second)
        }
    }

    @Test
    fun `Follower should no change on Action with RequestAppend but Accept the request`() {
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
    fun `Follower should no change on Action with AppendResponse`() {
        Transition.run {
            Follower("A".id, 1.term, listOf("A".id, "B".id, "C".id), "C".id)
                .perform({ true }, AppendResponse<Unit>("B".id, 1.term, true, 1.index))
        }.let {
            assertEquals(Follower("A".id, 1.term, listOf("A".id, "B".id, "C".id), "C".id), it.first)
            assertEquals(listOf(), it.second)
        }
    }

}