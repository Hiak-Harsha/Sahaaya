package com.sahaaya.companion

import com.sahaaya.companion.core.*
import com.sahaaya.companion.features.facilities.FacilityDirectory
import com.sahaaya.companion.features.triage.ConditionCriticalityEngine
import com.sahaaya.companion.features.triage.CriticalityLevel
import com.sahaaya.companion.features.scheduling.FollowUpScheduler
import com.sahaaya.companion.features.sync.OfflineSyncQueue
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File
import java.time.LocalDate

class SahaayaCoreTest {

    private lateinit var chunks: List<ProtocolChunk>
    private lateinit var rules: List<DangerRule>
    private lateinit var pipeline: SahaayaPipeline
    private lateinit var visitEngine: VisitListEngine
    private lateinit var registerMembers: List<RegisterMember>

    @Before
    fun setUp() {
        val assetsDir = listOf(
            File("src/main/assets"),
            File("app/src/main/assets"),
            File("android-app/app/src/main/assets"),
            File("assets"),
            File("../assets")
        ).firstOrNull { it.exists() && File(it, "protocol_chunks.json").exists() }
            ?: throw IllegalStateException("Assets directory not found")

        val chunksJson = File(assetsDir, "protocol_chunks.json").readText(Charsets.UTF_8)
        val rulesJson = File(assetsDir, "danger_rules.json").readText(Charsets.UTF_8)
        val registerCsv = File(assetsDir, "register.csv").readText(Charsets.UTF_8)

        chunks = DataLoader.parseProtocolChunks(chunksJson)
        rules = DataLoader.parseDangerRules(rulesJson)
        registerMembers = DataLoader.parseRegister(registerCsv)

        val dangerEngine = DangerRuleEngine(rules)
        val intentGate = IntentGate()
        val bm25Retriever = Bm25Retriever(chunks)
        pipeline = SahaayaPipeline(dangerEngine, intentGate, bm25Retriever)
        visitEngine = VisitListEngine()
    }

    @Test
    fun testProtocolChunksIngested() {
        assertEquals("Exactly 107 leaf protocol sections must be ingested", 107, chunks.size)
        val ancChunk = chunks.firstOrNull { it.id == "ANC-1.2" }
        assertNotNull("ANC-1.2 must be present", ancChunk)
        assertTrue("ANC-1.2 should mention 36 weeks", ancChunk!!.text.contains("36") || ancChunk.title.contains("ANC"))
    }

    @Test
    fun testDangerEscalationRecall() {
        val mustEscalateQueries = listOf(
            "Bacche ko tez bukhar hai aur jhatke aa rahe hain",
            "Pregnancy me chehra suja hua hai aur sar me tez dard hai",
            "Bachha thanda lag raha hai aur doodh nahi pee raha",
            "Sugar ka mareez behosh ho gaya hai",
            "Delivery ke baad pad 5 minute me poora bheeg gaya",
            "Navjaat shishu ka vajan 1.5 kg hai",
            "9 din se lagatar bukhar hai"
        )
        for (q in mustEscalateQueries) {
            val output = pipeline.process(q)
            assertTrue("Query '$q' must escalate to DangerEscalation", output is PipelineOutput.DangerEscalation)
            val esc = output as PipelineOutput.DangerEscalation
            assertTrue("Action must be REFER NOW", esc.actionText.contains("REFER NOW"))
            assertTrue("Citation must not be empty", esc.citation.isNotBlank())
        }
    }

    @Test
    fun testIntentGateRefusal() {
        val outOfScopeQueries = listOf(
            "Bacche ko bukhar me paracetamol syrup kitna ml dena chahiye?",
            "Kya main amoxicillin antibiotic de sakti hoon?",
            "U-WIN portal me login nahi ho raha otp nahi aaya",
            "Widal test positive aaya hai typhoid ki dawai batao"
        )
        for (q in outOfScopeQueries) {
            val output = pipeline.process(q)
            assertTrue("Query '$q' must be refused", output is PipelineOutput.ScopeRefusal)
            val refusal = output as PipelineOutput.ScopeRefusal
            assertTrue("Refusal must cite SCP-1.2", refusal.message.contains("SCP-1.2") || refusal.citedSectionIds.contains("SCP-1.2"))
        }
    }

