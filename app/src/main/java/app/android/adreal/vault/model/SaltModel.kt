package app.android.adreal.vault.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.android.adreal.vault.utils.Constants

@Entity(tableName = Constants.SALT_TABLE)
data class SaltModel(
    @PrimaryKey val deviceId: String,
    val salt: String
)
