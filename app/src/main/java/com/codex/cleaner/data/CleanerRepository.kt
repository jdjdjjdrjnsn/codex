package com.codex.cleaner.data

import com.codex.cleaner.domain.CleanableItem
import com.codex.cleaner.domain.CleaningSummary

interface CleanerRepository {
    suspend fun scan(): List<CleanableItem>
    suspend fun delete(items: List<CleanableItem>): CleaningSummary
}
