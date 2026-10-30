package com.sahaaya.companion.features.sync

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

enum class SyncStatus {
    PENDING,
    SYNCED,
    FAILED
}

data class SyncEvent(
    val id: String = UUID.randomUUID().toString(),
    val eventType: String,
    val entityId: String,
    val payloadJson: String,
    val createdAt: String = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
    var status: SyncStatus = SyncStatus.PENDING,
    var retryCount: Int = 0,
    var lastError: String? = null
)

object OfflineSyncQueue {

    private val queue = mutableListOf<SyncEvent>()

    fun recordVisitCompletion(memberId: String, memberName: String, notes: String): SyncEvent {
        val payload = """{"member_id":"$memberId","name":"$memberName","notes":"$notes","action":"VISIT_COMPLETED"}"""
        return enqueue("VISIT_COMPLETED", memberId, payload)
    }

    fun recordEmergencyReferral(memberId: String, dangerSign: String, facility: String): SyncEvent {
        val payload = """{"member_id":"$memberId","danger_sign":"$dangerSign","facility":"$facility","action":"EMERGENCY_REFERRAL"}"""
        return enqueue("EMERGENCY_REFERRAL", memberId, payload)
    }

    fun recordAdvisoryDelivery(memberId: String, protocolId: String, lang: String): SyncEvent {
        val payload = """{"member_id":"$memberId","protocol":"$protocolId","language":"$lang","action":"ADVISORY_SHARED"}"""
        return enqueue("ADVISORY_DELIVERY", memberId, payload)
    }

    fun record48hRevisitScheduled(memberId: String, memberName: String, notes: String = ""): SyncEvent {
        val payload = """{"member_id":"$memberId","name":"$memberName","action":"SCHEDULE_48H_REVISIT","notes":"$notes"}"""
        return enqueue("SCHEDULE_48H_REVISIT", memberId, payload)
    }

    fun enqueue(eventType: String, entityId: String, payloadJson: String): SyncEvent {
        val event = SyncEvent(
            eventType = eventType,
            entityId = entityId,
            payloadJson = payloadJson,
            status = SyncStatus.PENDING
        )
        synchronized(queue) {
            queue.add(event)
        }
        return event
    }

    fun getPendingEvents(): List<SyncEvent> {
        return synchronized(queue) {
            queue.filter { it.status == SyncStatus.PENDING }
        }
    }

    fun getAllEvents(): List<SyncEvent> {
        return synchronized(queue) {
            queue.toList()
        }
    }

    fun markSynced(eventId: String) {
        synchronized(queue) {
            queue.firstOrNull { it.id == eventId }?.status = SyncStatus.SYNCED
        }
    }

    fun simulateOnlineSync(): Int {
        var syncedCount = 0
        synchronized(queue) {
            for (ev in queue) {
                if (ev.status == SyncStatus.PENDING) {
                    ev.status = SyncStatus.SYNCED
                    syncedCount++
                }
            }
        }
        return syncedCount
    }
}

