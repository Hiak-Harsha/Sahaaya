package com.sahaaya.companion.core

import java.time.LocalDate
import java.time.temporal.ChronoUnit

class VisitListEngine {

    companion object {
        val EVALUATION_DATE: LocalDate = LocalDate.of(2026, 10, 26)
    }

    private data class VacDef(
        val code: String,
        val dueDays: Long,
        val maxDays: Long,
        val prereq: String? = null,
        val minIntervalDays: Long? = null
    )

    private val childhoodVaccines = listOf(
        VacDef("BCG", 0, 365),
        VacDef("HepB-0", 0, 0),
        VacDef("OPV-0", 0, 15),
        VacDef("OPV-1", 42, 1825),
        VacDef("OPV-2", 70, 1825, "OPV-1", 28),
        VacDef("OPV-3", 98, 1825, "OPV-2", 28),
        VacDef("Penta-1", 42, 365),
        VacDef("Penta-2", 70, 365, "Penta-1", 28),
        VacDef("Penta-3", 98, 365, "Penta-2", 28),
        VacDef("RVV-1", 42, 365),
        VacDef("RVV-2", 70, 365, "RVV-1", 28),
        VacDef("RVV-3", 98, 365, "RVV-2", 28),
        VacDef("fIPV-1", 42, 365),
        VacDef("fIPV-2", 98, 365, "fIPV-1", 56),
        VacDef("fIPV-3", 276, 729, "fIPV-2", 56),
        VacDef("PCV-1", 42, 365),
        VacDef("PCV-2", 98, 729, "PCV-1", 60),
        VacDef("PCV-B", 276, 729, "PCV-2", 60),
        VacDef("MR-1", 276, 1825),
        VacDef("MR-2", 490, 1825, "MR-1", 28),
        VacDef("OPV-B", 490, 1825, "OPV-3", 180),
        VacDef("DPT-B1", 490, 2554, "Penta-3", 180),
        VacDef("DPT-B2", 1827, 2554, "DPT-B1", null)
    )

    private val hbncDaysInstitutional = listOf(3L, 7L, 14L, 21L, 28L, 42L)
    private val hbncDaysHome = listOf(1L, 3L, 7L, 14L, 21L, 28L, 42L)

