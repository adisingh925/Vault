package app.android.adreal.vault.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.android.adreal.vault.utils.Constants

@Entity(tableName = Constants.TABLE_NAME)
data class Item(
    @PrimaryKey(autoGenerate = true) val id: Int,
    var title: String,
    var description: String
)