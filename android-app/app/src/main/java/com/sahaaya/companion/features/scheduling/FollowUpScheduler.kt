package com.sahaaya.companion.features.scheduling

import java.time.LocalDate
import java.util.UUID

data class FollowUpScheduleItem(
    val id: String = UUID.randomUUID().toString(),
    val beneficiaryName: String,
    val householdId: String,
    val visitType: String,
    val scheduledDate: LocalDate,
    val windowStart: LocalDate,
    val windowEnd: LocalDate,
    val clinicalPurpose: String,
    val citedProtocol: String,
    val isCompleted: Boolean = false
)

object FollowUpScheduler {

    fun generateAncSchedule(
        beneficiaryName: String,
        householdId: String,
        lmpDate: LocalDate
    ): List<FollowUpScheduleItem> {
        val ancWindows = listOf(
            Triple(1, 0L to 84L, "ANC-1: Registration, MCP Card, Baseline Vitals, Hb, Urine, Td-1, IFA (ANC-1.1, ANC-1.2)"),
            Triple(2, 98L to 182L, "ANC-2: 2nd Trimester, Ultrasound anomaly, Td-2, Abdominal exam, Calcium (ANC-1.2, ANC-2.3)"),
            Triple(3, 196L to 238L, "ANC-3: 3rd Trimester, Fundal height, Foetal heart rate, Pallor, Pre-eclampsia screening (ANC-1.2)"),
            Triple(4, 252L to 280L, "ANC-4: 36-40 Weeks, Foetal presentation, Birth preparedness, Institutional delivery plan (ANC-1.2)")
        )

        return ancWindows.map { (num, range, purpose) ->
            val (startDays, endDays) = range
            val winStart = lmpDate.plusDays(startDays)
            val winEnd = lmpDate.plusDays(endDays)
            val target = winStart.plusDays((endDays - startDays) / 2)
            FollowUpScheduleItem(
                beneficiaryName = beneficiaryName,
                householdId = householdId,
                visitType = "ANC Check-up #$num",
                scheduledDate = target,
                windowStart = winStart,
                windowEnd = winEnd,
                clinicalPurpose = purpose,
                citedProtocol = "ANC-1.2"
            )
        }
    }

    fun generateHbncSchedule(
        beneficiaryName: String,
        householdId: String,
        deliveryDate: LocalDate,
        isInstitutional: Boolean,
        isCaesarean: Boolean
    ): List<FollowUpScheduleItem> {
        val days = when {
            isCaesarean -> listOf(7L, 14L, 21L, 28L, 42L)
            isInstitutional -> listOf(3L, 7L, 14L, 21L, 28L, 42L)
            else -> listOf(1L, 3L, 7L, 14L, 21L, 28L, 42L)
        }

        return days.map { dayNum ->
            val visitDate = deliveryDate.plusDays(dayNum)
            FollowUpScheduleItem(
                beneficiaryName = beneficiaryName,
                householdId = householdId,
                visitType = "HBNC Visit Day $dayNum",
                scheduledDate = visitDate,
                windowStart = visitDate,
                windowEnd = visitDate.plusDays(1),
                clinicalPurpose = "Check cord, jaundice, breastfeeding, thermal protection (KMC), maternal pads & lochia (HBNC-1.1, HBNC-3.1).",
                citedProtocol = "HBNC-1.1"
            )
        }
    }

    fun scheduleDiarrhoeaFollowUp(
        beneficiaryName: String,
        householdId: String,
        today: LocalDate
    ): FollowUpScheduleItem {
        val revisitDate = today.plusDays(2)
        return FollowUpScheduleItem(
            beneficiaryName = beneficiaryName,
            householdId = householdId,
            visitType = "Diarrhoea Hydration & Zinc Follow-up",
            scheduledDate = revisitDate,
            windowStart = today.plusDays(2),
            windowEnd = today.plusDays(3),
            clinicalPurpose = "Assess hydration status, skin pinch, continuation of ORS and 14-day Zinc course (CHD-1.2, CHD-1.3).",
            citedProtocol = "CHD-1.3"
        )
    }

    fun scheduleNcdFollowUp(
        beneficiaryName: String,
        householdId: String,
        today: LocalDate,
        isUncontrolled: Boolean
    ): FollowUpScheduleItem {
        val days = if (isUncontrolled) 14L else 30L
        val schedDate = today.plusDays(days)
        return FollowUpScheduleItem(
            beneficiaryName = beneficiaryName,
            householdId = householdId,
            visitType = if (isUncontrolled) "Uncontrolled NCD High-Priority Re-check" else "Monthly NCD Routine Follow-up",
            scheduledDate = schedDate,
            windowStart = schedDate.minusDays(3),
            windowEnd = schedDate.plusDays(3),
            clinicalPurpose = if (isUncontrolled)
                "Recheck blood pressure and fasting glucose; review physician medication adherence (NCD-1.4)."
            else
                "Routine monthly BP/glucose surveillance, lifestyle counseling, and drug refill check (NCD-3.1).",
            citedProtocol = if (isUncontrolled) "NCD-1.4" else "NCD-3.1"
        )
    }
}