    @Test
    fun testGroundedProtocolAnswer() {
        val inScopeQueries = listOf(
            "Sugar ke mareez ko hafte me kitni exercise karni chahiye?" to "NCD-3.1",
            "3rd trimester me Hb 8.6 g/dl hai to kya ye anaemia hai?" to "ANC-3.1",
            "ORS pilate waqt bachha ulti kar de to kya karein?" to "CHD-1.2",
            "Delivery ke baad IFA ki laal goli kab tak khilani hai?" to "ANC-2.2",
            "Calcium ki goli khali pet le sakte hain kya?" to "ANC-2.3",
            "MUAC tape par green kab maana jaata hai?" to "NUT-1.2"
        )
        for ((q, expectedId) in inScopeQueries) {
            val output = pipeline.process(q)
            assertTrue("Query '$q' must return ProtocolAnswer", output is PipelineOutput.ProtocolAnswer)
            val ans = output as PipelineOutput.ProtocolAnswer
            assertTrue("Expected chunk $expectedId but got ${ans.chunk.id}", ans.citedSectionIds.contains(expectedId) || ans.chunk.id == expectedId)
            assertTrue("Answer citation text must be present", ans.citationText.contains(expectedId))
        }
    }

    @Test
    fun testVisitListPrioritization() {
        val visits = visitEngine.prioritizeVisits(registerMembers, today = LocalDate.of(2026, 10, 18))
        assertTrue("Visits list should not be empty", visits.isNotEmpty())

        var prevRank = 0
        for (v in visits) {
            assertTrue("Priority rank must be non-decreasing (1 <= 2 <= 3)", v.priorityRank >= prevRank)
            prevRank = v.priorityRank
        }

        val dangerVisits = visits.filter { it.priorityGroup == PriorityGroup.DANGER }
        assertTrue("Must have at least one danger visit", dangerVisits.isNotEmpty())
    }

    @Test
    fun testFacilityDirectory() {
        val fru = FacilityDirectory.getNearestEmergencyFacility()
        assertEquals("FAC-CHC-01", fru.id)
        assertTrue(fru.acceptsEmergency24x7)

        val rec = FacilityDirectory.recommendFacilityForCategory("HAEMORRHAGE", isEmergency = true)
        assertEquals("FAC-CHC-01", rec.primaryFacility.id)
        assertTrue(rec.clinicalRationale.contains("CEmONC") || rec.clinicalRationale.contains("emergency"))
    }

    @Test
    fun testConditionCriticalityTriage() {
        val alert = ConditionCriticalityEngine.assessCondition(
            beneficiaryName = "Sunita Bai",
            householdId = "HH-101",
            symptoms = "jhatke aa rahe hain aur behosh hai",
            riskFlags = "",
            isPregnant = true,
            ageDays = 24 * 365
        )
        assertEquals(CriticalityLevel.RED_CRITICAL, alert.level)
        assertTrue(alert.actionRequired.contains("108"))
    }

    @Test
    fun testFollowUpScheduler() {
        val schedule = FollowUpScheduler.generateHbncSchedule(
            beneficiaryName = "Geeta Devi",
            householdId = "HH-202",
            deliveryDate = LocalDate.of(2026, 10, 1),
            isInstitutional = true,
            isCaesarean = false
        )
        assertEquals(6, schedule.size)
        assertEquals("HBNC Visit Day 3", schedule[0].visitType)
        assertEquals(LocalDate.of(2026, 10, 4), schedule[0].scheduledDate)
    }

    @Test
    fun testOfflineSyncQueue() {
        val event = OfflineSyncQueue.recordVisitCompletion("M-101", "Radha Devi", "Completed ANC-4 checkup")
        assertEquals("VISIT_COMPLETED", event.eventType)
        val pending = OfflineSyncQueue.getPendingEvents()
        assertTrue("Pending events should contain the event", pending.any { it.id == event.id })
    }

