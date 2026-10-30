package com.sahaaya.companion.core

object DataLoader {

    class JsonParser(private val src: String) {
        private var pos = 0
        private val len = src.length

        fun parse(): Any? {
            skipWhitespace()
            if (pos >= len) return null
            return parseValue()
        }

        private fun skipWhitespace() {
            while (pos < len) {
                val c = src[pos]
                if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                    pos++
                } else {
                    break
                }
            }
        }

        private fun parseValue(): Any? {
            skipWhitespace()
            if (pos >= len) return null
            return when (val c = src[pos]) {
                '{' -> parseObject()
                '[' -> parseArray()
                '"' -> parseString()
                't', 'f' -> parseBoolean()
                'n' -> parseNull()
                else -> {
                    if (c == '-' || c in '0'..'9') parseNumber()
                    else throw IllegalArgumentException("Unexpected char '$c' at position $pos")
                }
            }
        }

        private fun parseObject(): Map<String, Any?> {
            val map = mutableMapOf<String, Any?>()
            pos++ // skip '{'
            skipWhitespace()
            if (pos < len && src[pos] == '}') {
                pos++
                return map
            }
            while (pos < len) {
                skipWhitespace()
                val key = parseString()
                skipWhitespace()
                if (pos < len && src[pos] == ':') {
                    pos++ // skip ':'
                } else {
                    throw IllegalArgumentException("Expected ':' at position $pos")
                }
                val value = parseValue()
                map[key] = value
                skipWhitespace()
                if (pos < len && src[pos] == ',') {
                    pos++
                } else if (pos < len && src[pos] == '}') {
                    pos++
                    break
                } else {
                    throw IllegalArgumentException("Expected ',' or '}' at position $pos")
                }
            }
            return map
        }

        private fun parseArray(): List<Any?> {
            val list = mutableListOf<Any?>()
            pos++ // skip '['
            skipWhitespace()
            if (pos < len && src[pos] == ']') {
                pos++
                return list
            }
            while (pos < len) {
                val value = parseValue()
                list.add(value)
                skipWhitespace()
                if (pos < len && src[pos] == ',') {
                    pos++
                } else if (pos < len && src[pos] == ']') {
                    pos++
                    break
                } else {
                    throw IllegalArgumentException("Expected ',' or ']' at position $pos")
                }
            }
            return list
        }

        private fun parseString(): String {
            if (pos >= len || src[pos] != '"') throw IllegalArgumentException("Expected '\"' at position $pos")
            pos++ // skip opening quote
            val sb = StringBuilder()
            while (pos < len) {
                val c = src[pos++]
                if (c == '"') {
                    return sb.toString()
                } else if (c == '\\') {
                    if (pos >= len) break
                    val esc = src[pos++]
                    when (esc) {
                        '"' -> sb.append('"')
                        '\\' -> sb.append('\\')
                        '/' -> sb.append('/')
                        'b' -> sb.append('\b')
                        'f' -> sb.append('\u000c')
                        'n' -> sb.append('\n')
                        'r' -> sb.append('\r')
                        't' -> sb.append('\t')
                        'u' -> {
                            if (pos + 4 <= len) {
                                val hex = src.substring(pos, pos + 4)
                                pos += 4
                                try {
                                    sb.append(hex.toInt(16).toChar())
                                } catch (e: Exception) {
                                    sb.append('?')
                                }
                            }
                        }
                        else -> sb.append(esc)
                    }
                } else {
                    sb.append(c)
                }
            }
            return sb.toString()
        }

        private fun parseBoolean(): Boolean {
            if (src.startsWith("true", pos)) {
                pos += 4
                return true
            } else if (src.startsWith("false", pos)) {
                pos += 5
                return false
            }
            throw IllegalArgumentException("Expected boolean at position $pos")
        }

        private fun parseNull(): Any? {
            if (src.startsWith("null", pos)) {
                pos += 4
                return null
            }
            throw IllegalArgumentException("Expected null at position $pos")
        }

