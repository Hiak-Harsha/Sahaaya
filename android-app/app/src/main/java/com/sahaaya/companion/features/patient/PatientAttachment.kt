package com.sahaaya.companion.features.patient

import java.time.LocalDateTime

enum class AttachmentCategory {
    CLINICAL_OBSERVATION, // Wound, rash, or visible symptom observation
    MEDICAL_DOCUMENT,     // MCP card, prescription or lab report snapshot
    REFERRAL_SLIP         // Signed referral slip or transfer document
}

data class PatientAttachment(
    val id: String,
    val memberId: String,
    val householdId: String,
    val localFilePath: String,
    val description: String,
    val category: AttachmentCategory,
    val capturedAt: LocalDateTime = LocalDateTime.now(),
    val fileSizeBytes: Long = 0,
    val mimeType: String = "image/jpeg"
)
