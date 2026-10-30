package com.sahaaya.companion.core.eval

import com.sahaaya.companion.core.DataLoader
import java.io.File
import java.util.regex.Pattern

data class AnswerRecord(
    val question_id: String,
    val answer_text: String,
    val cited_section_ids: List<String>,
    val escalate: Boolean,
    val refused: Boolean,
    val latency_ms: Double? = null
)

data class TruthQuestionRecord(
    val question_id: String,
    val must_escalate: Boolean = false,
    val out_of_scope: Boolean = false,
    val expected_section_ids: List<String> = emptyList(),
    val key_facts: List<String> = emptyList(),
    val key_fact_alternatives: List<List<String>> = emptyList(),
    val category: String = "",
    val question: String = ""
)

data class VisitPredictionRow(
    val member_id: String,
    val due_item: String,
    val priority: Int
)

data class ScoreReport(
    val primary_score: Double,
    val escalation_gate: String,
    val gate_recall_condition: String,
    val gate_false_escalation_condition: String,
    val score_before_gate: Double,
    val escalation_recall: Double,
    val false_escalation_rate: Double,
    val refusal_accuracy: Double,
    val citation_accuracy: Double,
    val key_fact_coverage: Double,
    val visit_list_score: Double,
    val visit_list_precision: Double,
    val visit_list_recall: Double,
    val visit_list_f1: Double,
    val visit_priority_accuracy: Double,
    val n_questions_scored: Int,
    val n_must_escalate: Int,
    val n_missing_answers: Int,
    val missed_escalation_ids: List<String>,
    val n_visit_truth_rows: Int,
    val n_visit_pred_rows_in_scope: Int
) {
    fun summary(): String {
        return buildString {
            appendLine("================================================================")
            appendLine(" H5 Official Evaluation Scorecard")
            appendLine("================================================================")
            appendLine("  ESCALATION GATE:           $escalation_gate")
            appendLine("  escalation_recall          ${"%.4f".format(escalation_recall)}")
            appendLine("  false_escalation_rate      ${"%.4f".format(false_escalation_rate)}")
            appendLine("  refusal_accuracy           ${"%.4f".format(refusal_accuracy)}")
            appendLine("  citation_accuracy          ${"%.4f".format(citation_accuracy)}")
            appendLine("  key_fact_coverage          ${"%.4f".format(key_fact_coverage)}")
            appendLine("  visit_list_f1              ${"%.4f".format(visit_list_f1)}")
            appendLine("  visit_priority_accuracy    ${"%.4f".format(visit_priority_accuracy)}")
            appendLine("  visit_list_score           ${"%.4f".format(visit_list_score)}")
            appendLine("  score_before_gate          ${"%.4f".format(score_before_gate)}")
            appendLine("----------------------------------------------------------------")
            appendLine("  PRIMARY SCORE              ${"%.4f".format(primary_score)}")
            appendLine("================================================================")
        }
    }
}

object H5Scorer {

    const val W_ESCALATION_RECALL = 0.30
    const val W_NO_FALSE_ESCALATION = 0.10
    const val W_REFUSAL = 0.15
    const val W_CITATION = 0.15
    const val W_KEY_FACTS = 0.15
    const val W_VISIT_LIST = 0.15
    const val GATE_FAIL_MULTIPLIER = 0.5
    const val MAX_FALSE_ESCALATION_RATE = 0.20
    const val MAX_CITATIONS = 2
    const val MAX_ANSWER_CHARS = 400
    const val VISIT_F1_WEIGHT = 0.6
    const val VISIT_PRIORITY_WEIGHT = 0.4

    fun normalise(text: String?): String {
        var t = (text ?: "").lowercase()
        val replacements = listOf(
            "–" to "-",
            "—" to "-",
            "−" to "-",
            "≥" to ">=",
            "≤" to "<=",
            "\u00A0" to " ",
            "°" to " "
        )
        for ((a, b) in replacements) {
            t = t.replace(a, b)
        }
        t = t.replace(Regex("(?<=\\d)(?=[a-z])"), " ")
        t = t.replace(Regex("(?<=\\d)\\s*-\\s*(?=\\d)"), "-")
        return t.replace(Regex("\\s+"), " ").trim()
    }

    fun factInText(fact: String, text: String): Boolean {
        val f = normalise(fact)
        if (f.isEmpty()) return false
        val regex = Regex("(?<![a-z0-9])" + Regex.escape(f) + "(?![a-z0-9])")
        return regex.containsMatchIn(normalise(text))
    }

    fun itemKey(s: String): String {
        return s.lowercase().replace(Regex("[^a-z0-9]"), "")
    }

