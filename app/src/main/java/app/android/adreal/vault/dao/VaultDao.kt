package app.android.adreal.vault.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import app.android.adreal.vault.model.DeviceModel
import app.android.adreal.vault.model.Item
import app.android.adreal.vault.model.SaltModel

@Dao
interface VaultDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(data : Item)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWithReplace(data : Item)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDeviceWithReplace(data : DeviceModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSalt(data : SaltModel)

    @Update
    fun update(data : Item)

    @Delete
    fun delete(data: Item)

    @Query("SELECT * from Item where deviceId = :deviceId order by id asc")
    fun read(deviceId : String) : LiveData<List<Item>>

    @Query("SELECT * from Item where deviceId = :deviceId order by id asc")
    fun readWithoutLiveData(deviceId : String) : List<Item>

    @Query("SELECT * from device_table order by lastUpdated desc")
    fun readDevices() : List<DeviceModel>

    @Query("SELECT salt from salt_table where deviceId = :deviceId")
    fun readSalt(deviceId : String) : String
}