package com.sahaaya.companion.features.triage

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

enum class CriticalityLevel {
    RED_CRITICAL,         // Priority 1: Immediate life-threatening emergency, call 108
    AMBER_HIGH_PRIORITY,  // Priority 2: High clinical risk, visit within 24-48h
    GREEN_ROUTINE         // Priority 3: Routine protocol visit
}

data class CriticalityAlert(
    val id: String = UUID.randomUUID().toString(),
    val beneficiaryName: String,
    val householdId: String,
    val level: CriticalityLevel,
    val conditionTitle: String,
    val clinicalRationale: String,
    val actionRequired: String,
    val citedProtocol: String,
    val supervisorNotificationSent: Boolean = true,
    val timestamp: String = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
)

object ConditionCriticalityEngine {

    fun assessCondition(
        beneficiaryName: String,
        householdId: String,
        symptoms: String,
        riskFlags: String,
        isPregnant: Boolean,
        ageDays: Long,
        sbp: Int? = null,
        dbp: Int? = null,
        bloodSugar: Int? = null
    ): CriticalityAlert {
        val sym = symptoms.lowercase()
        val rsk = riskFlags.lowercase()

        // 1. RED_CRITICAL Checks (ESC Protocols)
        if (sym.contains("convulsion") || sym.contains("jhatke") || sym.contains("fits")) {
            return CriticalityAlert(
                beneficiaryName = beneficiaryName,
                householdId = householdId,
                level = CriticalityLevel.RED_CRITICAL,
                conditionTitle = "Convulsions / Eclampsia / Febrile Seizures",
                clinicalRationale = "Critical neurological emergency risking airway compromise and maternal/child mortality.",
                actionRequired = "Call 108 immediately. Place patient in recovery position, maintain airway, do not restrain.",
                citedProtocol = "ESC-5.1"
            )
        }

        if (sym.contains("unconscious") || sym.contains("behosh") || sym.contains("coma")) {
            return CriticalityAlert(
                beneficiaryName = beneficiaryName,
                householdId = householdId,
                level = CriticalityLevel.RED_CRITICAL,
                conditionTitle = "Unconsciousness / Diabetic Coma",
                clinicalRationale = "Severe metabolic collapse or acute cerebral crisis.",
                actionRequired = "Call 108 immediately. Give NOTHING by mouth. Immediate hospital transfer.",
                citedProtocol = "ESC-6.3"
            )
        }

        if (sym.contains("bleeding") || sym.contains("haemorrhage") || sym.contains("pad soaked")) {
            return CriticalityAlert(
                beneficiaryName = beneficiaryName,
                householdId = householdId,
                level = CriticalityLevel.RED_CRITICAL,
                conditionTitle = "Severe Haemorrhage (Antepartum / Postpartum)",
                clinicalRationale = "Acute obstetrical haemorrhage leading to hypovolaemic shock.",
                actionRequired = "Emergency 108 transfer to CEmONC / FRU. Uterine massage if postpartum.",
                citedProtocol = "ESC-3.1"
            )
        }

        if ((sbp != null && sbp >= 160) || (dbp != null && dbp >= 110)) {
            return CriticalityAlert(
                beneficiaryName = beneficiaryName,
                householdId = householdId,
                level = CriticalityLevel.RED_CRITICAL,
                conditionTitle = "Severe Hypertension (BP $sbp/$dbp mmHg)",
                clinicalRationale = "Imminent stroke or eclamptic seizure risk.",
                actionRequired = "Immediate referral to FRU / Hospital for IV antihypertensive therapy.",
                citedProtocol = "ESC-2.3"
            )
        }

        if (sym.contains("chest indrawing") || sym.contains("fast breathing") || sym.contains("pasli")) {
            return CriticalityAlert(
                beneficiaryName = beneficiaryName,
                householdId = householdId,
                level = CriticalityLevel.RED_CRITICAL,
                conditionTitle = "Severe Pneumonia / Respiratory Distress",
                clinicalRationale = "Hypoxemia and respiratory exhaustion in paediatric patient.",
                actionRequired = "Immediate referral to CHC / District Hospital with oxygen support.",
                citedProtocol = "ESC-5.2"
            )
        }

        if (sym.contains("hypothermia") || sym.contains("thanda") || sym.contains("cold to touch")) {
            return CriticalityAlert(
                beneficiaryName = beneficiaryName,
                householdId = householdId,
                level = CriticalityLevel.RED_CRITICAL,
                conditionTitle = "Severe Neonatal Hypothermia",
                clinicalRationale = "Rapid neonatal metabolic failure and death if unwarmed.",
                actionRequired = "Initiate continuous skin-to-skin Kangaroo Mother Care (KMC) and call 108.",
                citedProtocol = "ESC-4.3"
            )
        }

        // 2. AMBER_HIGH_PRIORITY Checks (VIS High Risk / Overdue)
        if (isPregnant && (rsk.contains("high_risk") || rsk.contains("caesarean") || (sbp != null && sbp >= 140) || (bloodSugar != null && bloodSugar >= 200))) {
            return CriticalityAlert(
                beneficiaryName = beneficiaryName,
                householdId = householdId,
                level = CriticalityLevel.AMBER_HIGH_PRIORITY,
                conditionTitle = "High-Risk Antenatal Pregnancy",
                clinicalRationale = "Maternal risk factors requiring physician evaluation and specialized ANC.",
                actionRequired = "Schedule ANM / Medical Officer visit at next PMSMA (9th of month) or PHC.",
                citedProtocol = "VIS-5.1"
            )
        }

        if (ageDays in 0..28 && (rsk.contains("lbw") || rsk.contains("preterm"))) {
            return CriticalityAlert(
                beneficiaryName = beneficiaryName,
                householdId = householdId,
                level = CriticalityLevel.AMBER_HIGH_PRIORITY,
                conditionTitle = "High-Risk Newborn (LBW / Preterm)",
                clinicalRationale = "Vulnerable neonate prone to hypothermia, sepsis, and feeding failure.",
                actionRequired = "Conduct daily/alternate day HBNC home visits; ensure exclusive breastfeeding and KMC.",
                citedProtocol = "HBNC-2.1"
            )
        }

        if (rsk.contains("overdue_vaccine") || rsk.contains("overdue")) {
            return CriticalityAlert(
                beneficiaryName = beneficiaryName,
                householdId = householdId,
                level = CriticalityLevel.AMBER_HIGH_PRIORITY,
                conditionTitle = "Overdue Immunization (> 28 Days Grace Period)",
                clinicalRationale = "Child susceptible to vaccine-preventable life-threatening infections.",
                actionRequired = "Mobilize family for immediate catch-up at nearest weekly Sub-Centre session.",
                citedProtocol = "VIS-1.3"
            )
        }

        // 3. GREEN_ROUTINE Default
        return CriticalityAlert(
            beneficiaryName = beneficiaryName,
            householdId = householdId,
            level = CriticalityLevel.GREEN_ROUTINE,
            conditionTitle = "Routine Maternal / Child Health Visit",
            clinicalRationale = "Standard universal healthcare protocol tracking.",
            actionRequired = "Conduct regular scheduled visit as per microplan.",
            citedProtocol = "VIS-4.1"
        )
    }
}

