package com.sahaaya.companion.features.patient

import android.content.Context
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.util.UUID

/**
 * Thread-safe local repository managing patient documentation and observation images.
 * Keeps all data in private internal storage (`filesDir/patient_attachments`), offline-only.
 */
object PatientImageRepository {

    private val attachments = mutableListOf<PatientAttachment>()

    fun getAttachmentsForMember(memberId: String): List<PatientAttachment> {
        return synchronized(attachments) {
            attachments.filter { it.memberId == memberId }
        }
    }

    fun getAllAttachments(): List<PatientAttachment> {
        return synchronized(attachments) {
            attachments.toList()
        }
    }

    fun addAttachment(attachment: PatientAttachment) {
        synchronized(attachments) {
            attachments.add(attachment)
        }
    }

    /**
     * Saves a captured or picked bitmap into app private storage, compressing to JPEG.
     */
    fun saveBitmapAttachment(
        context: Context?,
        bitmap: Bitmap,
        memberId: String,
        householdId: String,
        description: String,
        category: AttachmentCategory
    ): PatientAttachment {
        val id = "ATT-${UUID.randomUUID().toString().take(8).uppercase()}"
        val filename = "${memberId}_${id}.jpg"

        val dir = if (context != null) {
            File(context.filesDir, "patient_attachments").apply { if (!exists()) mkdirs() }
        } else {
            File("patient_attachments").apply { if (!exists()) mkdirs() }
        }

        val file = File(dir, filename)
        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }
        } catch (e: Exception) {
            // Log fallback
        }

        val attachment = PatientAttachment(
            id = id,
            memberId = memberId,
            householdId = householdId,
            localFilePath = file.absolutePath,
            description = description,
            category = category,
            capturedAt = LocalDateTime.now(),
            fileSizeBytes = if (file.exists()) file.length() else 0L
        )

        addAttachment(attachment)
        return attachment
    }

    fun deleteAttachment(attachmentId: String): Boolean {
        synchronized(attachments) {
            val item = attachments.firstOrNull { it.id == attachmentId }
            if (item != null) {
                try {
                    val f = File(item.localFilePath)
                    if (f.exists()) f.delete()
                } catch (_: Exception) {}
                attachments.remove(item)
                return true
            }
            return false
        }
    }

    fun clearAll() {
        synchronized(attachments) {
            attachments.clear()
        }
    }
}
