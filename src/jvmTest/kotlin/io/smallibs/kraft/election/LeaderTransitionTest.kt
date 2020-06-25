package io.smallibs.kraft.election

import io.smallibs.kraft.common.Identifier.Companion.id
import io.smallibs.kraft.common.Index.Companion.index
import io.smallibs.kraft.common.Term.Companion.term
import io.smallibs.kraft.election.data.Action.*
import io.smallibs.kraft.election.data.Node.Elector
import io.smallibs.kraft.election.data.Node.Leader
import io.smallibs.kraft.election.data.Reaction.AppendAccepted
import io.smallibs.kraft.election.data.Reaction.ArmElectionTimeout
import org.junit.Test
import kotlin.test.assertEquals

class LeaderTransitionTest {

    @Test
    fun `Leader should stay a Leader on Vote`() {
        Transition.run {
            Leader("A".id, 1.term, listOf("A".id))
                .perform({ true }, Voted<Unit>("A".id, 1.term))
        }.let {
            assertEquals(Leader("A".id, 1.term, listOf("A".id)), it.first)
            assertEquals(listOf(), it.second)
        }
    }

    @Test
    fun `Leader should stay a Leader on RequestVote`() {
        Transition.run {
            Leader("A".id, 1.term, listOf("A".id))
                .perform({ true }, RequestVote<Unit>("A".id, 1.term, 0.index to 0.term))
        }.let {
            assertEquals(Leader("A".id, 1.term, listOf("A".id)), it.first)
            assertEquals(listOf(), it.second)
        }
    }

    @Test
    fun `Leader should become an Elector on Action with Younger Term`() {
        Transition.run {
            Leader("A".id, 1.term, listOf("A".id, "B".id, "C".id))
                .perform({ true }, RequestVote<Unit>("A".id, 2.term, 0.index to 1.term))
        }.let {
            assertEquals(Elector("A".id, 2.term, listOf("A".id, "B".id, "C".id)), it.first)
            assertEquals(listOf(ArmElectionTimeout()), it.second)
        }
    }

    @Test
    fun `Leader should no change on Action with Older Term`() {
        Transition.run {
            Leader("A".id, 2.term, listOf("A".id, "B".id, "C".id))
                .perform({ true }, RequestVote<Unit>("A".id, 1.term, 0.index to 1.term))
        }.let {
            assertEquals(Leader("A".id, 2.term, listOf("A".id, "B".id, "C".id)), it.first)
            assertEquals(listOf(), it.second)
        }
    }

    @Test
    fun `Leader should no change on Action with Leader Completeness is not verified`() {
        Transition.run {
            Leader("A".id, 1.term, listOf("A".id))
                .perform({ false }, Voted<Unit>("A".id, 1.term))
        }.let {
            assertEquals(Leader("A".id, 1.term, listOf("A".id)), it.first)
            assertEquals(listOf(), it.second)
        }
    }

    @Test
    fun `Leader should no change on Action with RequestAppend`() {
        Transition.run {
            Leader("A".id, 1.term, listOf("A".id, "B".id, "C".id))
                .perform({ true }, RequestAppend<Unit>("B".id, 1.term, 0.index to 1.term, 0.index))
        }.let {
            assertEquals(Leader("A".id, 1.term, listOf("A".id, "B".id, "C".id)), it.first)
            assertEquals(listOf(), it.second)
        }
    }

    @Test
    fun `Leader should no change on Action with AppendResponse`() {
        Transition.run {
            Leader("A".id, 1.term, listOf("A".id, "B".id, "C".id))
                .perform({ true }, AppendResponse<Unit>("B".id, 1.term, true, 1.index))
        }.let {
            assertEquals(Leader("A".id, 1.term, listOf("A".id, "B".id, "C".id)), it.first)
            assertEquals(listOf(AppendAccepted(AppendResponse<Unit>("B".id, 1.term, true, 1.index))), it.second)
        }
    }

}