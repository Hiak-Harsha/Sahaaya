package com.sahaaya.companion.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sahaaya.companion.core.ClinicalTextFormatter
import com.sahaaya.companion.core.ProtocolChunk
import com.sahaaya.companion.ui.i18n.AppLanguage
import com.sahaaya.companion.ui.i18n.Strings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideScreen(
    chunks: List<ProtocolChunk>,
    language: AppLanguage,
    isDarkTheme: Boolean,
    onNavigateToAsk: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }

    // Categorization logic based on official protocol ID prefixes
    fun getCategoryForChunk(chunk: ProtocolChunk): String {
        val id = chunk.id.uppercase()
        return when {
            id.startsWith("CHD") -> "CHD"
            id.startsWith("IMM") -> "IMM"
            id.startsWith("ANC") || id.startsWith("PNC") || id.startsWith("HBNC") -> "MATERNAL"
            id.startsWith("ESC") -> "ESC"
            id.startsWith("SCP") -> "SCP"
            id.startsWith("NUT") -> "NUT"
            id.startsWith("NCD") -> "NCD"
            else -> "OTHER"
        }
    }

    val filteredChunks = remember(chunks, searchQuery, selectedCategory) {
        val catFiltered = when (selectedCategory) {
            "ALL" -> chunks
            "CHD" -> chunks.filter { getCategoryForChunk(it) == "CHD" }
            "IMM" -> chunks.filter { getCategoryForChunk(it) == "IMM" }
            "MATERNAL" -> chunks.filter { getCategoryForChunk(it) == "MATERNAL" }
            "ESC" -> chunks.filter { getCategoryForChunk(it) == "ESC" }
            "SCP" -> chunks.filter { getCategoryForChunk(it) == "SCP" }
            "NUT" -> chunks.filter { getCategoryForChunk(it) == "NUT" }
            "NCD" -> chunks.filter { getCategoryForChunk(it) == "NCD" }
            else -> chunks
        }

        if (searchQuery.isBlank()) {
            catFiltered
        } else {
            val q = searchQuery.trim().lowercase()
            catFiltered.filter {
                it.id.lowercase().contains(q) ||
                it.title.lowercase().contains(q) ||
                it.text.lowercase().contains(q) ||
                it.sourceDocument.lowercase().contains(q)
            }
        }
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
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        // 1. Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = Strings.guideHeader(language),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) TextPrimaryDark else SecondaryNavy,
                        fontSize = 19.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = Strings.guideSubheader(chunks.size, language),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (isDarkTheme) TextSecondaryDark else Color(0xFF64748B),
                        fontSize = 11.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Official Protocol Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isDarkTheme) SurfaceElevatedDark else Color(0xFFE0F2F1))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "${filteredChunks.size} / ${chunks.size}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal,
                        fontSize = 11.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 46.dp),
            placeholder = {
                Text(
                    text = Strings.searchProtocolsPlaceholder(language),
                    fontSize = 12.5.sp,
                    color = if (isDarkTheme) TextSecondaryDark else Color(0xFF94A3B8)
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    tint = if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Clear",
                            modifier = Modifier.size(16.dp),
                            tint = if (isDarkTheme) TextSecondaryDark else Color(0xFF64748B)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal,
                unfocusedBorderColor = if (isDarkTheme) Color(0xFF24364D) else Color(0xFFCBD5E1),
                focusedContainerColor = if (isDarkTheme) SurfaceDark else Color.White,
                unfocusedContainerColor = if (isDarkTheme) SurfaceDark else Color.White,
                focusedTextColor = if (isDarkTheme) TextPrimaryDark else Color(0xFF0F172A),
                unfocusedTextColor = if (isDarkTheme) TextPrimaryDark else Color(0xFF0F172A)
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Category Horizontal Filter Chips
        val categories = listOf(
            "ALL" to Strings.categoryAll(language),
            "MATERNAL" to Strings.categoryMaternal(language),
            "CHD" to Strings.categoryChild(language),
            "IMM" to Strings.categoryImm(language),
            "NUT" to Strings.categoryNutrition(language),
            "NCD" to Strings.categoryNcd(language),
            "ESC" to Strings.categoryEmergency(language),
            "SCP" to Strings.categoryScope(language)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.forEach { (catKey, catLabel) ->
                val isSelected = selectedCategory == catKey
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = catKey },
                    label = {
                        Text(
                            text = catLabel,
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal,
                        selectedLabelColor = if (isDarkTheme) Color(0xFF022C28) else Color.White,
                        containerColor = if (isDarkTheme) SurfaceElevatedDark else Color.White,
                        labelColor = if (isDarkTheme) TextPrimaryDark else Color(0xFF334155)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isDarkTheme) Color(0xFF24364D) else Color(0xFFCBD5E1),
                        selectedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier.heightIn(min = 34.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 4. Protocols List
        if (filteredChunks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = Strings.noProtocolsMatch(language),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (isDarkTheme) TextSecondaryDark else Color(0xFF64748B),
                        textAlign = TextAlign.Center
                    )
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredChunks, key = { it.id }) { chunk ->
                    ProtocolGuideCard(
                        chunk = chunk,
                        language = language,
                        isDarkTheme = isDarkTheme,
                        onConsult = {
                            val prompt = "${chunk.id} ${chunk.title}"
                            onNavigateToAsk(prompt)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProtocolGuideCard(
    chunk: ProtocolChunk,
    language: AppLanguage,
    isDarkTheme: Boolean,
    onConsult: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember(chunk.id) { mutableStateOf(false) }

    val formattedPoints = remember(chunk.id) {
        ClinicalTextFormatter.formatToPoints(chunk.text)
    }

    val (badgeText, badgeColor) = remember(chunk.id, isDarkTheme) {
        val id = chunk.id.uppercase()
        when {
            id.startsWith("ESC") -> Strings.categoryEmergency(language) to (if (isDarkTheme) DangerRedDark else DangerRed)
            id.startsWith("CHD") -> Strings.categoryChild(language) to (if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal)
            id.startsWith("IMM") -> Strings.categoryImm(language) to Color(0xFF0284C7)
            id.startsWith("ANC") || id.startsWith("PNC") || id.startsWith("HBNC") -> Strings.categoryMaternal(language) to Color(0xFF8B5CF6)
            id.startsWith("SCP") -> Strings.categoryScope(language) to (if (isDarkTheme) WarningAmberDark else WarningAmber)
            id.startsWith("NUT") -> Strings.categoryNutrition(language) to Color(0xFF16A34A)
            id.startsWith("NCD") -> Strings.categoryNcd(language) to Color(0xFFD97706)
            else -> Strings.categoryOther(language) to (if (isDarkTheme) TextSecondaryDark else Color.Gray)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) SurfaceDark else Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isDarkTheme) Color(0xFF24364D) else Color(0xFFE2E8F0)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: ID Tag, Category Badge, Page Citation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = chunk.id,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkTheme) TextPrimaryDark else Color(0xFF0F172A),
                                fontSize = 11.5.sp
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(badgeColor.copy(alpha = if (isDarkTheme) 0.25f else 0.12f))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = badgeColor,
                                fontSize = 10.5.sp
                            )
                        )
                    }
                }

                if (chunk.pageNumber > 0) {
                    Text(
                        text = "p. ${chunk.pageNumber}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isDarkTheme) TextSecondaryDark else Color(0xFF64748B),
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Protocol Title
            Text(
                text = chunk.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.5.sp,
                    color = if (isDarkTheme) TextPrimaryDark else SecondaryNavy,
                    lineHeight = 19.sp
                )
            )

            // Source Document Citation
            if (chunk.sourceDocument.isNotBlank()) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "${chunk.sourceDocument}, p. ${chunk.pageNumber}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDarkTheme) TextSecondaryDark else Color(0xFF475569)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Clinical Protocol Bullet Points
            val displayPoints = if (isExpanded || formattedPoints.size <= 2) {
                formattedPoints
            } else {
                formattedPoints.take(2)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isDarkTheme) Color(0xFF0D1B2A) else Color(0xFFF8FAFC))
                    .padding(10.dp)
            ) {
                Column {
                    displayPoints.forEach { point ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "•",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = point,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp,
                                    color = if (isDarkTheme) TextPrimaryDark else Color(0xFF1E293B)
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Expand / Collapse Toggle if more than 2 points
            if (formattedPoints.size > 2) {
                Spacer(modifier = Modifier.height(4.dp))
                TextButton(
                    onClick = { isExpanded = !isExpanded },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                    modifier = Modifier.heightIn(min = 32.dp)
                ) {
                    Text(
                        text = if (isExpanded) {
                            Strings.btnHideDetailedProtocol(language)
                        } else {
                            Strings.btnViewDetailedProtocol(formattedPoints.size, language)
                        },
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action: Query this protocol in Ask Sahaaya
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onConsult,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.heightIn(min = 36.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal
                    )
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = Strings.btnAskAboutThis(language),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
