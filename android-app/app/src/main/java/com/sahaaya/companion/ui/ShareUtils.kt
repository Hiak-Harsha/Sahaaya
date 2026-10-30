package com.sahaaya.companion.ui

import android.content.Context
import android.content.Intent
import android.net.Uri

object ShareUtils {

    /**
     * Dispatches text via SMS composer directly (ACTION_SENDTO smsto:),
     * falling back to standard ACTION_SEND system chooser if no SMS app is available.
     * Keeps SMS concise (< 300 characters for multipart 2G SMS delivery).
     * Does NOT require or use SEND_SMS permission.
     */
    fun Context.shareViaSmsOrChooser(text: String, chooserTitle: String = "Share via") {
        val conciseText = if (text.length > 300) {
            text.take(297) + "..."
        } else {
            text
        }

        val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:")).apply {
            putExtra("sms_body", conciseText)
            putExtra(Intent.EXTRA_TEXT, conciseText)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            startActivity(smsIntent)
        } catch (_: Exception) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, conciseText)
            }
            val chooser = Intent.createChooser(shareIntent, chooserTitle).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                startActivity(chooser)
            } catch (_: Exception) {
                // Device has no intent handler
            }
        }
    }
}
