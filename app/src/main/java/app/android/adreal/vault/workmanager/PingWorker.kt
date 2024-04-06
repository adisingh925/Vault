package app.android.adreal.vault.workmanager

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import app.android.adreal.vault.utils.GlobalFunctions

class PingWorker(context: Context, workerParameters: WorkerParameters) : Worker(context, workerParameters){
    override fun doWork(): Result {
        return try {
            GlobalFunctions().deviceBroadcast()
            Result.success()
        }catch (e: Exception) {
            Result.failure()
        }
    }
}