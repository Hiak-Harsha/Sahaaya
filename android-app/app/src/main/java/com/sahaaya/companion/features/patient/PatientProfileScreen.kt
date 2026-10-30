package com.sahaaya.companion.features.patient

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.sahaaya.companion.core.RegisterMember
import com.sahaaya.companion.features.followup.FollowUpRepository
import com.sahaaya.companion.features.followup.PatientFollowUp
import com.sahaaya.companion.features.followup.RevisitOutcome
import com.sahaaya.companion.ui.PrimaryTeal
import com.sahaaya.companion.ui.PrimaryTealDarkTheme
import com.sahaaya.companion.ui.SurfaceDark
import com.sahaaya.companion.ui.SurfaceElevatedDark
import com.sahaaya.companion.ui.i18n.AppLanguage
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientProfileScreen(
    member: RegisterMember,
    isDarkTheme: Boolean,
    language: AppLanguage,
    onNavigateBack: () -> Unit,
    onNavigateToAsk: (String) -> Unit
) {
    val context = LocalContext.current
    var followUps by remember { mutableStateOf(FollowUpRepository.getFollowUpsForMember(member.memberId)) }
    var attachments by remember { mutableStateOf(PatientImageRepository.getAttachmentsForMember(member.memberId)) }

    var showOutcomeDialogFor by remember { mutableStateOf<PatientFollowUp?>(null) }
    var showScheduleDialog by remember { mutableStateOf(false) }
    var showAddAttachmentDialog by remember { mutableStateOf(false) }

    val primaryColor = if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal
    val bgColor = if (isDarkTheme) SurfaceDark else Color(0xFFF8FBFB)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = member.name,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Beneficiary ID: ${member.memberId} | Family: ${member.familyId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryColor
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Patient Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkTheme) SurfaceElevatedDark else Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(primaryColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = member.name,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (isDarkTheme) Color.White else Color.Black
                                )
                                Text(
                                    text = "Sex: ${member.sex} | Age/DOB: ${member.dob}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                            // Category Badge
                            val isPreg = member.pregnant.equals("Y", ignoreCase = true) || member.memberType.contains("PREGNANT", ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isPreg) Color(0xFFFCE4EC) else Color(0xFFE0F2F1)
                            ) {
                                Text(
                                    text = if (isPreg) "Pregnant (ANC)" else "Beneficiary",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isPreg) Color(0xFFC2185B) else Color(0xFF00796B),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Divider(color = Color.LightGray.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onNavigateToAsk("${member.name} ke liye protocol") },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.QuestionAnswer, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ask Protocol", style = MaterialTheme.typography.labelMedium)
                            }
                            OutlinedButton(
                                onClick = { showScheduleDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Schedule", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }

            // 2. Attached Documentation & Observations Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Attached Photos & Documents (${attachments.size})",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (isDarkTheme) Color.White else Color.Black
                    )
                    TextButton(onClick = { showAddAttachmentDialog = true }) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Attach Photo")
                    }
                }
            }

            if (attachments.isEmpty()) {
                item {
                    Text(
                        text = "No images or documents attached yet. Attach photographs of visible symptoms, MCP cards, or referral slips.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            } else {
                items(attachments) { att ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDarkTheme) SurfaceElevatedDark else Color.White
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                when (att.category) {
                                    AttachmentCategory.CLINICAL_OBSERVATION -> Icons.Default.CameraAlt
                                    AttachmentCategory.MEDICAL_DOCUMENT -> Icons.Default.Description
                                    AttachmentCategory.REFERRAL_SLIP -> Icons.Default.LocalHospital
                                },
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = att.description,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isDarkTheme) Color.White else Color.Black
                                )
                                Text(
                                    text = "${att.category.name.replace('_', ' ')} • ${att.capturedAt.toLocalDate()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                            IconButton(onClick = {
                                PatientImageRepository.deleteAttachment(att.id)
                                attachments = PatientImageRepository.getAttachmentsForMember(member.memberId)
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f))
                            }
                        }
                    }
                }
            }

            // 3. Follow-Ups & Revisit Workflow Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Visits & Follow-Up History (${followUps.size})",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (isDarkTheme) Color.White else Color.Black
                    )
                }
            }

            if (followUps.isEmpty()) {
                item {
                    Text(
                        text = "No scheduled follow-up visits recorded for this beneficiary.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            } else {
                items(followUps) { fup ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDarkTheme) SurfaceElevatedDark else Color.White
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Scheduled: ${fup.scheduledDate}",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = primaryColor
                                )
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (fup.isCompleted) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                                ) {
                                    Text(
                                        text = if (fup.isCompleted) "COMPLETED" else "PENDING",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (fup.isCompleted) Color(0xFF2E7D32) else Color(0xFFEF6C00),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = fup.reason,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isDarkTheme) Color.White else Color.Black
                            )
                            Text(
                                text = "Protocol Reference: ${fup.protocolReference}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )

                            if (fup.isCompleted && fup.outcome != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = when (fup.outcome) {
                                        RevisitOutcome.IMPROVED -> Color(0xFFE8F5E9)
                                        RevisitOutcome.UNCHANGED -> Color(0xFFFFFDE7)
                                        RevisitOutcome.WORSENED, RevisitOutcome.REFERRED -> Color(0xFFFFEBEE)
                                        else -> Color(0xFFECEFF1)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = "Revisit Outcome: ${fup.outcome.name.replace('_', ' ')}",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = when (fup.outcome) {
                                                RevisitOutcome.IMPROVED -> Color(0xFF1B5E20)
                                                RevisitOutcome.UNCHANGED -> Color(0xFFF57F17)
                                                RevisitOutcome.WORSENED, RevisitOutcome.REFERRED -> Color(0xFFB71C1C)
                                                else -> Color.DarkGray
                                            }
                                        )
                                        if (!fup.outcomeNotes.isNullOrBlank()) {
                                            Text(
                                                text = fup.outcomeNotes,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Black
                                            )
                                        }
                                        if (fup.isEmergencyEscalation) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "CRITICAL: Immediate referral required! Call 108.",
                                                color = Color.Red,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }
                                }
                            }

                            if (!fup.isCompleted) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { showOutcomeDialogFor = fup },
                                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Record Revisit Outcome", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog: Record Revisit Outcome
    showOutcomeDialogFor?.let { targetFup ->
        var selectedOutcome by remember { mutableStateOf(RevisitOutcome.IMPROVED) }
        var notes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showOutcomeDialogFor = null },
            title = { Text("Record Revisit Outcome", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Visit: ${targetFup.reason}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Select Patient Observation Outcome:", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))

                    RevisitOutcome.values().forEach { outcome ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedOutcome = outcome }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedOutcome == outcome,
                                onClick = { selectedOutcome = outcome }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = outcome.name.replace('_', ' '),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Clinical Notes / Follow-up Details") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        FollowUpRepository.recordOutcome(targetFup.id, selectedOutcome, notes)
                        followUps = FollowUpRepository.getFollowUpsForMember(member.memberId)
                        showOutcomeDialogFor = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text("Save Outcome")
                }
            },
            dismissButton = {
                TextButton(onClick = { showOutcomeDialogFor = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog: Schedule New Follow-Up
    if (showScheduleDialog) {
        var reason by remember { mutableStateOf("") }
        var protocolRef by remember { mutableStateOf("ANC-1.2") }
        var daysAhead by remember { mutableStateOf("7") }

        AlertDialog(
            onDismissRequest = { showScheduleDialog = false },
            title = { Text("Schedule Patient Follow-up", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Follow-up Reason") },
                        placeholder = { Text("e.g. Check blood pressure & IFA adherence") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = protocolRef,
                        onValueChange = { protocolRef = it },
                        label = { Text("Protocol Reference (e.g. ANC-1.2, CHD-1.1)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = daysAhead,
                        onValueChange = { daysAhead = it },
                        label = { Text("Days from today") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val days = daysAhead.toLongOrNull() ?: 7L
                        FollowUpRepository.scheduleFollowUp(
                            memberId = member.memberId,
                            memberName = member.name,
                            householdId = member.familyId,
                            scheduledDate = LocalDate.now().plusDays(days),
                            reason = reason.ifBlank { "Routine health checkup" },
                            protocolReference = protocolRef.ifBlank { "ANC-1.1" }
                        )
                        followUps = FollowUpRepository.getFollowUpsForMember(member.memberId)
                        showScheduleDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text("Schedule Visit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showScheduleDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog: Attach Photo / Document
    if (showAddAttachmentDialog) {
        var description by remember { mutableStateOf("") }
        var selectedCategory by remember { mutableStateOf(AttachmentCategory.CLINICAL_OBSERVATION) }

        AlertDialog(
            onDismissRequest = { showAddAttachmentDialog = false },
            title = { Text("Attach Photo / Document", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select Attachment Type:", fontWeight = FontWeight.SemiBold)
                    AttachmentCategory.values().forEach { cat ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedCategory = cat }
                        ) {
                            RadioButton(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(cat.name.replace('_', ' '), style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description / Observation Note") },
                        placeholder = { Text("e.g. Bilateral pedal oedema check / MCP card page") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Clinical Notice: Photographic records are for documentation and referral only. They are not automated diagnoses.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Generate simulated thumbnail bitmap for local attachment demonstration
                        val bitmap = Bitmap.createBitmap(120, 120, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(bitmap)
                        val paint = Paint().apply { color = android.graphics.Color.DKGRAY }
                        canvas.drawRect(0f, 0f, 120f, 120f, paint)

                        PatientImageRepository.saveBitmapAttachment(
                            context = context,
                            bitmap = bitmap,
                            memberId = member.memberId,
                            householdId = member.familyId,
                            description = description.ifBlank { "Patient record photo" },
                            category = selectedCategory
                        )
                        attachments = PatientImageRepository.getAttachmentsForMember(member.memberId)
                        showAddAttachmentDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text("Capture & Attach")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAttachmentDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
