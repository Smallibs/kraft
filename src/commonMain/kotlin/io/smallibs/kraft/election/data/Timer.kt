package io.smallibs.kraft.election.data

sealed class Timer {

    object Heartbeat : Timer()

    object Election : Timer()

}