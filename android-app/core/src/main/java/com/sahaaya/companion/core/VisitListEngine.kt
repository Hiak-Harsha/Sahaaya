package com.sahaaya.companion.core

import com.sahaaya.companion.core.eval.VisitPredictionRow
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class VisitListEngine {

    companion object {
        val EVALUATION_DATE: LocalDate = LocalDate.of(2026, 10, 26)
    }

    private data class VacDef(
        val code: String,
        val dueDays: Long,
        val notAfterDays: Long,
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

    private val ancWindows = mapOf(
        1 to (0L to 84L),
        2 to (98L to 182L),
        3 to (196L to 238L),
        4 to (252L to 280L)
    )

    private val hbncDaysInstitutional = listOf(3L, 7L, 14L, 21L, 28L, 42L)
    private val hbncDaysHome = listOf(1L, 3L, 7L, 14L, 21L, 28L, 42L)

    private fun itemKey(s: String): String = s.lowercase().replace(Regex("[^a-z0-9]"), "")

    fun isHighRisk(m: RegisterMember, today: LocalDate = EVALUATION_DATE): Boolean {
        val dob = parseDate(m.dob)
        val ageDays = if (dob != null) ChronoUnit.DAYS.between(dob, today) else 999999L

        // 1. Pregnant woman whose last recorded Hb < 7 g/dL or last ANC BP >= 140/90
        if (m.pregnant.equals("Y", ignoreCase = true)) {
            val hb = m.lastHbGdl.toDoubleOrNull() ?: 99.0
            val (sbp, dbp) = parseBp(m.lastAncBp)
            val bpHigh = (sbp != null && sbp >= 140) || (dbp != null && dbp >= 90)
            if (hb < 7.0 || bpHigh) return true
        }

        // 2. Baby <= 42 days old
        if (ageDays in 0..42) return true

        // 3. Child aged 184-1824 days whose last MUAC < 11.5 cm or bilateral pitting oedema == 'Y'
        if (ageDays in 184..1824) {
            val muac = m.lastMuacCm.toDoubleOrNull() ?: 99.0
            val oedema = m.bilateralPittingOedema.equals("Y", ignoreCase = true)
            if (muac < 11.5 || oedema) return true
        }

        // 4. NCD patient whose last recorded BP >= 140/90
        if (m.ncdCondition.isNotBlank()) {
            val (sbp, dbp) = parseBp(m.lastBpReading.ifBlank { m.lastBp })
            val bpHigh = (sbp != null && sbp >= 140) || (dbp != null && dbp >= 90)
            if (bpHigh) return true
        }

        return false
    }

    /**
     * Generates daily visit list strictly compliant with Chandrapuri Block VIS rules (09_VIS).
     * Returns deduplicated (member_id, due_item, priority) triples.
     */
    fun generateDailyVisitList(
        members: List<RegisterMember>,
        today: LocalDate = EVALUATION_DATE
    ): List<VisitPredictionRow> {
        val rows = mutableListOf<VisitPredictionRow>()
        val seenKeys = mutableSetOf<String>()

        fun addRow(memberId: String, dueItem: String, priority: Int) {
            val key = memberId.trim() + "|" + itemKey(dueItem)
            if (seenKeys.add(key)) {
                rows.add(VisitPredictionRow(memberId.trim(), dueItem, priority))
            }
        }

        for (m in members) {
            val mid = m.memberId.trim()
            if (mid.isBlank()) continue

            val dob = parseDate(m.dob)
            val ageDays = if (dob != null) ChronoUnit.DAYS.between(dob, today) else 999999L
            val isPreg = m.pregnant.equals("Y", ignoreCase = true)
            val lmp = parseDate(m.lmpDate)
            val ancDates = m.ancVisitDates.split(";").mapNotNull { parseDate(it) }

            // High risk determination (VIS-5.1)
            val hb = m.lastHbGdl.toDoubleOrNull() ?: 99.0
            val (sbpAnc, dbpAnc) = parseBp(m.lastAncBp)
            val ancBpHigh = (sbpAnc != null && sbpAnc >= 140) || (dbpAnc != null && dbpAnc >= 90)
            val pregHr = isPreg && (hb < 7.0 || ancBpHigh)

            val babyHr = ageDays in 0..42

            val muacVal = m.lastMuacCm.toDoubleOrNull() ?: 99.0
            val oedema = m.bilateralPittingOedema.equals("Y", ignoreCase = true)
            val childHr = (ageDays in 184..1824) && (muacVal < 11.5 || oedema)

            val (sbpNcd, dbpNcd) = parseBp(m.lastBpReading.ifBlank { m.lastBp })
            val ncdBpHigh = (sbpNcd != null && sbpNcd >= 140) || (dbpNcd != null && dbpNcd >= 90)
            val ncdHr = m.ncdCondition.isNotBlank() && ncdBpHigh

            val isHr = pregHr || babyHr || childHr || ncdHr

            fun evaluateItem(
                item: String,
                dueD: LocalDate?,
                isAnc: Boolean = false,
                ancClose: Long = 0L,
                isHbnc: Boolean = false
            ) {
                val status: String? = when {
                    isAnc -> {
                        val gest = if (lmp != null) ChronoUnit.DAYS.between(lmp, today) else 0L
                        if (gest > ancClose) "overdue" else "due_today"
                    }
                    isHbnc -> {
                        if (dueD == null) null
                        else if (dueD < today) "overdue"
                        else if (dueD == today) "due_today"
                        else if (dueD <= today.plusDays(6)) "due_this_week"
                        else null
                    }
                    else -> {
                        if (dueD == null) "overdue"
                        else if (today > dueD.plusDays(28)) "overdue"
                        else if (today >= dueD) "due_today"
                        else if (dueD <= today.plusDays(6)) "due_this_week"
                        else null
                    }
                }

                if (status != null) {
                    val priority = when {
                        isHr && (status == "overdue" || status == "due_today") -> 1
                        status == "overdue" || status == "due_today" -> 2
                        else -> 3
                    }
                    addRow(mid, item, priority)
                }
            }

            // VIS-2.3 High-Risk Pregnancy Follow-up
            if (pregHr) {
                addRow(mid, "HRP-followup", 1)
            }

            // VIS-2.1 ANC Check-ups
            if (isPreg && lmp != null) {
                val k = ancDates.size + 1
                if (k in ancWindows) {
                    val (openD, closeD) = ancWindows[k]!!
                    val gest = ChronoUnit.DAYS.between(lmp, today)
                    val dueD = lmp.plusDays(openD)
                    if (gest > closeD) {
                        evaluateItem("ANC-$k", dueD, isAnc = true, ancClose = closeD)
                    } else if (gest >= openD) {
                        evaluateItem("ANC-$k", dueD, isAnc = true, ancClose = closeD)
                    } else if (dueD <= today.plusDays(6)) {
                        addRow(mid, "ANC-$k", 3)
                    }
                }
            }

            // VIS-2.2 Td in Pregnancy
            if (isPreg) {
                val tdGiven = mutableMapOf<String, LocalDate>()
                for (x in m.tdDoses.split(";")) {
                    if (":" in x) {
                        val parts = x.split(":")
                        val dObj = parseDate(parts.getOrNull(1))
                        if (dObj != null) tdGiven[itemKey(parts[0])] = dObj
                    }
                }
                val isBooster = m.tdBoosterEligible.equals("Y", ignoreCase = true)
                if (isBooster) {
                    if ("tdbst" !in tdGiven && ancDates.isNotEmpty()) {
                        evaluateItem("Td-BST", ancDates[0])
                    }
                } else {
                    if ("td1" !in tdGiven) {
                        if (ancDates.isNotEmpty()) {
                            evaluateItem("Td-1", ancDates[0])
                        }
                    } else if ("td2" !in tdGiven) {
                        val td1Date = tdGiven["td1"]
                        if (td1Date != null) {
                            evaluateItem("Td-2", td1Date.plusDays(28))
                        }
                    }
                }
            }

            // VIS-4 Newborn HBNC
            if (dob != null && ageDays in 0..42) {
                val hbncDone = mutableSetOf<Long>()
                for (x in m.hbncVisitsDone.split(";")) {
                    val dClean = x.filter { it.isDigit() }.toLongOrNull()
                    if (dClean != null) hbncDone.add(dClean)
                }
                val isHome = m.placeOfDelivery.contains("home", ignoreCase = true)
                val sched = if (isHome) hbncDaysHome else hbncDaysInstitutional

                val pastOrToday = sched.filter { dob.plusDays(it - 1) <= today }
                if (pastOrToday.isNotEmpty() && pastOrToday.last() !in hbncDone) {
                    val targetD = pastOrToday.last()
                    val dueD = dob.plusDays(targetD - 1)
                    evaluateItem("HBNC-D$targetD", dueD, isHbnc = true)
                } else {
                    val future = sched.filter { dob.plusDays(it - 1) > today }
                    if (future.isNotEmpty() && future.first() !in hbncDone) {
                        val targetD = future.first()
                        val dueD = dob.plusDays(targetD - 1)
                        evaluateItem("HBNC-D$targetD", dueD, isHbnc = true)
                    }
                }
            }

            // VIS-3 Vaccines
            if (dob != null && ageDays in 0..2554) {
                val vacGiven = mutableMapOf<String, LocalDate>()
                for (x in m.childVaccines.split(";")) {
                    if (":" in x) {
                        val parts = x.split(":")
                        val dObj = parseDate(parts.getOrNull(1))
                        if (dObj != null) vacGiven[itemKey(parts[0])] = dObj
                    }
                }
                for (vac in childhoodVaccines) {
                    val cKey = itemKey(vac.code)
                    if (cKey in vacGiven) continue
                    if (vac.prereq != null && itemKey(vac.prereq) !in vacGiven) continue
                    if (ageDays > vac.notAfterDays) continue

                    var dueD = dob.plusDays(vac.dueDays)
                    if (vac.prereq != null && vac.minIntervalDays != null) {
                        val prevD = vacGiven[itemKey(vac.prereq)]
                        if (prevD != null) {
                            val minD = prevD.plusDays(vac.minIntervalDays)
                            if (minD > dueD) dueD = minD
                        }
                    }
                    if (ChronoUnit.DAYS.between(dob, dueD) > vac.notAfterDays) continue
                    evaluateItem(vac.code, dueD)
                }
            }

            // VIS-5.2 MUAC
            if (dob != null && ageDays in 178..1824) {
                val lastMuacD = parseDate(m.lastMuacDate)
                val dueD = if (lastMuacD != null) lastMuacD.plusDays(30) else dob.plusDays(184)
                evaluateItem("MUAC-check", dueD)
            }

            // VIS-5.3 NCD follow-up
            val ncd = m.ncdCondition.uppercase()
            if ("HTN" in ncd) {
                val lastBpD = parseDate(m.lastBpCheckDate)
                val dueD = if (lastBpD != null) lastBpD.plusDays(30) else null
                evaluateItem("BP-followup", dueD)
            }
            if ("DM" in ncd) {
                val lastSugarD = parseDate(m.lastSugarCheckDate)
                val dueD = if (lastSugarD != null) lastSugarD.plusDays(30) else null
                evaluateItem("Sugar-followup", dueD)
            }
        }

        return rows
    }

    /**
     * Produces PrioritizedVisit objects for frontline UI dashboard & route optimization.
     */
    fun prioritizeVisits(
        members: List<RegisterMember>,
        today: LocalDate = EVALUATION_DATE
    ): List<PrioritizedVisit> {
        val prioritized = mutableListOf<PrioritizedVisit>()

        // 1. Immediate Danger Sign Check (ASHA Immediate Referral Priority)
        for (m in members) {
            val symptoms = m.reportedSymptoms.lowercase()
            val dangerFlag = m.dangerFlag.lowercase()

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
                prioritized.add(
                    PrioritizedVisit(
                        member = m,
                        priorityGroup = PriorityGroup.DANGER,
                        priorityRank = 1,
                        urgencyBadge = "URGENT DANGER",
                        reason = "$reason. Immediate hospital referral required.",
                        daysDiff = 0L,
                        actionProtocolId = "ESC-1.1",
                        recommendedFacility = "CHC / FRU Hospital",
                        dueItem = "EMERGENCY-REFERRAL"
                    )
                )
            } else if (m.dangerFlag.isNotBlank() && !m.dangerFlag.equals("none", ignoreCase = true)) {
                prioritized.add(
                    PrioritizedVisit(
                        member = m,
                        priorityGroup = PriorityGroup.DANGER,
                        priorityRank = 1,
                        urgencyBadge = "URGENT DANGER",
                        reason = "Reported danger sign: ${m.dangerFlag}. Immediate hospital referral required.",
                        daysDiff = 0L,
                        actionProtocolId = "ESC-1.1",
                        recommendedFacility = "CHC / FRU Hospital",
                        dueItem = "EMERGENCY-REFERRAL"
                    )
                )
            }
        }

        // 2. Scheduled Visits Generated from Chandrapuri Block VIS Rules
        val dailyList = generateDailyVisitList(members, today)
        val memberMap = members.associateBy { it.memberId.trim() }

        for (pred in dailyList) {
            val m = memberMap[pred.member_id] ?: continue
            // Avoid duplicate priority 1 if emergency already added
            if (prioritized.any { it.member.memberId == m.memberId && it.priorityGroup == PriorityGroup.DANGER && it.dueItem == "EMERGENCY-REFERRAL" }) {
                continue
            }

            val group = when (pred.priority) {
                1 -> PriorityGroup.DANGER   // Priority 1: High Risk Overdue/Due Today
                2 -> PriorityGroup.OVERDUE  // Priority 2: Standard Overdue/Due Today
                else -> PriorityGroup.UPCOMING // Priority 3: Upcoming This Week
            }

            val badge = when (pred.priority) {
                1 -> "PRIORITY 1: HIGH RISK"
                2 -> "PRIORITY 2: DUE/OVERDUE"
                else -> "PRIORITY 3: UPCOMING"
            }

            val reason = "${pred.due_item} scheduled / required under protocol."

            prioritized.add(
                PrioritizedVisit(
                    member = m,
                    priorityGroup = group,
                    priorityRank = pred.priority,
                    urgencyBadge = badge,
                    reason = reason,
                    daysDiff = if (pred.priority == 3) 5L else 0L,
                    actionProtocolId = "VIS-1.1",
                    recommendedFacility = if (pred.priority == 1) "PHC / CHC" else "Sub-Centre / Home Visit",
                    dueItem = pred.due_item
                )
            )
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

    private fun parseDate(s: String?): LocalDate? {
        if (s.isNullOrBlank() || s.equals("nan", ignoreCase = true)) return null
        return try {
            LocalDate.parse(s.trim())
        } catch (e: Exception) {
            null
        }
    }

    private fun parseBp(s: String?): Pair<Int?, Int?> {
        if (s.isNullOrBlank() || s.equals("nan", ignoreCase = true)) return null to null
        val digits = Regex("\\d+").findAll(s).map { it.value.toInt() }.toList()
        if (digits.size >= 2) {
            return digits[0] to digits[1]
        }
        return null to null
    }
}
