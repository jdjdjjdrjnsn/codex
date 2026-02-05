package com.codex.cleaner.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.codex.cleaner.data.AndroidCleanerRepository

class ScheduledScanWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val repository = AndroidCleanerRepository(applicationContext)
        return runCatching {
            repository.scan()
            Result.success()
        }.getOrElse {
            Result.retry()
        }
    }
}
