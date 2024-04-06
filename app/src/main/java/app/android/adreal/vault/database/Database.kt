package app.android.adreal.vault.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import app.android.adreal.vault.dao.VaultDao
import app.android.adreal.vault.model.DeviceModel
import app.android.adreal.vault.model.Item
import app.android.adreal.vault.model.SaltModel

@Database(entities = [Item::class, SaltModel::class, DeviceModel::class], version = 1, exportSchema = false)
abstract class Database : RoomDatabase() {

    abstract fun dao(): VaultDao

    companion object {
        @Volatile
        private var INSTANCE: app.android.adreal.vault.database.Database? = null

        fun getDatabase(context: Context): app.android.adreal.vault.database.Database {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this)
            {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    app.android.adreal.vault.database.Database::class.java,
                    "EncryptedDatabase"
                ).build()

                INSTANCE = instance
                return instance
            }
        }
    }
}