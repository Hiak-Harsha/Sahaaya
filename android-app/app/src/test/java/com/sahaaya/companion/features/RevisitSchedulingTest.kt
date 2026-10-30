package com.sahaaya.companion.features

import com.sahaaya.companion.core.PrioritizedVisit
import com.sahaaya.companion.core.PriorityGroup
import com.sahaaya.companion.core.RegisterMember
import com.sahaaya.companion.features.reordering.DynamicVisitReorderEngine
import com.sahaaya.companion.features.scheduling.FollowUpScheduler
import com.sahaaya.companion.features.scheduling.RevisitManager
import com.sahaaya.companion.features.sync.OfflineSyncQueue
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class RevisitSchedulingTest {

    @Before
    fun setup() {
        RevisitManager.clear()
    }

    @Test
    fun testSchedule48hRevisitCreation() {
        val today = LocalDate.of(2026, 3, 30)
        val item = FollowUpScheduler.schedule48hRevisit("Sunita Devi", "HH-01", today)

        assertEquals("Sunita Devi", item.beneficiaryName)
        assertEquals("HH-01", item.householdId)
        assertEquals(LocalDate.of(2026, 4, 1), item.scheduledDate)
        assertEquals(today, item.windowStart)
        assertEquals(LocalDate.of(2026, 4, 1), item.windowEnd)
        assertTrue(item.visitType.contains("48-Hour"))
        assertTrue(item.citedProtocol.contains("HBNC-1.1"))
    }

    @Test
    fun testRevisitManagerToggleAndPersistence() {
        assertFalse(RevisitManager.getScheduledIds().contains("MEM-101"))

        val scheduled = RevisitManager.toggleScheduled(null, "MEM-101")
        assertTrue(scheduled)
        assertTrue(RevisitManager.getScheduledIds().contains("MEM-101"))

        val unScheduled = RevisitManager.toggleScheduled(null, "MEM-101")
        assertFalse(unScheduled)
        assertFalse(RevisitManager.getScheduledIds().contains("MEM-101"))
    }

    @Test
    fun testOfflineSyncQueueRecord48h() {
        val event = OfflineSyncQueue.record48hRevisitScheduled("MEM-202", "Pooja Sharma", "Severe dehydration follow-up")
        assertEquals("SCHEDULE_48H_REVISIT", event.eventType)
        assertEquals("MEM-202", event.entityId)
        assertTrue(event.payloadJson.contains("Pooja Sharma"))
        assertTrue(event.payloadJson.contains("Severe dehydration follow-up"))
    }

    @Test
    fun testDynamicVisitReorderPins48hToTop() {
        val m1 = RegisterMember(memberId = "M1", familyId = "HH-01", name = "Member One", memberType = "CHILD")
        val m2 = RegisterMember(memberId = "M2", familyId = "HH-02", name = "Member Two", memberType = "PREGNANT")
        val m3 = RegisterMember(memberId = "M3", familyId = "HH-03", name = "Member Three", memberType = "CHILD")

        val v1 = PrioritizedVisit(m1, PriorityGroup.DANGER, 1, "CRITICAL", "Danger fever", 0L)
        val v2 = PrioritizedVisit(m2, PriorityGroup.OVERDUE, 2, "OVERDUE", "Overdue ANC", 5L)
        val v3 = PrioritizedVisit(m3, PriorityGroup.UPCOMING, 3, "ROUTINE", "Routine visit", 3L)

        val list = listOf(v1, v2, v3)

        // Case 1: No overrides
        val regular = DynamicVisitReorderEngine.apply48hPriorityOverrides(list, emptySet())
        assertEquals("M1", regular[0].member.memberId)
        assertEquals("M2", regular[1].member.memberId)
        assertEquals("M3", regular[2].member.memberId)

        // Case 2: M3 is flagged for 48h re-visit -> M3 pinned to the absolute top
        val reordered = DynamicVisitReorderEngine.apply48hPriorityOverrides(list, setOf("M3"))
        assertEquals("M3", reordered[0].member.memberId)
        assertEquals("M1", reordered[1].member.memberId)
        assertEquals("M2", reordered[2].member.memberId)

        // Case 3: Multiple pinned -> both go to top in relative order
        val reorderedMulti = DynamicVisitReorderEngine.apply48hPriorityOverrides(list, setOf("M3", "M2"))
        assertEquals("M2", reorderedMulti[0].member.memberId)
        assertEquals("M3", reorderedMulti[1].member.memberId)
        assertEquals("M1", reorderedMulti[2].member.memberId)
    }
}
