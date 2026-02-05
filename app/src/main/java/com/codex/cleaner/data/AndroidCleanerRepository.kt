package com.codex.cleaner.data

import android.content.Context
import com.codex.cleaner.domain.CleanableItem
import com.codex.cleaner.domain.CleaningSummary
import com.codex.cleaner.domain.ItemCategory
import com.codex.cleaner.domain.RiskClassifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class AndroidCleanerRepository(
    private val context: Context,
    private val classifier: RiskClassifier = RiskClassifier()
) : CleanerRepository {

    override suspend fun scan(): List<CleanableItem> = withContext(Dispatchers.IO) {
        val candidates = mutableListOf<CleanableItem>()

        collectFromDirectory(context.cacheDir, ItemCategory.CACHE, candidates)
        context.externalCacheDir?.let { collectFromDirectory(it, ItemCategory.TEMP, candidates) }

        val downloads = context.getExternalFilesDir(null)
        if (downloads != null) {
            collectLargeFiles(downloads, candidates)
            collectEmptyFolders(downloads, candidates)
        }

        candidates.sortedByDescending { it.sizeBytes }
    }

    override suspend fun delete(items: List<CleanableItem>): CleaningSummary = withContext(Dispatchers.IO) {
        var cleanedBytes = 0L
        items.filter { it.isSelected && it.riskLevel != com.codex.cleaner.domain.RiskLevel.RISKY }
            .forEach { item ->
                val file = File(item.path)
                if (file.exists() && file.deleteRecursively()) {
                    cleanedBytes += item.sizeBytes
                }
            }

        CleaningSummary(
            totalScannedBytes = items.sumOf { it.sizeBytes },
            reclaimableBytes = items.filter { it.isSelected }.sumOf { it.sizeBytes },
            cleanedBytes = cleanedBytes,
            topCategories = items.groupBy { it.category }.mapValues { entry -> entry.value.sumOf { it.sizeBytes } }
        )
    }

    private fun collectFromDirectory(
        directory: File,
        category: ItemCategory,
        output: MutableList<CleanableItem>
    ) {
        if (!directory.exists()) return

        directory.walkTopDown()
            .maxDepth(3)
            .filter { it.isFile }
            .forEach { file ->
                output += CleanableItem(
                    id = UUID.randomUUID().toString(),
                    name = file.name,
                    path = file.absolutePath,
                    sizeBytes = file.length(),
                    category = category,
                    riskLevel = classifier.classify(category, file.absolutePath)
                )
            }
    }

    private fun collectLargeFiles(root: File, output: MutableList<CleanableItem>) {
        root.walkTopDown()
            .maxDepth(4)
            .filter { it.isFile && it.length() > LARGE_FILE_THRESHOLD_BYTES }
            .forEach { file ->
                output += CleanableItem(
                    id = UUID.randomUUID().toString(),
                    name = file.name,
                    path = file.absolutePath,
                    sizeBytes = file.length(),
                    category = ItemCategory.LARGE_FILE,
                    riskLevel = classifier.classify(ItemCategory.LARGE_FILE, file.absolutePath),
                    isSelected = false
                )
            }
    }

    private fun collectEmptyFolders(root: File, output: MutableList<CleanableItem>) {
        root.walkTopDown()
            .maxDepth(4)
            .filter { it.isDirectory && it.listFiles().isNullOrEmpty() }
            .forEach { folder ->
                output += CleanableItem(
                    id = UUID.randomUUID().toString(),
                    name = folder.name.ifBlank { "Empty Folder" },
                    path = folder.absolutePath,
                    sizeBytes = 0L,
                    category = ItemCategory.EMPTY_FOLDER,
                    riskLevel = classifier.classify(ItemCategory.EMPTY_FOLDER, folder.absolutePath)
                )
            }
    }

    private companion object {
        const val LARGE_FILE_THRESHOLD_BYTES = 20L * 1024 * 1024
    }
}
