package com.sahaaya.companion.features.facilities

enum class FacilityType {
    SUB_CENTRE,
    PHC,
    CHC_FRU,
    DISTRICT_HOSPITAL
}

data class HealthFacility(
    val id: String,
    val name: String,
    val type: FacilityType,
    val typeLabel: String,
    val distanceKm: Double,
    val contactPhone: String,
    val ambulancePhone: String = "108",
    val address: String,
    val services: List<String>,
    val availableDoctors: List<String>,
    val acceptsEmergency24x7: Boolean,
    val operationalHours: String
)

data class FacilityRecommendation(
    val primaryFacility: HealthFacility,
    val escalationLevel: String,
    val clinicalRationale: String,
    val recommendedTransport: String,
    val targetArrivalTime: String
)

object FacilityDirectory {

    private val facilities = listOf(
        HealthFacility(
            id = "FAC-CHC-01",
            name = "Chandrapuri Community Health Centre & FRU",
            type = FacilityType.CHC_FRU,
            typeLabel = "First Referral Unit (CHC / FRU)",
            distanceKm = 3.2,
            contactPhone = "0864-223344",
            ambulancePhone = "108",
            address = "Main Hospital Road, Chandrapuri Block HQ, AP",
            services = listOf(
                "24x7 Emergency Obstetric Care (CEmONC)",
                "Special Newborn Care Unit (SNCU)",
                "Blood Storage Unit",
                "Caesarean Delivery Operating Theatre",
                "24x7 Emergency Resuscitation & Ambulance Base"
            ),
            availableDoctors = listOf(
                "Dr. K. Srinivas (Medical Superintendent / Surgeon)",
                "Dr. P. Lalitha (Consultant Gynaecologist)",
                "Dr. V. Raman (Paediatrician)"
            ),
            acceptsEmergency24x7 = true,
            operationalHours = "24 Hours / 7 Days"
        ),
        HealthFacility(
            id = "FAC-PHC-01",
            name = "Chandrapuri Primary Health Centre",
            type = FacilityType.PHC,
            typeLabel = "Primary Health Centre (PHC)",
            distanceKm = 1.8,
            contactPhone = "0864-223311",
            ambulancePhone = "108",
            address = "Near Panchayat Office, Chandrapuri Village, AP",
            services = listOf(
                "Basic Emergency Obstetric Care (BEmONC)",
                "Normal Delivery Room (24x7)",
                "Outpatient Diagnosis & Treatment",
                "Routine Immunization Hub & Cold Chain",
                "NCD Screening & Essential Drugs Dispensary"
            ),
            availableDoctors = listOf(
                "Dr. M. Radha (Medical Officer MBBS)",
                "Staff Nurse Anasuya (Lady Health Visitor)"
            ),
            acceptsEmergency24x7 = true,
            operationalHours = "OPD: 9:00 AM - 4:00 PM; Emergency 24 Hours"
        ),
        HealthFacility(
            id = "FAC-SC-WEST",
            name = "Chandrapuri Sub-Centre (West)",
            type = FacilityType.SUB_CENTRE,
            typeLabel = "Health & Wellness Sub-Centre",
            distanceKm = 0.6,
            contactPhone = "0864-223301",
            ambulancePhone = "108",
            address = "West Ward Community Hall, Chandrapuri",
            services = listOf(
                "Weekly Village Health and Nutrition Day (VHND)",
                "Antenatal & Postnatal Mother Check-ups",
                "Universal Immunization Programme (UIP)",
                "IFA, Calcium, Albendazole & Zinc/ORS Dispensary",
                "Rapid Diagnostic Tests (Pregnancy, Blood Glucose, Haemoglobin)"
            ),
            availableDoctors = listOf(
                "ANM Nirmala Devi",
                "MPHW (Male) Venkatesh"
            ),
            acceptsEmergency24x7 = false,
            operationalHours = "9:00 AM - 5:00 PM (Monday to Saturday)"
        ),
        HealthFacility(
            id = "FAC-SC-EAST",
            name = "Chandrapuri Sub-Centre (East)",
            type = FacilityType.SUB_CENTRE,
            typeLabel = "Health & Wellness Sub-Centre",
            distanceKm = 1.1,
            contactPhone = "0864-223302",
            ambulancePhone = "108",
            address = "East Colony Primary School, Chandrapuri",
            services = listOf(
                "VHND Sessions",
                "Maternal & Child Health Tracking",
                "Routine Immunization Sessions",
                "Nutrition Screening & MUAC Triage",
                "First Aid & Emergency Referral Initiation"
            ),
            availableDoctors = listOf(
                "ANM S. Lakshmi",
                "Community Health Officer (CHO) Priya"
            ),
            acceptsEmergency24x7 = false,
            operationalHours = "9:00 AM - 5:00 PM (Monday to Saturday)"
        ),
        HealthFacility(
            id = "FAC-DH-01",
            name = "District Hospital Rajahmundry",
            type = FacilityType.DISTRICT_HOSPITAL,
            typeLabel = "Apex District Hospital",
            distanceKm = 24.5,
            contactPhone = "0864-220000",
            ambulancePhone = "108",
            address = "Collectorate Road, Rajahmundry, East Godavari, AP",
            services = listOf(
                "Tertiary Intensive Care Unit (ICU)",
                "Neonatal Intensive Care Unit (NICU - Level 3)",
                "Licensed Blood Bank with Component Separation",
                "Advanced Trauma & Surgical Centre",
                "Dialysis & Multi-Specialty Department"
            ),
            availableDoctors = listOf(
                "Dr. A. Sudhakar (Civil Surgeon / HOD Medicine)",
                "Dr. G. Hemalatha (HOD Obstetrics & Gynaecology)",
                "Dr. R. Chandra (Senior Paediatric Intensivist)"
            ),
            acceptsEmergency24x7 = true,
            operationalHours = "24 Hours / 7 Days"
        )
    )

