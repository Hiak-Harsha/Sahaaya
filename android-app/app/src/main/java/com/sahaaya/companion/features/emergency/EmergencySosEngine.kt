package com.sahaaya.companion.features.emergency

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.sahaaya.companion.features.facilities.FacilityDirectory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

data class EmergencySosEvent(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: String = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
    val beneficiaryName: String,
    val householdId: String,
    val category: String,
    val dangerSign: String,
    val citedProtocol: String,
    val recommendedFacility: String,
    val targetAmbulanceNumber: String = "108",
    val status: String = "DISPATCH_INITIATED"
)

object EmergencySosEngine {

    private val eventHistory = mutableListOf<EmergencySosEvent>()

    fun createEmergencyDialIntent(serviceNumber: String = "108"): Intent {
        return Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$serviceNumber")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    fun triggerEmergencySos(
        beneficiaryName: String,
        householdId: String,
        category: String,
        dangerSign: String,
        citedProtocol: String
    ): EmergencySosEvent {
        val rec = FacilityDirectory.recommendFacilityForCategory(category, isEmergency = true)
        val event = EmergencySosEvent(
            beneficiaryName = beneficiaryName,
            householdId = householdId,
            category = category,
            dangerSign = dangerSign,
            citedProtocol = citedProtocol,
            recommendedFacility = rec.primaryFacility.name,
            targetAmbulanceNumber = rec.primaryFacility.ambulancePhone
        )
        synchronized(eventHistory) {
            eventHistory.add(0, event)
        }
        return event
    }

    fun getSosHistory(): List<EmergencySosEvent> {
        return synchronized(eventHistory) {
            eventHistory.toList()
        }
    }

    fun formatEmergencySmsAlert(event: EmergencySosEvent): String {
        return """
            EMERGENCY 108 ALERT - SAHAAYA ESCALATION
            Beneficiary: ${event.beneficiaryName} (HH: ${event.householdId})
            Danger Sign: ${event.dangerSign}
            Protocol: ${event.citedProtocol}
            Target Facility: ${event.recommendedFacility}
            Time: ${event.timestamp}
            Action: Immediate 108 Ambulance Transport
        """.trimIndent()
    }
}

