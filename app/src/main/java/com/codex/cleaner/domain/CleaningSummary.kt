package com.codex.cleaner.domain

data class CleaningSummary(
    val totalScannedBytes: Long,
    val reclaimableBytes: Long,
    val cleanedBytes: Long,
    val topCategories: Map<ItemCategory, Long>
)
