package io.smallibs.kraft.coordination

import io.smallibs.kraft.coordination.impl.NodeManagerImpl
import io.smallibs.kraft.coordination.service.Connector
import io.smallibs.kraft.coordination.service.Executor
import io.smallibs.kraft.election.data.Action
import io.smallibs.kraft.election.data.Context
import io.smallibs.kraft.log.Log

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
 *
 */
interface NodeManager< Command> {

    /**
     * Method called when a new operation should be performed on the database.
     */
    fun insert(a: Command): NodeManager<Command>

    /**
     * Method called when an action has been received by the system.
     */
    fun accept(action: Action<Command>): NodeManager<Command>


    /**
     * Companion
     */
    companion object {

        operator fun <Command> invoke(connector: Connector<Command>, executor: Executor<Command>, context: Context, log: Log<Command>) =
                NodeManagerImpl(connector, executor, context, log)

    }

}