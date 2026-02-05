package com.codex.cleaner.domain

class RiskClassifier {
    fun classify(category: ItemCategory, path: String): RiskLevel {
        val normalizedPath = path.lowercase()

        return when {
            normalizedPath.contains("/android/data") && category != ItemCategory.CACHE -> RiskLevel.REVIEW
            normalizedPath.contains("/system") || normalizedPath.contains("/vendor") -> RiskLevel.RISKY
            category == ItemCategory.CACHE || category == ItemCategory.TEMP || category == ItemCategory.EMPTY_FOLDER -> RiskLevel.SAFE
            category == ItemCategory.SCREENSHOT || category == ItemCategory.CHAT_MEDIA -> RiskLevel.REVIEW
            else -> RiskLevel.REVIEW
        }
    }
}
