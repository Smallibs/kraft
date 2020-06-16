package io.smallibs.kraft.coordination

import io.smallibs.kraft.election.data.Action

/**
 * The node manager is in charge of the orchestration for the log manager and the node each time a request is
 * received (Insertion, Vote and Log).
 *
 * Note: This component is immutable.
 *
 * <pre>
 *
 *   Log
 *    ^
 *    |
 * LogManager       Node
 *    ^              ^
 *    |              |
 *    +------+-------+
 *           |
 *      NodeManager
 *           |
 *    +--------------+
 *    |              |
 *    v              v
 * Connector     Database
 *
 * </pre>
 */

interface NodeManager<A> {

    fun accept(action: Action<A>): NodeManager<A>

}