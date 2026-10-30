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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
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
import com.sahaaya.companion.ui.i18n.AppLanguage
import com.sahaaya.companion.ui.i18n.Strings

@Composable
fun ReferralSlipDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    patientName: String,
    dangerSign: String,
    category: String,
    citation: String,
    language: AppLanguage,
    isDarkTheme: Boolean
) {
    if (!isOpen) return

    val context = LocalContext.current
    val slipText = """
🚨 SAHAAYA EMERGENCY REFERRAL SLIP 🚨
=========================================
Beat: ASHA Sub-Centre Primary Health Post
Date: 18 Oct 2026
Beneficiary: $patientName
Category: ${category.uppercase()} EMERGENCY
Identified Danger Sign: $dangerSign

URGENT CLINICAL INSTRUCTIONS:
- Immediately escort patient to nearest First Referral Unit (FRU) / CHC Hospital.
- Call 108 Emergency Ambulance service.
- Keep patient warm, monitor airway and vitals.
- Do not administer oral solids if convulsing/unconscious.

Official Protocol Citation:
$citation
=========================================
Generated offline via Sahaaya Companion
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
                // Header Row
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
                                .background(if (isDarkTheme) DangerRedDark else DangerRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = "Emergency",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = Strings.referralSlipHeader(language),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDarkTheme) DangerRedDark else DangerRed,
                                    fontSize = 16.sp
                                )
                            )
                            Text(
                                text = Strings.referralSubcentre(language),
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

                // Slip Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Patient & Category Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.5.dp,
                                if (isDarkTheme) DangerRedDark else DangerRed,
                                RoundedCornerShape(12.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDarkTheme) DangerRedDarkBg else DangerRedLight
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = Strings.patientNameLabel(language),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isDarkTheme) TextSecondaryDark else Color.DarkGray
                                )
                            )
                            Text(
                                text = patientName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDarkTheme) TextPrimaryDark else Color(0xFF0F172A)
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${Strings.categoryLabel(language)} ${category.uppercase()}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDarkTheme) DangerRedDark else DangerRed
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Detected Danger Sign Section
                    Text(
                        text = Strings.dangerDetected(language),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkTheme) TextPrimaryDark else SecondaryNavy
                        )
                    )
                    Text(
                        text = dangerSign,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkTheme) DangerRedDark else DangerRed
                        ),
                        modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                    )

                    // Urgent Instructions
                    Text(
                        text = Strings.immediateAction(language),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (isDarkTheme) TextPrimaryDark else Color(0xFF1E293B),
                            lineHeight = 18.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Citation Box
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDarkTheme) SurfaceElevatedDark else Color(0xFFF1F5F9)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Clinical Protocol Citation:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = citation,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDarkTheme) TextPrimaryDark else SecondaryNavy
                                )
                            )
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = if (isDarkTheme) Color(0xFF24364D) else Color(0xFFE2E8F0)
                )

                // Actions: Share via SMS, Copy, Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Emergency Referral Slip", slipText)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Slip copied to clipboard", Toast.LENGTH_SHORT).show()
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
                                putExtra(Intent.EXTRA_TEXT, slipText)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Share Emergency Slip via SMS / 108")
                            context.startActivity(shareIntent)
                        },
                        modifier = Modifier.weight(1.3f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDarkTheme) DangerRedDark else DangerRed,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            Strings.shareSlipSms(language),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
