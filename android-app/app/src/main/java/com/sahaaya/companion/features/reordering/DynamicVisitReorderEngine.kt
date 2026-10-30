package com.sahaaya.companion.features.reordering

import com.sahaaya.companion.core.PrioritizedVisit
import com.sahaaya.companion.core.PriorityGroup
import com.sahaaya.companion.core.RegisterMember
import com.sahaaya.companion.core.VisitListEngine
import java.time.LocalDate

object DynamicVisitReorderEngine {

    fun reorderVisitsOnNewEvent(
        currentList: List<PrioritizedVisit>,
        updatedMember: RegisterMember,
        newDangerSign: String? = null,
        today: LocalDate = VisitListEngine.EVALUATION_DATE
    ): List<PrioritizedVisit> {
        val memberToEvaluate = if (!newDangerSign.isNullOrBlank()) {
            updatedMember.copy(
                dangerFlag = newDangerSign,
                reportedSymptoms = newDangerSign
            )
        } else {
            updatedMember
        }

        // Re-evaluate the updated member using official rules
        val engine = VisitListEngine()
        val evaluatedNew = engine.prioritizeVisits(listOf(memberToEvaluate), today).firstOrNull()

        val mutableList = currentList.filter { it.member.memberId != updatedMember.memberId }.toMutableList()

        if (evaluatedNew != null) {
            if (evaluatedNew.priorityGroup == PriorityGroup.DANGER) {
                // Prepend emergency to the absolute top of the route
                mutableList.add(0, evaluatedNew)
            } else {
                mutableList.add(evaluatedNew)
                // Stable sort maintaining PriorityGroup order
                mutableList.sortWith(
                    compareBy<PrioritizedVisit> { it.priorityRank }
                        .thenBy { visit ->
                            when (visit.priorityGroup) {
                                PriorityGroup.OVERDUE -> -visit.daysDiff
                                PriorityGroup.UPCOMING -> visit.daysDiff
                                PriorityGroup.DANGER -> 0L
                            }
                        }
                        .thenBy { it.member.familyId }
                        .thenBy { it.member.memberId }
                )
            }
        }

        return mutableList
    }

    /**
     * Pins 48h-scheduled members to the absolute top of the route
     * (above DANGER-group rank ties, below nothing).
     */
    fun apply48hPriorityOverrides(
        visits: List<PrioritizedVisit>,
        scheduled48hMemberIds: Set<String>
    ): List<PrioritizedVisit> {
        if (scheduled48hMemberIds.isEmpty()) return visits
        val (pinned, normal) = visits.partition { scheduled48hMemberIds.contains(it.member.memberId) }
        return pinned + normal
    }
}