        private fun parseNumber(): Number {
            val start = pos
            if (pos < len && (src[pos] == '-' || src[pos] == '+')) pos++
            while (pos < len && (src[pos] in '0'..'9' || src[pos] == '.' || src[pos] == 'e' || src[pos] == 'E' || src[pos] == '-' || src[pos] == '+')) {
                pos++
            }
            val numStr = src.substring(start, pos)
            return if (numStr.contains('.') || numStr.contains('e') || numStr.contains('E')) {
                numStr.toDoubleOrNull() ?: 0.0
            } else {
                numStr.toLongOrNull() ?: 0L
            }
        }
    }

    fun parseCsv(csvText: String): List<Map<String, String>> {
        val lines = csvText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) return emptyList()

        val header = parseCsvLine(lines[0])
        val results = mutableListOf<Map<String, String>>()

        for (i in 1 until lines.size) {
            val values = parseCsvLine(lines[i])
            val row = mutableMapOf<String, String>()
            for (j in header.indices) {
                row[header[j]] = if (j < values.size) values[j] else ""
            }
            results.add(row)
        }
        return results
    }

    private fun parseCsvLine(line: String): List<String> {
        val tokens = mutableListOf<String>()
        val sb = StringBuilder()
        var insideQuotes = false

        for (c in line) {
            when {
                c == '"' -> insideQuotes = !insideQuotes
                c == ',' && !insideQuotes -> {
                    tokens.add(sb.toString().trim())
                    sb.clear()
                }
                else -> sb.append(c)
            }
        }
        tokens.add(sb.toString().trim())
        return tokens
    }

    fun parseRegister(csvText: String): List<RegisterMember> {
        val rows = parseCsv(csvText)
        return rows.map { row ->
            val familyOrHhId = row["household_id"] ?: row["family_id"] ?: ""
            RegisterMember(
                familyId = familyOrHhId,
                memberId = row["member_id"] ?: "",
                name = row["name"] ?: "",
                memberType = row["member_type"] ?: if (row["pregnant"] == "Y") "PREGNANT" else "",
                targetDate = row["target_date"] ?: row["edd_date"] ?: row["dob"] ?: "",
                lastVisitDate = row["last_visit_date"] ?: "",
                dangerFlag = row["danger_flag"] ?: row["reported_symptoms"] ?: "NONE",
                vaccinesGiven = row["vaccines_given"] ?: "",
                nextDueItem = row["next_due_item"] ?: "",
                dueDate = row["due_date"] ?: "",
                relationToHead = row["relation_to_head"] ?: "",
                sex = row["sex"] ?: "",
                dob = row["dob"] ?: "",
                pregnant = row["pregnant"] ?: "N",
                lmpDate = row["lmp_date"] ?: "",
                eddDate = row["edd_date"] ?: "",
                deliveryDate = row["delivery_date"] ?: "",
                deliveryPlace = row["delivery_place"] ?: "",
                gravida = row["gravida"] ?: "",
                parity = row["parity"] ?: "",
                lastBp = row["last_bp"] ?: "",
                lastBloodSugar = row["last_blood_sugar"] ?: "",
                reportedSymptoms = row["reported_symptoms"] ?: "",
                riskFlags = row["risk_flags"] ?: "",
                phone = row["phone"] ?: "",
                assignedAshaId = row["assigned_asha_id"] ?: ""
            )
        }
    }

    fun parseProtocolChunks(jsonText: String): List<ProtocolChunk> {
        val root = JsonParser(jsonText).parse() as? List<*> ?: return emptyList()
        val list = mutableListOf<ProtocolChunk>()
        for (item in root) {
            val map = item as? Map<*, *> ?: continue
            val id = map["id"]?.toString() ?: ""
            val title = map["title"]?.toString() ?: ""
            val lang = map["language"]?.toString() ?: "en"
            val text = map["text"]?.toString() ?: ""
            val docName = (map["document_name"] ?: map["source_document"])?.toString() ?: ""
            val srcUrl = (map["source_reference"] ?: map["source_url"])?.toString() ?: ""
            val pageNum = (map["page_number"] as? Number)?.toInt() ?: 1
            val parentId = map["parent_id"]?.toString() ?: ""
            val keywords = (map["keywords"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
            val approvedSyns = (map["approved_synonyms"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
            val protocolVersion = map["protocol_version"]?.toString() ?: "v1.0"
            val contentHash = map["content_hash"]?.toString() ?: ""

            if (id.isNotBlank()) {
                list.add(
                    ProtocolChunk(
                        id = id,
                        title = title,
                        language = lang,
                        text = text,
                        sourceDocument = docName,
                        sourceUrl = srcUrl,
                        pageNumber = pageNum,
                        parentId = parentId,
                        keywords = keywords,
                        approvedSynonyms = approvedSyns,
                        protocolVersion = protocolVersion,
                        contentHash = contentHash
                    )
                )
            }
        }
        return list
    }

    fun parseDangerRules(jsonText: String): List<DangerRule> {
        val root = JsonParser(jsonText).parse() as? List<*> ?: return emptyList()
        val list = mutableListOf<DangerRule>()
        for (item in root) {
            val map = item as? Map<*, *> ?: continue
            val id = map["id"]?.toString() ?: ""
            val chunkId = map["chunk_id"]?.toString() ?: ""
            val category = map["category"]?.toString() ?: ""
            val dangerSign = map["danger_sign"]?.toString() ?: ""
            val action = map["action"]?.toString() ?: ""
            val citation = map["source_citation"]?.toString() ?: ""
            val note = map["note"]?.toString() ?: ""

            val phrasingsRaw = map["phrasings"] as? Map<*, *>
            val phrasings = mutableMapOf<String, List<String>>()
            if (phrasingsRaw != null) {
                for ((k, v) in phrasingsRaw) {
                    val key = k?.toString() ?: continue
                    val vals = (v as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                    phrasings[key] = vals
                }
            }

            if (id.isNotBlank()) {
                list.add(
                    DangerRule(
                        id = id,
                        chunkId = chunkId,
                        category = category,
                        dangerSign = dangerSign,
                        action = action,
                        sourceCitation = citation,
                        note = note,
                        phrasings = phrasings
                    )
                )
            }
        }
        return list
    }
}
