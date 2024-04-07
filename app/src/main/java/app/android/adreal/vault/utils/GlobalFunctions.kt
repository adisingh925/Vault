package app.android.adreal.vault.utils

import android.content.Context
import android.util.Log
import app.android.adreal.vault.database.Database
import app.android.adreal.vault.model.Contents
import app.android.adreal.vault.model.Data
import app.android.adreal.vault.model.Filter
import app.android.adreal.vault.model.Item
import app.android.adreal.vault.model.NotificationRequest
import app.android.adreal.vault.model.NotificationResponse
import app.android.adreal.vault.model.SaltModel
import app.android.adreal.vault.retrofit.ApiClient
import app.android.adreal.vault.sharedpreferences.SharedPreferences
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response

class GlobalFunctions {

    /**
     * 0 - device broadcast
     * 1 - request
     * 2 - response
     */

    fun insertFirestore(data: Item, userId : String, firestore: FirebaseFirestore) {
        val encryptedNotes = Gson().toJson(data)
        val encryptedNotesMap = mapOf(data.id.toString() to encryptedNotes)
        firestore.collection(Constants.COLLECTION_NAME).document(userId).set(encryptedNotesMap, SetOptions.merge())
    }

    fun saveSaltInFirestore(salt: String, userId : String, firestore: FirebaseFirestore) {
        val saltMap = mapOf(Constants.SALT to salt)
        firestore.collection(Constants.COLLECTION_NAME).document(userId).set(saltMap, SetOptions.merge())
    }

    fun sendNotification(content : String, data : Data, filter : Filter){
        val request = ApiClient.apiService.sendNotification(
            NotificationRequest(
                Constants.ONE_SIGNAL_APP_ID,
                Contents(content),
                data,
                listOf(
                    filter
                )
            )
        )

        request.enqueue(object : retrofit2.Callback<NotificationResponse> {
            override fun onResponse(
                call: Call<NotificationResponse>,
                response: Response<NotificationResponse>
            ) {
                Log.d("MainActivity", "Notification Sent!")
            }

            override fun onFailure(call: Call<NotificationResponse>, t: Throwable) {
                Log.d("MainActivity", "Notification Failed!")
            }
        })
    }

    fun dataRequest() {
        val request = ApiClient.apiService.sendNotification(
            NotificationRequest(
                Constants.ONE_SIGNAL_APP_ID,
                Contents("Requesting Data"),
                Data(SharedPreferences.read(Constants.USER_ID, "").toString(), 2),
                listOf(
                    Filter(
                        "tag",
                        "userId",
                        "=",
                        SharedPreferences.read(Constants.USER_ID, "").toString()
                    )
                )
            )
        )

        request.enqueue(object : retrofit2.Callback<NotificationResponse> {
            override fun onResponse(
                call: Call<NotificationResponse>,
                response: Response<NotificationResponse>
            ) {
                Log.d("MainActivity", "Data Request Sent!")
            }

            override fun onFailure(call: Call<NotificationResponse>, t: Throwable) {
                Log.d("MainActivity", "Data Request Failed!")
            }
        })
    }

    fun fetchAndStoreData(userId: String, context: Context, firestore: FirebaseFirestore) {
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

    fun dataResponse() {
        val request = ApiClient.apiService.sendNotification(
            NotificationRequest(
                Constants.ONE_SIGNAL_APP_ID,
                Contents("Sending Data"),
                Data(SharedPreferences.read(Constants.USER_ID, "").toString(), 3),
                listOf(
                    Filter(
                        "tag",
                        "userId",
                        "=",
                        SharedPreferences.read(Constants.USER_ID, "").toString()
                    )
                )
            )
        )

        request.enqueue(object : retrofit2.Callback<NotificationResponse> {
            override fun onResponse(
                call: Call<NotificationResponse>,
                response: Response<NotificationResponse>
            ) {
                Log.d("MainActivity", "Data Response Sent!")
            }

            override fun onFailure(call: Call<NotificationResponse>, t: Throwable) {
                Log.d("MainActivity", "Data Response Failed!")
            }
        })
    }
}