package app.android.adreal.vault.onesignal

import android.util.Log
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import app.android.adreal.vault.database.Database
import app.android.adreal.vault.model.Data
import app.android.adreal.vault.model.DeviceModel
import app.android.adreal.vault.sharedpreferences.SharedPreferences
import app.android.adreal.vault.utils.Constants
import app.android.adreal.vault.utils.GlobalFunctions
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.gson.Gson
import com.onesignal.notifications.IDisplayableMutableNotification
import com.onesignal.notifications.INotificationReceivedEvent
import com.onesignal.notifications.INotificationServiceExtension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationServiceExtension : INotificationServiceExtension {

    private val firestore = Firebase.firestore

    override fun onNotificationReceived(event: INotificationReceivedEvent) {
        SharedPreferences.init(event.context)
        Log.d(
            "NotificationServiceExtension",
            "One Signal Notification Received : ${event.notification}"
        )
        val notification: IDisplayableMutableNotification = event.notification

        if (notification.additionalData != null && notification.additionalData.toString().length > 2) {
            val data = Gson().fromJson(notification.additionalData.toString(), Data::class.java)

            when (data.type) {
                0 -> {
                    Log.d("NotificationServiceExtension", "Storing Data!")
                    if(data.deviceId == SharedPreferences.read(Constants.USER_ID, "")){
                        return
                    }

                    CoroutineScope(Dispatchers.IO).launch {
                        Database.getDatabase(event.context).dao().insertDeviceWithReplace(
                            DeviceModel(
                                data.deviceId,
                                System.currentTimeMillis()
                            )
                        )
                    }.invokeOnCompletion {
                        try {
                            GlobalFunctions().fetchAndStoreData(
                                data.deviceId,
                                event.context,
                                firestore
                            )
                            Log.d("NotificationServiceExtension", "Data fetched and stored!")
                        } catch (e: Exception) {
                            Log.d("NotificationServiceExtension", "Error: ${e.message}")
                        }
                    }
                }

                1 -> {
                    //upload data back to firestore for the requested device
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            Log.d("NotificationServiceExtension", "Uploading data to firestore")
                            val topFiveDevices = Database.getDatabase(event.context).dao().readTopFiveDevices()
                            for (device in topFiveDevices) {
                                if (device.deviceId == SharedPreferences.read(Constants.USER_ID, "")) {
                                    Log.d("NotificationServiceExtension", "Device found!")
                                    val deviceData = Database.getDatabase(event.context).dao()
                                        .readWithoutLiveData(data.deviceId)
                                    val salt = Database.getDatabase(event.context).dao()
                                        .readSalt(data.deviceId)

                                    if (deviceData.isNotEmpty() && salt.isNotEmpty()) {
                                        Log.d(
                                            "NotificationServiceExtension",
                                            "Data found!")

                                        for (item in deviceData) {
                                            GlobalFunctions().insertFirestore(
                                                item,
                                                data.deviceId,
                                                firestore
                                            )
                                        }

                                        GlobalFunctions().saveSaltInFirestore(
                                            salt,
                                            data.deviceId,
                                            firestore
                                        )
                                    } else {
                                        Log.d(
                                            "NotificationServiceExtension",
                                            "No data found for device: ${data.deviceId}"
                                        )
                                    }
                                }
                            }
                        }catch (e: Exception) {
                            Log.d("NotificationServiceExtension", "Error: ${e.message}")
                        }
                    }
                }
            }
        }

//        event.preventDefault()
    }
}