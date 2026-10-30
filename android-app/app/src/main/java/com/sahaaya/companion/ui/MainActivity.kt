package com.sahaaya.companion.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sahaaya.companion.R
import com.sahaaya.companion.core.*
import com.sahaaya.companion.ui.i18n.AppLanguage
import com.sahaaya.companion.ui.i18n.Strings
import java.io.File

class MainActivity : ComponentActivity() {

    private lateinit var pipeline: SahaayaPipeline
    private lateinit var visitEngine: VisitListEngine
    private var registerMembers: List<RegisterMember> = emptyList()
    private var testQuestions: List<Map<String, String>> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load offline assets
        val chunksJson = assets.open("protocol_chunks.json").bufferedReader().use { it.readText() }
        val rulesJson = assets.open("danger_rules.json").bufferedReader().use { it.readText() }
        val registerCsv = try {
            assets.open("household_register.csv").bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            assets.open("register.csv").bufferedReader().use { it.readText() }
        }
        val questionsCsv = try {
            assets.open("questions_dev.csv").bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            assets.open("test_questions.csv").bufferedReader().use { it.readText() }
        }

        val chunks = DataLoader.parseProtocolChunks(chunksJson)
        val rules = DataLoader.parseDangerRules(rulesJson)
        registerMembers = DataLoader.parseRegister(registerCsv)
        testQuestions = DataLoader.parseCsv(questionsCsv)

        val dangerEngine = DangerRuleEngine(rules)
        val intentGate = IntentGate()
        val bm25Retriever = Bm25Retriever(chunks)
        pipeline = SahaayaPipeline(dangerEngine, intentGate, bm25Retriever)
        visitEngine = VisitListEngine()

        setContent {
            var isDarkTheme by rememberSaveable { mutableStateOf(false) }
            var currentLanguage by rememberSaveable { mutableStateOf(AppLanguage.ENGLISH) }

            SahaayaTheme(darkTheme = isDarkTheme) {
                MainAppScaffold(
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { isDarkTheme = !isDarkTheme },
                    language = currentLanguage,
                    onLanguageChange = { currentLanguage = it }
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainAppScaffold(
        isDarkTheme: Boolean,
        onToggleTheme: () -> Unit,
        language: AppLanguage,
        onLanguageChange: (AppLanguage) -> Unit
    ) {
        var selectedTab by rememberSaveable { mutableStateOf(0) }
        var showBenchmarkDialog by remember { mutableStateOf(false) }
        var showLanguageMenu by remember { mutableStateOf(false) }
        var askInitialQuery by remember { mutableStateOf<String?>(null) }

        // Choose logo based on mode: Dark mode uses glowing squircle, Light mode uses bright symbol
        val logoRes = if (isDarkTheme) {
            R.drawable.ic_sahaaya_logo
        } else {
            R.drawable.ic_sahaaya_symbol_bright
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isDarkTheme) Color(0xFF041930) else Color.White)
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = logoRes),
                                    contentDescription = "Sahaaya Logo",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    Strings.appTitle(language),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    Strings.appSubtitle(language),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = if (isDarkTheme) SurfaceDark else PrimaryTeal
                    ),
                    actions = {
                        // 1. Language Switcher Menu
                        Box {
                            IconButton(onClick = { showLanguageMenu = true }) {
                                Icon(
                                    Icons.Default.Translate,
                                    contentDescription = "Switch Language",
                                    tint = Color.White
                                )
                            }
                            DropdownMenu(
                                expanded = showLanguageMenu,
                                onDismissRequest = { showLanguageMenu = false },
                                modifier = Modifier.background(
                                    if (isDarkTheme) SurfaceElevatedDark else Color.White
                                )
                            ) {
                                AppLanguage.values().forEach { lang ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "${lang.nativeName} (${lang.displayName})",
                                                    fontWeight = if (lang == language) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isDarkTheme) TextPrimaryDark else Color.Black
                                                )
                                                if (lang == language) {
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Icon(
                                                        Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            onLanguageChange(lang)
                                            showLanguageMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        // 2. Dark / Bright Theme Toggle
                        IconButton(onClick = onToggleTheme) {
                            Icon(
                                if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = if (isDarkTheme) "Bright Mode" else "Dark Mode",
                                tint = Color.White
                            )
                        }

                        // 3. Benchmark Dialog
                        IconButton(onClick = { showBenchmarkDialog = true }) {
                            Icon(
                                Icons.Default.Speed,
                                contentDescription = "Benchmark",
                                tint = Color.White
                            )
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = if (isDarkTheme) SurfaceDark else SurfaceWhite
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.QuestionAnswer, contentDescription = null) },
                        label = { Text(Strings.tabAsk(language), fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal,
                            selectedTextColor = if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal,
                            indicatorColor = if (isDarkTheme) SurfaceElevatedDark else Color(0xFFE0F2F1)
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                        label = { Text(Strings.tabVisits(language), fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal,
                            selectedTextColor = if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal,
                            indicatorColor = if (isDarkTheme) SurfaceElevatedDark else Color(0xFFE0F2F1)
                        )
                    )
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                if (selectedTab == 0) {
                    AskScreen(
                        pipeline = pipeline,
                        language = language,
                        isDarkTheme = isDarkTheme,
                        initialQuery = askInitialQuery
                    )
                } else {
                    VisitsScreen(
                        members = registerMembers,
                        visitEngine = visitEngine,
                        language = language,
                        isDarkTheme = isDarkTheme,
                        onNavigateToAsk = { query ->
                            askInitialQuery = query
                            selectedTab = 0
                        }
                    )
                }

                if (showBenchmarkDialog) {
                    val apkFile = applicationInfo?.sourceDir?.let { File(it) }
                    BenchmarkScreen(
                        pipeline = pipeline,
                        testQuestions = testQuestions,
                        apkFile = apkFile,
                        isDarkTheme = isDarkTheme,
                        language = language,
                        onDismiss = { showBenchmarkDialog = false }
                    )
                }
            }
        }
    }
}