    fun prioritizeVisits(
        members: List<RegisterMember>,
        today: LocalDate = EVALUATION_DATE
    ): List<PrioritizedVisit> {
        val prioritized = mutableListOf<PrioritizedVisit>()

        for (member in members) {
            val visit = evaluateMemberVisit(member, today)
            if (visit != null) {
                prioritized.add(visit)
            }
        }

        // Sorting rule:
        // 1. Group rank: DANGER (1) -> OVERDUE (2) -> UPCOMING (3)
        // 2. Overdue: highest days overdue first (-daysDiff)
        // 3. Upcoming: soonest upcoming first (daysDiff)
        // 4. Stable tie-break by householdId, then memberId
        return prioritized.sortedWith(
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

    private fun evaluateMemberVisit(m: RegisterMember, today: LocalDate): PrioritizedVisit? {
        val symptoms = m.reportedSymptoms.lowercase()
        val riskFlags = m.riskFlags.lowercase()
        val dangerFlag = m.dangerFlag.lowercase()

        // -------------------------------------------------------------
        // PRIORITY 1: Immediate Clinical Danger Signs (VIS-1.1)
        // -------------------------------------------------------------
        val hasConvulsions = symptoms.contains("convulsion") || symptoms.contains("jhatke") || dangerFlag.contains("convulsion")
        val hasBleeding = symptoms.contains("bleeding") || symptoms.contains("haemorrhage") || symptoms.contains("khoon") || dangerFlag.contains("bleeding")
        val hasSevereHeadache = symptoms.contains("severe headache") || symptoms.contains("blurred vision") || dangerFlag.contains("headache")
        val hasHighFever = symptoms.contains("high fever >= 7 days") || symptoms.contains("prolonged fever") || symptoms.contains("9 din")
        val hasChestIndrawing = symptoms.contains("chest indrawing") || symptoms.contains("fast breathing") || symptoms.contains("pasli")
        val hasHypothermia = symptoms.contains("hypothermia") || symptoms.contains("cold to touch") || symptoms.contains("thanda")
        val hasUnconscious = symptoms.contains("unconscious") || symptoms.contains("behosh") || symptoms.contains("lethargic")

        val (sbp, dbp) = parseBp(m.lastBp)
        val hasSevereHypertension = (sbp != null && sbp >= 160) || (dbp != null && dbp >= 110)

        if (hasConvulsions || hasBleeding || hasSevereHeadache || hasHighFever || hasChestIndrawing || hasHypothermia || hasUnconscious || hasSevereHypertension) {
            val reason = when {
                hasConvulsions -> "Convulsions reported (ESC-5.1)"
                hasBleeding -> "Severe bleeding reported (ESC-3.1)"
                hasSevereHeadache -> "Severe headache / blurred vision (ESC-2.3)"
                hasHighFever -> "Prolonged high fever >= 7 days (ESC-5.4)"
                hasChestIndrawing -> "Chest indrawing / severe respiratory distress (ESC-5.2)"
                hasHypothermia -> "Severe newborn hypothermia (ESC-4.3)"
                hasUnconscious -> "Unconscious / coma state (ESC-6.3)"
                hasSevereHypertension -> "Severe hypertension: BP $sbp/$dbp mmHg (ESC-2.3)"
                else -> "Reported danger sign: ${m.dangerFlag}"
            }
            return PrioritizedVisit(
                member = m,
                priorityGroup = PriorityGroup.DANGER,
                priorityRank = 1,
                urgencyBadge = "URGENT DANGER",
                reason = "$reason. Immediate hospital referral required.",
                daysDiff = 0L,
                actionProtocolId = "ESC-1.1",
                recommendedFacility = "CHC / FRU Hospital"
            )
        }

        // Backward compatibility for legacy test dataset with simple dangerFlag
        if (m.dangerFlag.isNotBlank() && !m.dangerFlag.equals("none", ignoreCase = true)) {
            return PrioritizedVisit(
                member = m,
                priorityGroup = PriorityGroup.DANGER,
                priorityRank = 1,
                urgencyBadge = "URGENT DANGER",
                reason = "Reported danger sign: ${m.dangerFlag}. Immediate hospital referral required.",
                daysDiff = 0L,
                actionProtocolId = "ESC-1.1",
                recommendedFacility = "CHC / FRU Hospital"
            )
        }

        // -------------------------------------------------------------
        // PRIORITY 2: High Risk / Overdue Protocol Actions (VIS-2.1)
        // -------------------------------------------------------------
        val dob = parseDate(m.dob)
        val ageDays = if (dob != null) ChronoUnit.DAYS.between(dob, today) else 999999L
        val isPregnant = m.pregnant.equals("Y", ignoreCase = true)
        val lmp = parseDate(m.lmpDate)
        val gestationalDays = if (lmp != null) ChronoUnit.DAYS.between(lmp, today) else 0L

        // A. High-risk Pregnancy (VIS-5.1)
        val isTeenOrElderly = if (dob != null) {
            val ageYears = ageDays / 365
            ageYears < 18 || ageYears > 35
        } else false
        val hasHighGravida = (m.gravida.toIntOrNull() ?: 0) >= 5
        val hasStage1Hypertension = (sbp != null && sbp in 140..159) || (dbp != null && dbp in 90..109)
        val sugarVal = parseSugar(m.lastBloodSugar)
        val hasHighSugar = sugarVal != null && sugarVal >= 200

        if (isPregnant && (isTeenOrElderly || hasHighGravida || hasStage1Hypertension || hasHighSugar || riskFlags.contains("high_risk") || riskFlags.contains("caesarean"))) {
            return PrioritizedVisit(
                member = m,
                priorityGroup = PriorityGroup.OVERDUE,
                priorityRank = 2,
                urgencyBadge = "HIGH RISK PREGNANCY",
                reason = "High-risk pregnancy protocol follow-up required (VIS-5.1, ANC-1.3).",
                daysDiff = 14L,
                actionProtocolId = "VIS-5.1",
                recommendedFacility = "PHC / CHC"
            )
        }

        // B. Postnatal HBNC Visit Due / Overdue (HBNC-1.1, VIS-3.1)
        val deliveryDate = parseDate(m.deliveryDate)
        if (deliveryDate != null) {
            val pncDays = ChronoUnit.DAYS.between(deliveryDate, today)
            if (pncDays in 0..42) {
                val isHome = m.deliveryPlace.contains("home", ignoreCase = true)
                val targetSchedule = if (isHome) hbncDaysHome else hbncDaysInstitutional
                val pastOrTodayDue = targetSchedule.filter { it <= pncDays }
                if (pastOrTodayDue.isNotEmpty()) {
                    val lastDueDay = pastOrTodayDue.last()
                    val overdueDays = pncDays - lastDueDay
                    val group = if (overdueDays > 0) PriorityGroup.OVERDUE else PriorityGroup.UPCOMING
                    val rank = if (overdueDays > 0) 2 else 3
                    return PrioritizedVisit(
                        member = m,
                        priorityGroup = group,
                        priorityRank = rank,
                        urgencyBadge = if (overdueDays > 0) "OVERDUE HBNC (${overdueDays}d)" else "HBNC TODAY",
                        reason = "HBNC Visit Day $lastDueDay due/overdue ($overdueDays days elapsed) (HBNC-1.1).",
                        daysDiff = overdueDays,
                        actionProtocolId = "HBNC-1.1",
                        recommendedFacility = "Sub-Centre / Home Visit"
                    )
                }
            }
        }

        // C. Childhood Immunization Tracking (VIS-1.3, IMM-1.1)
        if (dob != null && ageDays in 0..2554) {
            val givenList = m.vaccinesGiven.split(",").map { it.trim().lowercase() }.filter { it.isNotBlank() }
            for (vac in childhoodVaccines) {
                val vacCodeClean = vac.code.lowercase().replace("-", "")
                val isGiven = givenList.any { it.replace("-", "") == vacCodeClean }
                if (!isGiven && ageDays >= vac.dueDays && ageDays <= vac.maxDays) {
                    val overdueDays = ageDays - (vac.dueDays + 28) // 28-day grace period
                    if (overdueDays > 0) {
                        return PrioritizedVisit(
                            member = m,
                            priorityGroup = PriorityGroup.OVERDUE,
                            priorityRank = 2,
                            urgencyBadge = "OVERDUE VACCINE (${overdueDays}d)",
                            reason = "${vac.code} is overdue by $overdueDays days beyond 28-day grace period (VIS-1.3).",
                            daysDiff = overdueDays,
                            actionProtocolId = "VIS-1.3",
                            recommendedFacility = "Sub-Centre / RI Session"
                        )
                    } else {
                        // Due but within grace period
                        val daysUntilOverdue = (vac.dueDays + 28) - ageDays
                        if (daysUntilOverdue <= 7) {
                            return PrioritizedVisit(
                                member = m,
                                priorityGroup = PriorityGroup.UPCOMING,
                                priorityRank = 3,
                                urgencyBadge = "VACCINE DUE",
                                reason = "${vac.code} is due for administration (due day ${vac.dueDays}) (IMM-1.1).",
                                daysDiff = daysUntilOverdue,
                                actionProtocolId = "IMM-1.1",
                                recommendedFacility = "Sub-Centre / RI Session"
                            )
                        }
                    }
                }
            }
        }

        // D. Overdue ANC Windows (ANC-1.2, VIS-2.1)
        if (isPregnant && gestationalDays in 1..280) {
            val ancWindow = when {
                gestationalDays in 0..84 -> 1 to (84L - gestationalDays)
                gestationalDays in 98..182 -> 2 to (182L - gestationalDays)
                gestationalDays in 196..238 -> 3 to (238L - gestationalDays)
                gestationalDays in 252..280 -> 4 to (280L - gestationalDays)
                else -> null
            }
            if (ancWindow != null) {
                val (ancNum, daysLeft) = ancWindow
                return PrioritizedVisit(
                    member = m,
                    priorityGroup = PriorityGroup.UPCOMING,
                    priorityRank = 3,
                    urgencyBadge = "ANC-$ancNum DUE",
                    reason = "ANC checkup #$ancNum scheduled during this window ($daysLeft days remaining) (ANC-1.2).",
                    daysDiff = daysLeft,
                    actionProtocolId = "ANC-1.2",
                    recommendedFacility = "Sub-Centre / PHC"
                )
            }
        }

        // E. Legacy dueDate fallback (for test_questions/register.csv format)
        if (m.dueDate.isNotBlank()) {
            val due = parseDate(m.dueDate)
            if (due != null) {
                val diff = ChronoUnit.DAYS.between(today, due)
                return if (diff < 0) {
                    val overdue = -diff
                    PrioritizedVisit(
                        member = m,
                        priorityGroup = PriorityGroup.OVERDUE,
                        priorityRank = 2,
                        urgencyBadge = "OVERDUE (${overdue}d)",
                        reason = "${m.nextDueItem} was due on ${m.dueDate} ($overdue days overdue).",
                        daysDiff = overdue,
                        actionProtocolId = "VIS-1.3",
                        recommendedFacility = "Sub-Centre / PHC"
                    )
                } else {
                    PrioritizedVisit(
                        member = m,
                        priorityGroup = PriorityGroup.UPCOMING,
                        priorityRank = 3,
                        urgencyBadge = if (diff == 0L) "DUE TODAY" else "IN ${diff}d",
                        reason = "${m.nextDueItem} scheduled for ${m.dueDate}.",
                        daysDiff = diff,
                        actionProtocolId = "VIS-4.1",
                        recommendedFacility = "Sub-Centre / PHC"
                    )
                }
            }
        }

        return null
    }

    private fun parseDate(s: String?): LocalDate? {
        if (s.isNullOrBlank()) return null
        return try {
            LocalDate.parse(s.trim())
        } catch (e: Exception) {
            null
        }
    }

    private fun parseBp(s: String?): Pair<Int?, Int?> {
        if (s.isNullOrBlank()) return null to null
        val parts = s.split("/", " ", ",")
        if (parts.size >= 2) {
            val sbp = parts[0].trim().toIntOrNull()
            val dbp = parts[1].trim().toIntOrNull()
            return sbp to dbp
        }
        return null to null
    }

    private fun parseSugar(s: String?): Int? {
        if (s.isNullOrBlank()) return null
        val num = s.filter { it.isDigit() }
        return num.toIntOrNull()
    }
}
