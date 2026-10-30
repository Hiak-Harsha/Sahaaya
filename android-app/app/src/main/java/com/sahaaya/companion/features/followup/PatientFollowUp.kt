package com.sahaaya.companion.features.followup

import java.time.LocalDate
import java.util.UUID

enum class RevisitOutcome {
    IMPROVED,
    UNCHANGED,
    WORSENED,
    NOT_AVAILABLE,
    REFERRED,
    FOLLOW_UP_INCOMPLETE
}

data class PatientFollowUp(
    val id: String,
    val memberId: String,
    val memberName: String,
    val householdId: String,
    val scheduledDate: LocalDate,
    val reason: String,
    val protocolReference: String,
    val responsibleWorker: String = "ASHA S. Devi",
    val isCompleted: Boolean = false,
    val completedDate: LocalDate? = null,
    val outcome: RevisitOutcome? = null,
    val outcomeNotes: String? = null,
    val isEmergencyEscalation: Boolean = false
)

object FollowUpRepository {

    private val followUps = mutableListOf<PatientFollowUp>()

    init {
        // Pre-populate realistic sample follow-ups for demonstrations
        followUps.add(
            PatientFollowUp(
                id = "FUP-101",
                memberId = "M-101",
                memberName = "Sunita Bai",
                householdId = "HH-101",
                scheduledDate = LocalDate.of(2026, 10, 20),
                reason = "HBNC Visit Day 7 checkup and maternal pad check",
                protocolReference = "HBNC-1.1",
                isCompleted = true,
                completedDate = LocalDate.of(2026, 10, 20),
                outcome = RevisitOutcome.IMPROVED,
                outcomeNotes = "Mother is healing well, infant feeding normally"
            )
        )
        followUps.add(
            PatientFollowUp(
                id = "FUP-102",
                memberId = "M-102",
                memberName = "Radha Sharma",
                householdId = "HH-102",
                scheduledDate = LocalDate.of(2026, 10, 28),
                reason = "ANC 4th visit checkup (36 weeks) and BP measurement",
                protocolReference = "ANC-1.2",
                isCompleted = false
            )
        )
    }

    fun getFollowUpsForMember(memberId: String): List<PatientFollowUp> {
        return synchronized(followUps) {
            followUps.filter { it.memberId == memberId }.sortedBy { it.scheduledDate }
        }
    }

    fun getAllFollowUps(): List<PatientFollowUp> {
        return synchronized(followUps) {
            followUps.toList()
        }
    }

    fun scheduleFollowUp(
        memberId: String,
        memberName: String,
        householdId: String,
        scheduledDate: LocalDate,
        reason: String,
        protocolReference: String,
        worker: String = "ASHA S. Devi"
    ): PatientFollowUp {
        val fup = PatientFollowUp(
            id = "FUP-${UUID.randomUUID().toString().take(6).uppercase()}",
            memberId = memberId,
            memberName = memberName,
            householdId = householdId,
            scheduledDate = scheduledDate,
            reason = reason,
            protocolReference = protocolReference,
            responsibleWorker = worker,
            isCompleted = false
        )
        synchronized(followUps) {
            followUps.add(fup)
        }
        return fup
    }

    fun recordOutcome(
        followUpId: String,
        outcome: RevisitOutcome,
        notes: String,
        completedDate: LocalDate = LocalDate.now()
    ): PatientFollowUp? {
        synchronized(followUps) {
            val idx = followUps.indexOfFirst { it.id == followUpId }
            if (idx != -1) {
                val current = followUps[idx]
                val isEmergency = outcome == RevisitOutcome.WORSENED || outcome == RevisitOutcome.REFERRED
                val updated = current.copy(
                    isCompleted = true,
                    completedDate = completedDate,
                    outcome = outcome,
                    outcomeNotes = notes,
                    isEmergencyEscalation = isEmergency
                )
                followUps[idx] = updated
                return updated
            }
            return null
        }
    }

    fun reschedule(followUpId: String, newDate: LocalDate): Boolean {
        synchronized(followUps) {
            val idx = followUps.indexOfFirst { it.id == followUpId }
            if (idx != -1) {
                followUps[idx] = followUps[idx].copy(scheduledDate = newDate)
                return true
            }
            return false
        }
    }
}
