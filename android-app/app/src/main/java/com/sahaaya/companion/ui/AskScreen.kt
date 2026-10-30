package com.sahaaya.companion.ui

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import com.sahaaya.companion.features.advisory.PatientAdvisoryEngine
import com.sahaaya.companion.features.emergency.EmergencySosEngine
import com.sahaaya.companion.features.facilities.FacilityDirectory
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sahaaya.companion.R
import com.sahaaya.companion.core.PipelineOutput
import com.sahaaya.companion.core.SahaayaPipeline
import com.sahaaya.companion.ui.i18n.AppLanguage
import com.sahaaya.companion.ui.i18n.Strings

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AskScreen(
    pipeline: SahaayaPipeline,
    language: AppLanguage,
    isDarkTheme: Boolean,
    initialQuery: String? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var queryText by remember { mutableStateOf(initialQuery ?: "") }
    var currentOutput by remember { mutableStateOf<PipelineOutput?>(null) }
    var showReferralSlip by remember { mutableStateOf(false) }

    // Offline Voice Recognizer Launcher
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText: String? =
                result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                queryText = spokenText
                currentOutput = pipeline.process(spokenText)
            }
        }
    }

    val startVoiceRecognition = {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, Strings.voicePrompt(language))
            val langTag = when (language) {
                AppLanguage.HINDI -> "hi-IN"
                AppLanguage.TELUGU -> "te-IN"
                AppLanguage.ENGLISH -> "en-IN"
            }
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, langTag)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, langTag)
        }
        try {
            speechRecognizerLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Voice recognition not available on device", Toast.LENGTH_SHORT).show()
        }
    }

    // Run pipeline if initial query provided
    LaunchedEffect(initialQuery) {
        if (!initialQuery.isNullOrBlank()) {
            queryText = initialQuery
            currentOutput = pipeline.process(initialQuery)
        }
    }

    val quickPrompts = Strings.quickPrompts(language)

    val logoRes = if (isDarkTheme) {
        R.drawable.ic_sahaaya_logo
    } else {
        R.drawable.ic_sahaaya_symbol_bright
    }

    val bgModifier = if (isDarkTheme) {
        Modifier.background(BackgroundDark)
    } else {
        Modifier.background(BackgroundLight)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .then(bgModifier)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = Strings.askHeader(language),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = if (isDarkTheme) TextPrimaryDark else SecondaryNavy
            )
        )
        Text(
            text = Strings.askSubheader(language),
            style = MaterialTheme.typography.bodySmall.copy(
                color = if (isDarkTheme) TextSecondaryDark else Color.Gray
            ),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Search Input Box
        OutlinedTextField(
            value = queryText,
            onValueChange = { queryText = it },
            placeholder = {
                Text(
                    Strings.searchPlaceholder(language),
                    fontSize = 13.sp,
                    color = if (isDarkTheme) TextSecondaryDark else Color.Gray
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    tint = if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = if (isDarkTheme) SurfaceDark else Color.White,
                unfocusedContainerColor = if (isDarkTheme) SurfaceDark else Color.White,
                focusedBorderColor = if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal,
                unfocusedBorderColor = if (isDarkTheme) Color(0xFF24364D) else Color(0xFFCBD5E1)
            ),
            minLines = 2,
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Primary Action Buttons Row (Search + Voice Mic)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    if (queryText.isNotBlank()) {
                        currentOutput = pipeline.process(queryText)
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal,
                    contentColor = if (isDarkTheme) Color(0xFF022C28) else Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Search, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    Strings.searchButton(language),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            FilledTonalIconButton(
                onClick = startVoiceRecognition,
                modifier = Modifier
                    .size(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = if (isDarkTheme) SurfaceElevatedDark else Color(0xFFE0F2F1),
                    contentColor = if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal
                )
            ) {
                Icon(
                    Icons.Default.Mic,
                    contentDescription = Strings.voiceButtonDesc(language)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Sample Chips
        Text(
            text = Strings.quickSamplesLabel(language),
            style = MaterialTheme.typography.labelMedium.copy(
                color = if (isDarkTheme) TextSecondaryDark else Color.DarkGray,
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.padding(bottom = 6.dp)
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            quickPrompts.forEach { prompt ->
                SuggestionChip(
                    onClick = {
                        queryText = prompt
                        currentOutput = pipeline.process(prompt)
                    },
                    label = { Text(prompt, fontSize = 11.sp, maxLines = 1) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = if (isDarkTheme) SurfaceElevatedDark else Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Initial welcome state
        if (currentOutput == null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkTheme) SurfaceDark else Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (isDarkTheme) Color(0xFF041830) else Color(0xFFF1F5F9))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = logoRes),
                            contentDescription = "Sahaaya Logo",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        Strings.welcomeTitle(language),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isDarkTheme) TextPrimaryDark else SecondaryNavy
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        Strings.welcomeDesc(language),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDarkTheme) TextSecondaryDark else Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }

        // Output Display Area
        currentOutput?.let { output ->
            when (output) {
                is PipelineOutput.DangerEscalation -> {
                    // Bright Red Card for Emergency Escalation
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, if (isDarkTheme) DangerRedDark else DangerRed, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDarkTheme) DangerRedDarkBg else DangerRedLight
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = "Danger",
                                    tint = if (isDarkTheme) DangerRedDark else DangerRed,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isDarkTheme) DangerRedDark else DangerRed)
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = Strings.emergencyTitle(language),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "${Strings.dangerDetected(language)} ${output.rule.dangerSign}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDarkTheme) DangerRedDark else DangerRed
                                )
                            )
                            Text(
                                text = "${Strings.categoryLabel(language)} ${output.rule.category.uppercase()}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = if (isDarkTheme) TextSecondaryDark else Color.DarkGray
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = Strings.immediateAction(language),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = if (isDarkTheme) TextPrimaryDark else Color(0xFF0F172A)
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = DangerRed.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = output.citation,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDarkTheme) TextPrimaryDark else SecondaryNavy
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            // Recommended emergency healthcare facility
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isDarkTheme) Color(0xFF3B1214) else Color(0xFFFFEBEE))
                                    .padding(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.LocalHospital,
                                        contentDescription = null,
                                        tint = if (isDarkTheme) DangerRedDark else DangerRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Destination: ${output.recommendedFacility}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDarkTheme) DangerRedDark else DangerRed
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Action buttons: 108 Call & Referral Slip
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val dialIntent = EmergencySosEngine.createEmergencyDialIntent("108")
                                        try {
                                            context.startActivity(dialIntent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Dialer unavailable", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFB71C1C),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Phone,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "CALL 108",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }

                                Button(
                                    onClick = { showReferralSlip = true },
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .height(44.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isDarkTheme) DangerRedDark else DangerRed,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        Icons.Default.LocalHospital,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = Strings.generateReferralSlip(language),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }

                is PipelineOutput.ScopeRefusal -> {
                    // Amber Refusal Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, if (isDarkTheme) WarningAmberDark else WarningAmber, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDarkTheme) WarningAmberDarkBg else WarningAmberLight
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = "Refusal",
                                    tint = if (isDarkTheme) WarningAmberDark else WarningAmber,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = Strings.outOfScopeTitle(language),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDarkTheme) WarningAmberDark else WarningAmber
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = output.message,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (isDarkTheme) TextPrimaryDark else Color(0xFF0F172A)
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = Strings.outOfScopeDesc(language),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isDarkTheme) TextSecondaryDark else Color(0xFF475569)
                                )
                            )
                        }
                    }
                }

                is PipelineOutput.ProtocolAnswer -> {
                    // Green Verified Protocol Match Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDarkTheme) SurfaceDark else Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Protocol Match",
                                    tint = if (isDarkTheme) SuccessGreenDark else SuccessGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = Strings.verifiedAnswerHeader(language),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = output.chunk.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isDarkTheme) TextPrimaryDark else Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = output.chunk.text,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    lineHeight = 20.sp,
                                    color = if (isDarkTheme) TextPrimaryDark else Color(0xFF1E293B)
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = if (isDarkTheme) Color(0xFF24364D) else Color.LightGray)
                            Spacer(modifier = Modifier.height(8.dp))

                            // Evidence Confidence & Healthcare Facility Recommendation
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isDarkTheme) Color(0xFF0F3830) else Color(0xFFE8F5E9))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Evidence: Strong (${output.chunk.id})",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDarkTheme) SuccessGreenDark else SuccessGreen
                                        )
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isDarkTheme) Color(0xFF162A45) else Color(0xFFE3F2FD))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = output.recommendedFacility,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isDarkTheme) Color(0xFF90CAF9) else Color(0xFF1565C0)
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isDarkTheme) SuccessGreenDarkBg else SuccessGreenLight)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = output.citationText,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isDarkTheme) SuccessGreenDark else SuccessGreen
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            // Share Advisory with Patient Button
                            OutlinedButton(
                                onClick = {
                                    val adv = PatientAdvisoryEngine.generateAdvisory(output.chunk.id, language)
                                    val text = PatientAdvisoryEngine.formatShareableText(adv)
                                    val intent = PatientAdvisoryEngine.createShareIntent(text)
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "No sharing app available", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    Icons.Default.Share,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Share Advisory with Patient",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                is PipelineOutput.OutOfCorpus -> {
                    // Neutral Card for questions outside verified protocol corpus
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDarkTheme) SurfaceElevatedDark else Color(0xFFEEEEEE)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = Strings.notFoundMessage(language),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDarkTheme) TextPrimaryDark else Color(0xFF0F172A)
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Sahaaya strictly avoids hallucinating answers beyond verified government training manuals.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isDarkTheme) TextSecondaryDark else Color.Gray
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    // Emergency Referral Slip Modal
    if (currentOutput is PipelineOutput.DangerEscalation) {
        val danger = currentOutput as PipelineOutput.DangerEscalation
        ReferralSlipDialog(
            isOpen = showReferralSlip,
            onDismiss = { showReferralSlip = false },
            patientName = Strings.patientWalkin(language),
            dangerSign = danger.rule.dangerSign,
            category = danger.rule.category,
            citation = danger.citation,
            language = language,
            isDarkTheme = isDarkTheme
        )
    }
}

