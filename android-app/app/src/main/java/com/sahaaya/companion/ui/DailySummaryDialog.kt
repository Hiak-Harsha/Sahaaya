package com.sahaaya.companion.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sahaaya.companion.core.PrioritizedVisit
import com.sahaaya.companion.ui.i18n.AppLanguage
import com.sahaaya.companion.ui.i18n.Strings

@Composable
fun DailySummaryDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    totalHouseholds: Int,
    completedVisits: List<String>,
    dangerList: List<PrioritizedVisit>,
    overdueCount: Int,
    language: AppLanguage,
    isDarkTheme: Boolean
) {
    if (!isOpen) return

    val context = LocalContext.current

    val dangerLines = if (dangerList.isEmpty()) {
        "None detected today (All stable)"
    } else {
        dangerList.joinToString("\n") {
            "• ${it.member.name} (${it.member.familyId}): ${it.member.dangerFlag}"
        }
    }

    val reportText = """
📋 SAHAAYA END-OF-DAY FIELD REPORT
=========================================
Date: 18 Oct 2026
ASHA Beat: Sub-Centre Health Post
Total Village Roster: $totalHouseholds households
Completed Visits Today: ${completedVisits.size}
Pending Overdue Follow-ups: $overdueCount

🚨 HIGH-ALERT EMERGENCY DANGER CASES (${dangerList.size}):
$dangerLines

Summary:
- All emergency beneficiaries flagged for immediate Medical Officer / ANM follow-up.
- Generated offline via Sahaaya Companion.
=========================================
    """.trimIndent()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(20.dp),
            color = if (isDarkTheme) SurfaceDark else Color.White,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Assignment,
                                contentDescription = "Summary",
                                tint = if (isDarkTheme) Color(0xFF022C28) else Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = Strings.dailySummaryHeader(language),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDarkTheme) TextPrimaryDark else SecondaryNavy,
                                    fontSize = 15.sp
                                )
                            )
                            Text(
                                text = "18 Oct 2026 • Sub-Centre Sync",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isDarkTheme) TextSecondaryDark else Color.Gray,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = if (isDarkTheme) TextSecondaryDark else Color.DarkGray
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = if (isDarkTheme) Color(0xFF24364D) else Color(0xFFE2E8F0)
                )

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Quick Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDarkTheme) SurfaceElevatedDark else Color(0xFFF1F5F9)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = Strings.visitedTodayLabel(language),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isDarkTheme) TextSecondaryDark else Color.Gray,
                                        fontSize = 10.sp
                                    )
                                )
                                Text(
                                    text = "${completedVisits.size} / $totalHouseholds",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal
                                    )
                                )
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDarkTheme) DangerRedDarkBg else DangerRedLight
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = Strings.dangerCasesLabel(language),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isDarkTheme) DangerRedDark else DangerRed,
                                        fontSize = 10.sp
                                    )
                                )
                                Text(
                                    text = "${dangerList.size} Cases",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDarkTheme) DangerRedDark else DangerRed
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Urgent Cases Alert Section
                    Text(
                        text = "🚨 Urgent Follow-ups for ANM / Doctor:",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkTheme) DangerRedDark else DangerRed
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    dangerList.forEach { visit ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .border(
                                    1.dp,
                                    if (isDarkTheme) DangerRedDark.copy(alpha = 0.5f) else DangerRed.copy(alpha = 0.3f),
                                    RoundedCornerShape(8.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDarkTheme) SurfaceElevatedDark else Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${visit.member.name} (${visit.member.familyId})",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDarkTheme) TextPrimaryDark else Color(0xFF0F172A)
                                        )
                                    )
                                    Text(
                                        text = visit.member.dangerFlag,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isDarkTheme) DangerRedDark else DangerRed,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }
                                SuggestionChip(
                                    onClick = { },
                                    label = { Text("URGENT", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = if (isDarkTheme) DangerRedDarkBg else DangerRedLight,
                                        labelColor = if (isDarkTheme) DangerRedDark else DangerRed
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Report Note
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDarkTheme) Color(0xFF132238) else Color(0xFFF8FAFC)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "💡 Frontline Sync Note: In remote areas lacking 4G/cellular data, this report can be transmitted immediately via basic SMS to the Sub-Centre Nurse-Midwife (ANM) or PHC Doctor.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = if (isDarkTheme) TextSecondaryDark else Color(0xFF475569),
                                lineHeight = 16.sp
                            ),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = if (isDarkTheme) Color(0xFF24364D) else Color(0xFFE2E8F0)
                )

                // Action Buttons: Copy & Send SMS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Daily Field Summary", reportText)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Summary copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(Strings.copySlip(language), fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                putExtra(Intent.EXTRA_TEXT, reportText)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Send Daily Report to ANM / Doctor")
                            context.startActivity(shareIntent)
                        },
                        modifier = Modifier.weight(1.3f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal,
                            contentColor = if (isDarkTheme) Color(0xFF022C28) else Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            Strings.sendToAnm(language),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
