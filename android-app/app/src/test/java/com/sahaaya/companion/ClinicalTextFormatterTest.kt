package com.sahaaya.companion

import com.sahaaya.companion.core.ClinicalTextFormatter
import org.junit.Assert.*
import org.junit.Test

class ClinicalTextFormatterTest {

    @Test
    fun testEmptyString() {
        val points = ClinicalTextFormatter.formatToPoints("")
        assertTrue("Empty string should return empty points list", points.isEmpty())
        val whitespacePoints = ClinicalTextFormatter.formatToPoints("   \n\t  ")
        assertTrue("Whitespace should return empty points list", whitespacePoints.isEmpty())
    }

    @Test
    fun testMuacThreeColumnTable() {
        val muacTableText = """
            NUT-1.2 MUAC tape measurement cutoffs.
            | MUAC | Nutritional Status | Colour |
            |---|---|---|
            | < 11.5 cm | Severe Acute Malnutrition (SAM) | Red |
            | 11.5 cm - 12.5 cm | Moderate Acute Malnutrition (MAM) | Yellow |
            | >= 12.5 cm | Normal (no acute malnutrition) | Green |
        """.trimIndent()

        val points = ClinicalTextFormatter.formatToPoints(muacTableText)
        assertFalse(points.isEmpty())
        assertTrue("Must contain formatted Red row", points.any { it.contains("< 11.5 cm [Red]: Severe Acute Malnutrition") })
        assertTrue("Must contain formatted Yellow row", points.any { it.contains("11.5 cm - 12.5 cm [Yellow]: Moderate Acute Malnutrition") })
        assertTrue("Must contain formatted Green row", points.any { it.contains(">= 12.5 cm [Green]: Normal") })
    }

    @Test
    fun testHbncVisitDaysTable() {
        val hbncText = """
            HBNC-1.1 Schedule of home visits.
            | Place of delivery | Number of home visits | Visit days |
            |---|---|---|
            | Home delivery | 7 visits | Days 1, 3, 7, 14, 21, 28, 42 |
            | Institutional delivery | 6 visits | Days 3, 7, 14, 21, 28, 42 |
        """.trimIndent()

        val points = ClinicalTextFormatter.formatToPoints(hbncText)
        assertFalse(points.isEmpty())
        assertTrue("Must format home delivery visits", points.any { it.contains("Home delivery (7 visits): Days 1, 3, 7, 14, 21, 28, 42") })
        assertTrue("Must format institutional delivery visits", points.any { it.contains("Institutional delivery (6 visits): Days 3, 7, 14, 21, 28, 42") })
    }

    @Test
    fun testBulletListsAndBolding() {
        val narrative = """
            ANC-2.3 Calcium supplementation rules.
            - **First rule**: Never take calcium on an empty stomach.
            - **Second rule**: Take with food or breakfast.
            - Drink plenty of clean water throughout pregnancy.
        """.trimIndent()

        val points = ClinicalTextFormatter.formatToPoints(narrative)
        assertFalse(points.isEmpty())
        assertTrue(points.any { it.contains("First rule: Never take calcium on an empty stomach") || it.contains("Never take calcium on an empty stomach") })
        assertFalse("Markdown bold asterisks must be stripped", points.any { it.contains("**") })
    }

    @Test
    fun testDedupeAgainstSummary() {
        val rawText = """
            ANC-2.3 Calcium tablets.
            Calcium tablets must be taken with meals, never on an empty stomach.
            Additional point: Do not take calcium and iron together at the exact same moment.
        """.trimIndent()

        val summary = "Calcium tablets must be taken with meals, never on an empty stomach (ANC-2.3)."
        val points = ClinicalTextFormatter.formatToPoints(rawText, excludeText = summary)

        assertFalse(points.isEmpty())
        assertFalse("Repeated short summary must be excluded", points.any { it.contains("Calcium tablets must be taken with meals") })
        assertTrue("Non-duplicated point must be kept", points.any { it.contains("Do not take calcium and iron together") })
    }
}
