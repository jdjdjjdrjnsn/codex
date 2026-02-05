package com.codex.cleaner.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class RiskClassifierTest {

    private val classifier = RiskClassifier()

    @Test
    fun `cache files are safe`() {
        val level = classifier.classify(ItemCategory.CACHE, "/storage/emulated/0/Android/data/app/cache/file.tmp")
        assertEquals(RiskLevel.SAFE, level)
    }

    @Test
    fun `system files are risky`() {
        val level = classifier.classify(ItemCategory.LARGE_FILE, "/system/lib/file.so")
        assertEquals(RiskLevel.RISKY, level)
    }
}
