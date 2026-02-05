package com.codex.cleaner.domain

enum class RiskLevel {
    SAFE,
    REVIEW,
    RISKY
}

enum class ItemCategory {
    CACHE,
    TEMP,
    EMPTY_FOLDER,
    APP_RESIDUE,
    LARGE_FILE,
    DUPLICATE_MEDIA,
    SIMILAR_MEDIA,
    SCREENSHOT,
    CHAT_MEDIA
}

data class CleanableItem(
    val id: String,
    val name: String,
    val path: String,
    val sizeBytes: Long,
    val category: ItemCategory,
    val riskLevel: RiskLevel,
    val isSelected: Boolean = riskLevel == RiskLevel.SAFE
)
