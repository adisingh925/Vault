package app.android.adreal.vault.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.android.adreal.vault.utils.Constants

@Entity(tableName = Constants.TABLE_NAME)
data class Item(
    @PrimaryKey val id: Int,
    val deviceId : String,
    var title: String,
    var description: String
)