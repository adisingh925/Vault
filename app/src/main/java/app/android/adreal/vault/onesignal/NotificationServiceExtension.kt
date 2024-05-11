package app.android.adreal.vault.onesignal

import android.util.Log
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
        Log.d("NotificationServiceExtension", "One Signal Notification Received : ${event.notification}")
        val notification: IDisplayableMutableNotification = event.notification

        if (notification.additionalData != null && notification.additionalData.toString().length > 2) {
            val data = Gson().fromJson(notification.additionalData.toString(), Data::class.java)

            if (data.type == 0) {
                CoroutineScope(Dispatchers.IO).launch {
                    Database.getDatabase(event.context).dao().insertDeviceWithReplace(
                        DeviceModel(
                            data.deviceId,
                            System.currentTimeMillis()
                        )
                    )
                }
            } else if (data.type == 1) {
                //upload data back to firestore for the requested device
                CoroutineScope(Dispatchers.IO).launch {
                    val topFiveDevices = Database.getDatabase(event.context).dao().readTopFiveDevices()
                    for (device in topFiveDevices) {
                        if(device.deviceId == SharedPreferences.read(Constants.USER_ID, "")){
                            val deviceData = Database.getDatabase(event.context).dao().readWithoutLiveData(data.deviceId)
                            val salt = Database.getDatabase(event.context).dao().readSalt(data.deviceId)

                            if(deviceData.isNotEmpty() && salt.isNotEmpty()){
                                for(item in deviceData){
                                    GlobalFunctions().insertFirestore(item, data.deviceId, firestore)
                                }

                                GlobalFunctions().saveSaltInFirestore(salt, data.deviceId, firestore)
                            }else{
                                Log.d("NotificationServiceExtension", "No data found for device: ${data.deviceId}")
                            }
                        }
                    }
                }
            }
        }

        event.preventDefault()
    }
}