    @Test
    fun testMultilingualQueryUnderstandingAndTypoTolerance() {
        // 1. Devanagari Hindi Script
        val devanagariQuery = "बच्चे को तेज बुखार है और झटके आ रहे हैं"
        val outDev = pipeline.process(devanagariQuery)
        assertTrue("Devanagari query must escalate", outDev is PipelineOutput.DangerEscalation)

        // 2. Telugu Script
        val teluguQuery = "పిల్లాడికి తీవ్రమైన జ్వరం మరియు ఫిట్స్ వస్తున్నాయి"
        val outTel = pipeline.process(teluguQuery)
        assertTrue("Telugu query must escalate", outTel is PipelineOutput.DangerEscalation)

        // 3. Typo Tolerance in Roman Hindi (e.g. 'palis dhns rhi' for pasli dhansna)
        val typoQuery = "bacche ki palis andar dhns rhi he"
        val outTypo = pipeline.process(typoQuery)
        assertTrue("Typo query must escalate", outTypo is PipelineOutput.DangerEscalation)

        // 4. Word-Order Variation
        val wo1 = pipeline.process("bachcha doodh nahi pee raha")
        val wo2 = pipeline.process("doodh nahi pee raha hai bachcha")
        assertTrue("Standard word order must escalate", wo1 is PipelineOutput.DangerEscalation)
        assertTrue("Inverted word order must escalate", wo2 is PipelineOutput.DangerEscalation)
    }

    @Test
    fun testNegationAndSafetyFilters() {
        // Negation: Symptoms denied by user must not escalate
        val negated1 = pipeline.process("bachhe ko bukhar tha par ab jhatke nahi aa rahe hain")
        // Should not escalate to emergency if convulsions are negated
        assertFalse("Negated fits should not escalate", negated1 is PipelineOutput.DangerEscalation)

        // Educational / Methodology questions should not falsely escalate
        val eduQuery = "MUAC tape se green kab maana jaata hai?"
        val outEdu = pipeline.process(eduQuery)
        assertTrue("Educational query must return ProtocolAnswer", outEdu is PipelineOutput.ProtocolAnswer)
    }

    @Test
    fun testNumericThresholds() {
        // 1. Birth weight < 1.8 kg must escalate; >= 1.8 kg must not escalate
        val lowBirthWeight = pipeline.process("navjaat shishu ka vajan 1.7 kg hai")
        assertTrue("1.7 kg infant must escalate to ESC-4.4", lowBirthWeight is PipelineOutput.DangerEscalation)
        assertEquals("ESC-4.4", (lowBirthWeight as PipelineOutput.DangerEscalation).citedSectionIds.first())

        val normalBirthWeight = pipeline.process("navjaat shishu ka vajan 1.9 kg hai aur nehlana kab hai")
        assertFalse("1.9 kg infant must not escalate as low birth weight", normalBirthWeight is PipelineOutput.DangerEscalation)

        // 2. Prolonged fever >= 7 days must escalate; < 7 days must not escalate as prolonged fever
        val fever6Days = pipeline.process("bacche ko 6 din se bukhar hai")
        assertFalse("6 days fever should not trigger prolonged fever escalation", fever6Days is PipelineOutput.DangerEscalation)

        val fever7Days = pipeline.process("bacche ko 7 din se bukhar hai")
        assertTrue("7 days fever must escalate to ESC-5.4", fever7Days is PipelineOutput.DangerEscalation)
        assertEquals("ESC-5.4", (fever7Days as PipelineOutput.DangerEscalation).citedSectionIds.first())

        val fever9Days = pipeline.process("9 din se lagatar bukhar hai")
        assertTrue("9 days fever must escalate to ESC-5.4", fever9Days is PipelineOutput.DangerEscalation)

        // 3. Severe hypertensive crisis (>= 180/110)
        val htnCrisis = pipeline.process("Patient ka blood pressure 190/115 aa raha hai")
        assertTrue("BP 190/115 must escalate to ESC-6.4", htnCrisis is PipelineOutput.DangerEscalation)
        assertEquals("ESC-6.4", (htnCrisis as PipelineOutput.DangerEscalation).citedSectionIds.first())
    }