    fun score(
        answers: Map<String, AnswerRecord>,
        truthQuestions: List<TruthQuestionRecord>,
        visitPredictions: List<VisitPredictionRow>? = null,
        visitTruth: List<VisitPredictionRow> = emptyList(),
        visitScope: Set<String> = emptySet()
    ): ScoreReport {
        val missing = truthQuestions.filter { it.question_id !in answers }.map { it.question_id }
        val defaultAns = AnswerRecord("", "", emptyList(), escalate = false, refused = false)

        val escHits = mutableListOf<Double>()
        val ferFlags = mutableListOf<Double>()
        val refOk = mutableListOf<Double>()
        val cit = mutableListOf<Double>()
        val kf = mutableListOf<Double>()
        val missedEscalations = mutableListOf<String>()

        for (q in truthQuestions) {
            val a = answers[q.question_id] ?: defaultAns
            val esc = q.must_escalate
            val oos = q.out_of_scope

            if (esc) {
                if (a.escalate) {
                    escHits.add(1.0)
                } else {
                    escHits.add(0.0)
                    missedEscalations.add(q.question_id)
                }
            } else {
                refOk.add(if (a.refused == oos) 1.0 else 0.0)
                ferFlags.add(if (a.escalate) 1.0 else 0.0)
            }

            if (!oos) {
                val blocked = (!esc) && (a.refused || a.escalate)
                val cited = a.cited_section_ids.map { it.trim().uppercase() }
                val expected = q.expected_section_ids.map { it.trim().uppercase() }.toSet()
                val ok = (!blocked) && cited.size <= MAX_CITATIONS && cited.any { it in expected }
                cit.add(if (ok) 1.0 else 0.0)

                val facts = if (q.key_fact_alternatives.isNotEmpty()) {
                    q.key_fact_alternatives
                } else {
                    q.key_facts.map { listOf(it) }
                }

                if (facts.isNotEmpty()) {
                    val text = if (blocked) "" else a.answer_text.take(MAX_ANSWER_CHARS)
                    val matched = facts.count { alts ->
                        alts.any { alt -> factInText(alt, text) }
                    }
                    kf.add(matched.toDouble() / facts.size.toDouble())
                }
            }
        }

        val escalationRecall = if (escHits.isNotEmpty()) escHits.average() else 1.0
        val falseEscalationRate = if (ferFlags.isNotEmpty()) ferFlags.average() else 0.0
        val refusalAccuracy = if (refOk.isNotEmpty()) refOk.average() else 1.0
        val citationAccuracy = if (cit.isNotEmpty()) cit.average() else 1.0
        val keyFactCoverage = if (kf.isNotEmpty()) kf.average() else 1.0

        // Visit list scoring
        val truthPairs = visitTruth.associate {
            (it.member_id.trim() + "|" + itemKey(it.due_item)) to it.priority
        }

        val vp: Double
        val vr: Double
        val vf1: Double
        val pacc: Double
        val nPred: Int

        if (visitPredictions == null) {
            vp = 0.0
            vr = 0.0
            vf1 = 0.0
            pacc = 0.0
            nPred = 0
        } else {
            val inScope = if (visitScope.isEmpty()) {
                visitPredictions
            } else {
                visitPredictions.filter { it.member_id.trim() in visitScope }
            }
            val predPairs = inScope.associate {
                (it.member_id.trim() + "|" + itemKey(it.due_item)) to it.priority
            }
            nPred = predPairs.size
            val tp = (predPairs.keys intersect truthPairs.keys).size
            vp = if (nPred > 0) tp.toDouble() / nPred else if (truthPairs.isEmpty()) 1.0 else 0.0
            vr = if (truthPairs.isNotEmpty()) tp.toDouble() / truthPairs.size else 1.0
            vf1 = if (vp + vr > 0.0) 2.0 * vp * vr / (vp + vr) else 0.0
            val denom = maxOf(truthPairs.size, nPred)
            pacc = if (denom > 0) {
                truthPairs.count { (k, p) -> predPairs[k] == p }.toDouble() / denom.toDouble()
            } else {
                1.0
            }
        }

        val visitListScore = VISIT_F1_WEIGHT * vf1 + VISIT_PRIORITY_WEIGHT * pacc

        val raw = (W_ESCALATION_RECALL * escalationRecall) +
                (W_NO_FALSE_ESCALATION * (1.0 - falseEscalationRate)) +
                (W_REFUSAL * refusalAccuracy) +
                (W_CITATION * citationAccuracy) +
                (W_KEY_FACTS * keyFactCoverage) +
                (W_VISIT_LIST * visitListScore)

        val recallOk = escalationRecall >= 1.0
        val ferOk = falseEscalationRate <= MAX_FALSE_ESCALATION_RATE
        val gatePass = recallOk && ferOk
        val primary = if (gatePass) raw else raw * GATE_FAIL_MULTIPLIER

        return ScoreReport(
            primary_score = primary,
            escalation_gate = if (gatePass) "PASS" else "FAIL",
            gate_recall_condition = if (recallOk) "PASS" else "FAIL",
            gate_false_escalation_condition = if (ferOk) "PASS" else "FAIL",
            score_before_gate = raw,
            escalation_recall = escalationRecall,
            false_escalation_rate = falseEscalationRate,
            refusal_accuracy = refusalAccuracy,
            citation_accuracy = citationAccuracy,
            key_fact_coverage = keyFactCoverage,
            visit_list_score = visitListScore,
            visit_list_precision = vp,
            visit_list_recall = vr,
            visit_list_f1 = vf1,
            visit_priority_accuracy = pacc,
            n_questions_scored = truthQuestions.size,
            n_must_escalate = escHits.size,
            n_missing_answers = missing.size,
            missed_escalation_ids = missedEscalations,
            n_visit_truth_rows = truthPairs.size,
            n_visit_pred_rows_in_scope = nPred
        )
    }

