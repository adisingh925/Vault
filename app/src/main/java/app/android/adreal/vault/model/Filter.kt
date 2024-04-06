package app.android.adreal.vault.model

data class Filter(
    val field: String,
    val key: String,
    val relation: String,
    val value: String
)