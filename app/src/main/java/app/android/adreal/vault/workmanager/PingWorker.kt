package app.android.adreal.vault.workmanager

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import app.android.adreal.vault.model.Data
import app.android.adreal.vault.model.Filter
import app.android.adreal.vault.sharedpreferences.SharedPreferences
import app.android.adreal.vault.utils.Constants
import app.android.adreal.vault.utils.GlobalFunctions

class PingWorker(context: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {
        Log.d("PingWorker", "doWork: Running PingWorker")
        return try {
            SharedPreferences.init(applicationContext)
            GlobalFunctions().sendNotification(
                "Syncing Network", Data(
                    SharedPreferences.read(
                        Constants.USER_ID, ""
                    ).toString(), 0
                ), Filter(
                    "tag",
                    Constants.ONE_SIGNAL_GENERAL_TAG,
                    "=",
                    Constants.ONE_SIGNAL_GENERAL_TAG
                )
            )
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}