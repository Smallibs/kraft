package io.smallibs.kraft.election

sealed class Timer
object Heartbeat : Timer()
object Election : Timer()