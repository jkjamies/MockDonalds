package com.mockdonalds.app.core.test

abstract class StateRobot<State, Event> {
    private val _capturedEvents = mutableListOf<Event>()
    val capturedEvents: List<Event> get() = _capturedEvents.toList()
    val lastEvent: Event? get() = _capturedEvents.lastOrNull()

    protected fun createEventSink(): (Event) -> Unit = { event ->
        _capturedEvents.add(event)
    }

    fun clearEvents() {
        _capturedEvents.clear()
    }

    abstract fun defaultState(): State
}