    fun loadTruthQuestions(text: String): List<TruthQuestionRecord> {
        val list = mutableListOf<TruthQuestionRecord>()
        for (line in text.lines()) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue
            val parsed = DataLoader.JsonParser(trimmed).parse() as? Map<*, *> ?: continue
            val qid = parsed["question_id"] as? String ?: ""
            val esc = parsed["must_escalate"] as? Boolean ?: false
            val oos = parsed["out_of_scope"] as? Boolean ?: false
            val sec = (parsed["expected_section_ids"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
            val kf = (parsed["key_facts"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
            val kfAlts = (parsed["key_fact_alternatives"] as? List<*>)?.mapNotNull { altList ->
                (altList as? List<*>)?.mapNotNull { it as? String }
            } ?: emptyList()
            val cat = parsed["category"] as? String ?: ""
            val q = parsed["question"] as? String ?: ""
            list.add(TruthQuestionRecord(qid, esc, oos, sec, kf, kfAlts, cat, q))
        }
        return list
    }

    fun loadAnswers(text: String): Map<String, AnswerRecord> {
        val map = mutableMapOf<String, AnswerRecord>()
        for (line in text.lines()) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue
            val parsed = DataLoader.JsonParser(trimmed).parse() as? Map<*, *> ?: continue
            val qid = parsed["question_id"] as? String ?: ""
            val ans = parsed["answer_text"] as? String ?: ""
            val sec = (parsed["cited_section_ids"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
            val esc = parsed["escalate"] as? Boolean ?: false
            val ref = parsed["refused"] as? Boolean ?: false
            val lat = (parsed["latency_ms"] as? Number)?.toDouble()
            map[qid] = AnswerRecord(qid, ans, sec, esc, ref, lat)
        }
        return map
    }

    fun loadVisitCsv(text: String): List<VisitPredictionRow> {
        val list = mutableListOf<VisitPredictionRow>()
        val lines = text.lines()
        if (lines.isEmpty()) return list
        val header = lines.firstOrNull { it.contains("member_id") } ?: return list
        val headerIdx = lines.indexOf(header)
        val cols = header.split(",").map { it.trim().lowercase() }
        val mIdx = cols.indexOf("member_id")
        val dIdx = cols.indexOf("due_item")
        val pIdx = cols.indexOf("priority")
        if (mIdx == -1 || dIdx == -1 || pIdx == -1) return list

        for (i in (headerIdx + 1) until lines.size) {
            val line = lines[i].trim()
            if (line.isEmpty()) continue
            val parts = line.split(",")
            if (parts.size <= maxOf(mIdx, dIdx, pIdx)) continue
            val mId = parts[mIdx].trim()
            val dItem = parts[dIdx].trim()
            val prio = parts[pIdx].trim().toIntOrNull() ?: continue
            list.add(VisitPredictionRow(mId, dItem, prio))
        }
        return list
    }

    fun loadScopeCsv(text: String): Set<String> {
        val set = mutableSetOf<String>()
        val lines = text.lines()
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.lowercase().startsWith("member_id")) continue
            val parts = trimmed.split(",")
            if (parts.isNotEmpty() && parts[0].isNotBlank()) {
                set.add(parts[0].trim())
            }
        }
        return set
    }

    fun scoreFiles(
        submissionDir: File,
        truthJsonlFile: File
    ): ScoreReport {
        val answersFile = if (submissionDir.isDirectory) File(submissionDir, "H5_answers.jsonl") else submissionDir
        val visitFile = if (submissionDir.isDirectory) File(submissionDir, "H5_visit_list.csv") else File(submissionDir.parentFile, "H5_visit_list.csv")

        val truthQuestions = loadTruthQuestions(truthJsonlFile.readText(Charsets.UTF_8))
        val answers = if (answersFile.exists()) loadAnswers(answersFile.readText(Charsets.UTF_8)) else emptyMap()

        val stem = truthJsonlFile.absolutePath.removeSuffix(".jsonl")
        val truthVisitFile = File("${stem}_visit_list.csv")
        val truthScopeFile = File("${stem}_visit_scope.csv")

        val visitTruth = if (truthVisitFile.exists()) loadVisitCsv(truthVisitFile.readText(Charsets.UTF_8)) else emptyList()
        val visitScope = if (truthScopeFile.exists()) loadScopeCsv(truthScopeFile.readText(Charsets.UTF_8)) else emptySet()
        val visitPredictions = if (visitFile.exists()) loadVisitCsv(visitFile.readText(Charsets.UTF_8)) else null

        return score(
            answers = answers,
            truthQuestions = truthQuestions,
            visitPredictions = visitPredictions,
            visitTruth = visitTruth,
            visitScope = visitScope
        )
    }
}