    fun getAllFacilities(): List<HealthFacility> = facilities

    fun getNearestEmergencyFacility(): HealthFacility {
        return facilities.first { it.type == FacilityType.CHC_FRU }
    }

    fun recommendFacilityForCategory(
        category: String,
        isEmergency: Boolean
    ): FacilityRecommendation {
        val cat = category.uppercase()
        return when {
            isEmergency || cat.contains("EMERGENCY") || cat.contains("DANGER") || cat.contains("HAEMORRHAGE") || cat.contains("CONVULSION") -> {
                val fru = facilities.first { it.type == FacilityType.CHC_FRU }
                FacilityRecommendation(
                    primaryFacility = fru,
                    escalationLevel = "TIER-2 FIRST REFERRAL UNIT (CRITICAL)",
                    clinicalRationale = "Patient requires emergency stabilization, 24x7 surgical/obstetric cover, or SNCU access (ESC-1.1).",
                    recommendedTransport = "108 Emergency Ambulance (Immediately dispatched)",
                    targetArrivalTime = "Immediate (< 20 minutes)"
                )
            }
            cat.contains("LBW") || cat.contains("< 1.8") || cat.contains("SNCU") -> {
                val fru = facilities.first { it.type == FacilityType.CHC_FRU }
                FacilityRecommendation(
                    primaryFacility = fru,
                    escalationLevel = "SPECIAL NEWBORN CARE UNIT (SNCU)",
                    clinicalRationale = "Severe low birth weight or neonatal complication requires specialized incubator and phototherapy (ESC-4.4).",
                    recommendedTransport = "108 Ambulance with skin-to-skin KMC during transit",
                    targetArrivalTime = "Under 30 minutes"
                )
            }
            cat.contains("OUT_OF_SCOPE") || cat.contains("DOSAGE") || cat.contains("PRESCRIPTION") || cat.contains("NCD") || cat.contains("DIAGNOSIS") -> {
                val phc = facilities.first { it.type == FacilityType.PHC }
                FacilityRecommendation(
                    primaryFacility = phc,
                    escalationLevel = "PRIMARY HEALTH CENTRE (MEDICAL OFFICER)",
                    clinicalRationale = "Physician clinical consultation, prescription generation, or lab investigation required (SCP-1.2).",
                    recommendedTransport = "Local transit / 102 JSSK Patient Transport vehicle",
                    targetArrivalTime = "Same day OPD or next morning"
                )
            }
            else -> {
                val sc = facilities.first { it.type == FacilityType.SUB_CENTRE }
                FacilityRecommendation(
                    primaryFacility = sc,
                    escalationLevel = "HEALTH & WELLNESS SUB-CENTRE",
                    clinicalRationale = "Routine frontline health worker follow-up, ANC checkup, or childhood immunization session (VIS-4.1).",
                    recommendedTransport = "Foot / Local transit",
                    targetArrivalTime = "Scheduled session day"
                )
            }
        }
    }

    fun searchFacilities(query: String): List<HealthFacility> {
        val q = query.trim().lowercase()
        if (q.isBlank()) return facilities
        return facilities.filter {
            it.name.lowercase().contains(q) ||
            it.typeLabel.lowercase().contains(q) ||
            it.address.lowercase().contains(q) ||
            it.services.any { s -> s.lowercase().contains(q) } ||
            it.availableDoctors.any { d -> d.lowercase().contains(q) }
        }
    }
}

