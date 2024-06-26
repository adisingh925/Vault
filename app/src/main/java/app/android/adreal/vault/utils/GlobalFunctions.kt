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
     */

    fun insertFirestore(data: Item, userId : String, firestore: FirebaseFirestore) {
        val encryptedNotes = Gson().toJson(data)
        val encryptedNotesMap = mapOf(data.id.toString() to encryptedNotes)
        firestore.collection(Constants.COLLECTION_NAME).document(userId).set(encryptedNotesMap, SetOptions.merge()).addOnSuccessListener {
            Log.d("GlobalFunctions", "Data Inserted Successfully")
            GlobalFunctions().sendNotification(
                "Syncing Network - Inserting Data", Data(
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
        }.addOnFailureListener {
            Log.d("GlobalFunctions", "Data Insertion Failed")
        }
    }

    fun saveSaltInFirestore(salt: String, userId : String, firestore: FirebaseFirestore) {
        val saltMap = mapOf(Constants.SALT to salt)
        firestore.collection(Constants.COLLECTION_NAME).document(userId).set(saltMap, SetOptions.merge()).addOnSuccessListener {
            Log.d("GlobalFunctions", "Salt Inserted Successfully")
            GlobalFunctions().sendNotification(
                "Syncing Network - Storing Salt", Data(
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
        }.addOnFailureListener {
            Log.d("GlobalFunctions", "Salt Insertion Failed")
        }
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
}