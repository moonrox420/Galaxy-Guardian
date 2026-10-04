package com.example.galaxyguardian

import com.example.galaxyguardian.ui.components.DiffCalculator
import com.example.galaxyguardian.ui.components.DiffLine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DiffCalculatorTest {

    @Test
    fun testComputeDiff_identicalStrings_returnsUnchanged() {
        val code = "val x = 1\nval y = 2"
        val diff = DiffCalculator.computeDiff(code, code)

        assertEquals(2, diff.size)
        assertTrue(diff.all { it is DiffLine.Unchanged })
    }

    @Test
    fun testComputeDiff_additionAndDeletion() {
        val oldCode = "val x = 1"
        val newCode = "val x = 2"

        val diff = DiffCalculator.computeDiff(oldCode, newCode)
        assertTrue(diff.any { it is DiffLine.Deletion })
        assertTrue(diff.any { it is DiffLine.Addition })
    }
}
