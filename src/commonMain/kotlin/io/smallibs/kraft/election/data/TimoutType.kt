package io.smallibs.kraft.election.data

sealed class TimoutType {

    object Heartbeat : TimoutType()

    object Election : TimoutType()

}