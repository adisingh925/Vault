package app.android.adreal.vault.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import app.android.adreal.vault.model.Item

@Dao
interface VaultDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(data : Item)

    @Update
    fun update(data : Item)

    @Delete
    fun delete(data: Item)

    @Query("SELECT * from Item order by id asc")
    fun read() : LiveData<List<Item>>
}