package com.sahaaya.companion.ui.screens.dashboard

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sahaaya.companion.core.PrioritizedVisit
import com.sahaaya.companion.core.PriorityGroup
import com.sahaaya.companion.core.RegisterMember
import com.sahaaya.companion.features.followup.FollowUpRepository
import com.sahaaya.companion.features.sync.OfflineSyncQueue
import com.sahaaya.companion.ui.PrimaryTeal
import com.sahaaya.companion.ui.PrimaryTealDarkTheme
import com.sahaaya.companion.ui.SurfaceDark
import com.sahaaya.companion.ui.SurfaceElevatedDark
import com.sahaaya.companion.ui.i18n.AppLanguage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    members: List<RegisterMember>,
    prioritizedVisits: List<PrioritizedVisit>,
    isDarkTheme: Boolean,
    language: AppLanguage,
    onNavigateToVisits: (filter: String) -> Unit,
    onNavigateToAsk: (initialQuery: String?) -> Unit,
    onSelectPatient: (RegisterMember) -> Unit
) {
    val primaryColor = if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal
    val bgColor = if (isDarkTheme) SurfaceDark else Color(0xFFF7FAFA)
    val cardBg = if (isDarkTheme) SurfaceElevatedDark else Color.White
    val textPrimary = if (isDarkTheme) Color.White else Color(0xFF0F172A)
    val textSecondary = if (isDarkTheme) Color.LightGray else Color(0xFF475569)

    var searchQuery by remember { mutableStateOf("") }
    val pendingEvents = remember { OfflineSyncQueue.getPendingEvents() }
    val followUps = remember { FollowUpRepository.getAllFollowUps() }

    val dangerCount = prioritizedVisits.count { it.priorityGroup == PriorityGroup.DANGER }
    val overdueCount = prioritizedVisits.count { it.priorityGroup == PriorityGroup.OVERDUE }
    val routineCount = prioritizedVisits.count { it.priorityGroup == PriorityGroup.UPCOMING }

    val searchResults = remember(searchQuery, members) {
        if (searchQuery.isBlank()) emptyList()
        else {
            val q = searchQuery.trim().lowercase()
            members.filter {
                it.name.lowercase().contains(q) ||
                it.familyId.lowercase().contains(q) ||
                it.memberId.lowercase().contains(q)
            }.take(5)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Search Box
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search beneficiary by name or household ID...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = primaryColor) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Search Dropdown Results if active
        if (searchResults.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "Search Matches (${searchResults.size}):",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall,
                            color = primaryColor
                        )
                        searchResults.forEach { mem ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectPatient(mem) }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(mem.name, fontWeight = FontWeight.Bold, color = textPrimary)
                                    Text("${mem.memberId} • ${mem.familyId} • ${mem.sex}", style = MaterialTheme.typography.bodySmall, color = textSecondary)
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = primaryColor)
                            }
                            Divider(color = Color.LightGray.copy(alpha = 0.3f))
                        }
                    }
                }
            }
        }

        // 2. Triage Status Cards (Danger, Overdue, Routine)
        item {
            Text(
                text = "Today's Field Priority Overview",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
                color = textPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DashboardMetricCard(
                    title = "Emergency Alerts",
                    count = dangerCount,
                    subtitle = "Referral required",
                    containerColor = if (isDarkTheme) Color(0xFF2C1318) else Color(0xFFFFF1F2),
                    accentColor = Color(0xFFDC2626),
                    icon = Icons.Default.Warning,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToVisits("DANGER") }
                )
                DashboardMetricCard(
                    title = "Overdue Visits",
                    count = overdueCount,
                    subtitle = "Action due",
                    containerColor = if (isDarkTheme) Color(0xFF2B210F) else Color(0xFFFFFBEB),
                    accentColor = Color(0xFFD97706),
                    icon = Icons.Default.NotificationsActive,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToVisits("OVERDUE") }
                )
                DashboardMetricCard(
                    title = "Routine Visits",
                    count = routineCount,
                    subtitle = "Scheduled",
                    containerColor = if (isDarkTheme) SurfaceElevatedDark else Color(0xFFE0F2F1),
                    accentColor = primaryColor,
                    icon = Icons.Default.DateRange,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToVisits("ALL") }
                )
            }
        }

        // 3. Quick Action Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onNavigateToAsk(null) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Icon(Icons.Default.QuestionAnswer, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ask Protocol", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { onNavigateToVisits("ALL") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.ListAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Visit Register", fontWeight = FontWeight.Bold)
                }
            }
        }

        // 4. Offline Queue & Device Status Pill
        item {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = cardBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2E7D32))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Offline Operation Active",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = textPrimary
                            )
                            Text(
                                text = "${pendingEvents.size} pending audit events queued locally",
                                fontSize = 11.sp,
                                color = textSecondary
                            )
                        }
                    }
                    Text(
                        text = "100% OFFLINE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = Color(0xFF2E7D32)
                    )
                }
            }
        }

        // 5. Recent Priority Cases (First 3 Priority 1 members)
        item {
            Text(
                text = "Priority Field Beneficiaries",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
                color = textPrimary
            )
        }

        items(prioritizedVisits.take(4)) { visit ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectPatient(visit.member) },
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = visit.member.name,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Text(
                            text = "${visit.member.familyId} • ${visit.reason}",
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondary,
                            maxLines = 1
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (visit.priorityGroup) {
                            PriorityGroup.DANGER -> Color(0xFFFFEBEE)
                            PriorityGroup.OVERDUE -> Color(0xFFFFF8E1)
                            else -> Color(0xFFE0F2F1)
                        }
                    ) {
                        Text(
                            text = if (visit.priorityGroup == PriorityGroup.DANGER) "DANGER" else if (visit.priorityGroup == PriorityGroup.OVERDUE) "OVERDUE" else "ROUTINE",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = when (visit.priorityGroup) {
                                PriorityGroup.DANGER -> Color(0xFFC62828)
                                PriorityGroup.OVERDUE -> Color(0xFFF57F17)
                                else -> Color(0xFF00796B)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardMetricCard(
    title: String,
    count: Int,
    subtitle: String,
    containerColor: Color,
    accentColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
                Text(
                    text = "$count",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                maxLines = 1
            )
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = accentColor.copy(alpha = 0.85f),
                maxLines = 1
            )
        }
    }
}
