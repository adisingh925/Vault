package app.android.adreal.vault.workmanager

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import app.android.adreal.vault.sharedpreferences.SharedPreferences
import app.android.adreal.vault.utils.GlobalFunctions

class PingWorker(context: Context, workerParameters: WorkerParameters) : CoroutineWorker(context, workerParameters){
    override suspend fun doWork(): Result {
        Log.d("PingWorker", "doWork: Running PingWorker")
        return try {
            SharedPreferences.init(applicationContext)
            GlobalFunctions().deviceBroadcast()
            Result.success()
        }catch (e: Exception) {
            Result.failure()
        }
    }
}