    @Test
    fun testIntentGateKitExceptions() {
        // Kit exceptions that MUST NOT be refused:
        val ifaQuery = pipeline.process("ifa ki lal goli kab khilana hai")
        assertFalse("IFA query must not be refused", ifaQuery is PipelineOutput.ScopeRefusal)

        val zincQuery = pipeline.process("dast me zinc tablet strip kaise dena hai")
        assertFalse("Zinc query must not be refused", zincQuery is PipelineOutput.ScopeRefusal)

        val orsQuery = pipeline.process("ors ka packet kitne paani me gholna chahiye")
        assertFalse("ORS query must not be refused", orsQuery is PipelineOutput.ScopeRefusal)

        val calciumQuery = pipeline.process("calcium goli khali pet khani hai kya")
        assertFalse("Calcium query must not be refused", calciumQuery is PipelineOutput.ScopeRefusal)

        val albendazoleQuery = pipeline.process("keede maarne ke liye albendazole kab de")
        assertFalse("Albendazole query must not be refused", albendazoleQuery is PipelineOutput.ScopeRefusal)

        val tepidQuery = pipeline.process("bukhar me tepid sponging kaise kare")
        assertFalse("Tepid sponging query must not be refused", tepidQuery is PipelineOutput.ScopeRefusal)

        // Out-of-scope queries with specific reasons:
        val rxGate = IntentGate().evaluate("bache ko paracetamol syrup kitna ml dena")
        assertTrue(rxGate.isRefusal)
        assertEquals("PRESCRIPTION_OUT_OF_SCOPE", rxGate.reason)

        val pcpndtGate = IntentGate().evaluate("ladka hoga ya ladki jaanch karani hai")
        assertTrue(pcpndtGate.isRefusal)
        assertEquals("PCPNDT_PROHIBITED", pcpndtGate.reason)

        val adminGate = IntentGate().evaluate("u-win portal me login nahi ho raha hai")
        assertTrue(adminGate.isRefusal)
        assertEquals("ADMINISTRATIVE_OUT_OF_SCOPE", adminGate.reason)

        val diagGate = IntentGate().evaluate("thyroid aur tsh ki test report dikhani hai")
        assertTrue(diagGate.isRefusal)
        assertEquals("DIAGNOSIS_OUT_OF_SCOPE", diagGate.reason)
    }

    @Test
    fun testSafetyOrderPipeline() {
        // Safety Order Invariant: Danger Detection MUST ALWAYS run before Intent Gate
        // Even if a query requests a prescription dose, if danger signs are present, it must ESCALATE as Danger
        val combinedQuery = "bache ko paracetamol dose kitna de jhatke aa rahe hain aur behosh hai"
        val out = pipeline.process(combinedQuery)
        assertTrue("Danger sign must fire before Intent Gate refusal", out is PipelineOutput.DangerEscalation)
        assertFalse("Must not be intercepted by ScopeRefusal when danger sign is active", out is PipelineOutput.ScopeRefusal)
    }

    @Test
    fun testRetrievalParity() {
        // Parity test across key clinical protocols
        val anc4 = pipeline.process("chautha anc checkup kab hota hai")
        assertTrue(anc4 is PipelineOutput.ProtocolAnswer)
        assertEquals("ANC-1.2", (anc4 as PipelineOutput.ProtocolAnswer).chunk.id)

        val calcium = pipeline.process("calcium ki goli khali pet lena hai kya")
        assertTrue(calcium is PipelineOutput.ProtocolAnswer)
        assertEquals("ANC-2.3", (calcium as PipelineOutput.ProtocolAnswer).chunk.id)

        val muac = pipeline.process("muac tape me green kab hota hai")
        assertTrue(muac is PipelineOutput.ProtocolAnswer)
        assertEquals("NUT-1.2", (muac as PipelineOutput.ProtocolAnswer).chunk.id)

        val exercise = pipeline.process("sugar aur bp ke marij ko exercise kitni karni chahiye")
        assertTrue(exercise is PipelineOutput.ProtocolAnswer)
        assertEquals("NCD-3.1", (exercise as PipelineOutput.ProtocolAnswer).chunk.id)

        val overdue = pipeline.process("tika kab overdue maana jaata hai")
        assertTrue(overdue is PipelineOutput.ProtocolAnswer)
        assertEquals("VIS-1.3", (overdue as PipelineOutput.ProtocolAnswer).chunk.id)

        // Bounded LBW bathing: general 'bathroom' or 'bathe wound' should not match HBNC-2.2
        val lbwBabyBath = pipeline.process("kam wajan ke bachche ko snan kab karaye")
        assertTrue(lbwBabyBath is PipelineOutput.ProtocolAnswer)
        assertEquals("HBNC-2.2", (lbwBabyBath as PipelineOutput.ProtocolAnswer).chunk.id)
    }
}
