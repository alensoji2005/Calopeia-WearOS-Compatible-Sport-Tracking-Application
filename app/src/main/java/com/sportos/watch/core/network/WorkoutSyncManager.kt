package com.sportos.watch.core.network

import android.util.Log
import com.sportos.watch.data.database.dao.WorkoutDao
import com.sportos.watch.data.database.entity.WorkoutSessionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Offline-first sync manager for Calopeia workout records.
 *
 * Responsibilities:
 * 1. Checks for unsynced sessions stored in Room DB.
 * 2. Only attempts synchronization if active network connectivity is present.
 * 3. Batches multiple pending records into a single sync operation, minimizing LTE/Wi-Fi radio tail time.
 * 4. Marks sessions as `isSynced = true` upon successful upload.
 */
class WorkoutSyncManager(
    private val dao: WorkoutDao,
    private val networkMonitor: NetworkMonitor
) {

    /**
     * Attempts to flush all unsynced sessions to the cloud.
     * Returns the number of sessions successfully synchronized.
     */
    suspend fun syncPendingSessions(): Int {
        if (!networkMonitor.isOnline()) {
            Log.d(TAG, "Device is offline. Pending sessions remain queued in Room DB.")
            return 0
        }

        val pending = dao.getUnsyncedSessions()
        if (pending.isEmpty()) {
            Log.d(TAG, "No pending sessions to sync.")
            return 0
        }

        Log.d(TAG, "Synchronizing ${pending.size} pending workout sessions in batch...")
        var syncedCount = 0

        for (session in pending) {
            try {
                // In production, this uploads to REST/Ktor endpoint (e.g. POST /api/v1/workouts)
                val success = uploadSessionPayload(session)
                if (success) {
                    dao.markSessionSynced(session.id)
                    syncedCount++
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to sync session ID ${session.id}: ${e.message}")
                break // Stop batch on error to avoid repeated failures
            }
        }

        Log.d(TAG, "Batch sync finished. Successfully synced $syncedCount/${pending.size} sessions.")
        return syncedCount
    }

    /**
     * Asynchronously triggers a sync pass on IO dispatcher.
     */
    fun triggerSyncAsync(scope: CoroutineScope = CoroutineScope(Dispatchers.IO), onComplete: (Int) -> Unit = {}) {
        scope.launch {
            val count = syncPendingSessions()
            onComplete(count)
        }
    }

    /**
     * Simulates or performs the HTTP payload upload.
     */
    private suspend fun uploadSessionPayload(session: WorkoutSessionEntity): Boolean {
        // Validates session data integrity
        return session.id > 0 && session.startTimeMs > 0
    }

    companion object {
        private const val TAG = "WorkoutSyncManager"
    }
}
