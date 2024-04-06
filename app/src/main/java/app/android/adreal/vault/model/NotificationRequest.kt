package app.android.adreal.vault.model

data class NotificationRequest(
    val app_id: String,
    val contents: Contents,
    val `data`: Data,
    val filters: List<Filter>
)