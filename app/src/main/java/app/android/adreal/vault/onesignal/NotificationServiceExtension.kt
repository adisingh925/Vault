package app.android.adreal.vault.onesignal

import android.content.Context
import android.util.Log
import app.android.adreal.vault.database.Database
import app.android.adreal.vault.database.Database_Impl
import app.android.adreal.vault.model.Data
import app.android.adreal.vault.model.Item
import app.android.adreal.vault.model.SaltModel
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
        Log.d("NotificationServiceExtension", "One Signal Notification Received")
        val notification: IDisplayableMutableNotification = event.notification
        val data = Gson().fromJson(notification.additionalData.toString(), Data::class.java)

        if(data.type == 1){
            fetchAndStoreData(data.deviceId, event.context)
        }

        event.preventDefault()
    }

    private fun fetchAndStoreData(userId : String, context: Context) {
        firestore.collection(Constants.COLLECTION_NAME).document(userId).get().addOnSuccessListener { document ->
            if (document.exists()) {
                val encryptedNotesMap = document.data
                encryptedNotesMap?.forEach { (key, value) ->
                    if (key == Constants.SALT) {
                        CoroutineScope(Dispatchers.IO).launch {
                            Database.getDatabase(context).dao().insertSalt(SaltModel(userId, value.toString()))
                        }
                    } else {
                        val decryptedItem = Gson().fromJson(value.toString(), Item::class.java)

                        CoroutineScope(Dispatchers.IO).launch {
                            Database.getDatabase(context).dao().insertWithReplace(decryptedItem)
                        }
                    }
                }
            }
        }
    }
}