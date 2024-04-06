package app.android.adreal.vault.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.android.adreal.vault.utils.Constants

@Entity(tableName = Constants.DEVICE_TABLE)
data class DeviceModel(
    @PrimaryKey val deviceId : String,
    val lastUpdated : Long,
)
