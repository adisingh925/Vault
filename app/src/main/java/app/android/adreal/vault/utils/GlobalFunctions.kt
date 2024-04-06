package app.android.adreal.vault.utils

import android.util.Log
import app.android.adreal.vault.model.Contents
import app.android.adreal.vault.model.Data
import app.android.adreal.vault.model.Filter
import app.android.adreal.vault.model.NotificationRequest
import app.android.adreal.vault.model.NotificationResponse
import app.android.adreal.vault.retrofit.ApiClient
import app.android.adreal.vault.sharedpreferences.SharedPreferences
import retrofit2.Call
import retrofit2.Response

class GlobalFunctions {

    fun getNextPrimaryKey(): Int {
        SharedPreferences.write(Constants.PRIMARY_KEY, getCurrentPrimaryKey() + 1)
        return getCurrentPrimaryKey()
    }

    fun getCurrentPrimaryKey(): Int {
        return SharedPreferences.read(Constants.PRIMARY_KEY, -1)
    }

    fun setCurrentPrimaryKey(primaryKey: Int) {
        SharedPreferences.write(Constants.PRIMARY_KEY, primaryKey)
    }

    /**
     * 1 - ping
     * 2 - request
     * 3 - response
     */

    fun deviceBroadcast() {
        val request = ApiClient.apiService.sendNotification(
            NotificationRequest(
                Constants.ONE_SIGNAL_APP_ID,
                Contents("Syncing Network"),
                Data(SharedPreferences.read(Constants.USER_ID, "").toString(), "1"),
                listOf(
                    Filter(
                        "tag",
                        Constants.ONE_SIGNAL_GENERAL_TAG,
                        "=",
                        Constants.ONE_SIGNAL_GENERAL_TAG
                    )
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
                Data(SharedPreferences.read(Constants.USER_ID, "").toString(), "2"),
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

    fun dataResponse() {
        val request = ApiClient.apiService.sendNotification(
            NotificationRequest(
                Constants.ONE_SIGNAL_APP_ID,
                Contents("Sending Data"),
                Data(SharedPreferences.read(Constants.USER_ID, "").toString(), "3"),
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