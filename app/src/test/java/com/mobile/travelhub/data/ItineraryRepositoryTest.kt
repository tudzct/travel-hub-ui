package com.mobile.travelhub.data

import org.junit.Assert.assertEquals
import org.junit.Test

class ItineraryRepositoryTest {

    @Test
    fun reorderEvents_movesEventAndKeepsTimesOnPositions() {
        val repository = ItineraryRepository()
        val groupName = "Tokyo reorder drag"
        val beforeEvents = repository.observeWorkspace(groupName).value.days
            .first { it.dayIndex == 1 }
            .events

        repository.reorderEvents(
            groupName = groupName,
            dayIndex = 1,
            fromIndex = 0,
            toIndex = 2
        )

        val afterEvents = repository.observeWorkspace(groupName).value.days
            .first { it.dayIndex == 1 }
            .events

        assertEquals(listOf("d1-e2", "d1-e3", "d1-e1"), afterEvents.map { it.eventId })
        assertEquals(beforeEvents.map { it.startTime }, afterEvents.map { it.startTime })
        assertEquals(beforeEvents.map { it.endTime }, afterEvents.map { it.endTime })
    }

    @Test
    fun reorderEvent_swapsAdjacentEventsAndTimeSlots() {
        val repository = ItineraryRepository()
        val groupName = "Tokyo reorder buttons"
        val beforeEvents = repository.observeWorkspace(groupName).value.days
            .first { it.dayIndex == 1 }
            .events

        repository.reorderEvent(
            groupName = groupName,
            dayIndex = 1,
            eventId = "d1-e2",
            moveUp = true
        )

        val afterEvents = repository.observeWorkspace(groupName).value.days
            .first { it.dayIndex == 1 }
            .events

        assertEquals(listOf("d1-e2", "d1-e1", "d1-e3"), afterEvents.map { it.eventId })
        assertEquals(beforeEvents.map { it.startTime }, afterEvents.map { it.startTime })
        assertEquals(beforeEvents.map { it.endTime }, afterEvents.map { it.endTime })
    }
}
