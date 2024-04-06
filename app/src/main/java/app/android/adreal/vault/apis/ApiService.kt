package app.android.adreal.vault.apis

import app.android.adreal.vault.model.NotificationRequest
import app.android.adreal.vault.model.NotificationResponse
import app.android.adreal.vault.utils.Constants
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @POST("notifications")
    fun sendNotification(
        @Body notificationRequest : NotificationRequest,
        @Header("Authorization") token: String = "Basic ${Constants.ONE_SIGNAL_REST_API_KEY}",
        @Header("accept") accept: String = "application/json",
        @Header("Content-Type") contentType: String = "application/json"
    ) : Call<NotificationResponse>
}