package app.android.adreal.vault.workmanager

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.android.adreal.vault.database.Database
import app.android.adreal.vault.utils.GlobalFunctions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class DownloadAndSaveWorker(context: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(context, workerParameters) {

    private val firestore = Firebase.firestore
    override suspend fun doWork(): Result {
        Log.d("DownloadAndSaveWorker", "doWork: Running DownloadAndSaveWorker")
        return try {
            val deviceIds = Database.getDatabase(applicationContext).dao().readDevices()

            for (deviceId in deviceIds) {
                GlobalFunctions().fetchAndStoreData(deviceId.deviceId, applicationContext, firestore)
            }
            Result.success()
        }catch (e: Exception) {
            Result.failure()
        }
    }
}