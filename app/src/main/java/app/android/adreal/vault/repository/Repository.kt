package app.android.adreal.vault.repository

import app.android.adreal.vault.dao.VaultDao
import app.android.adreal.vault.model.Item
import app.android.adreal.vault.sharedpreferences.SharedPreferences
import app.android.adreal.vault.utils.Constants

class Repository(private val dao: VaultDao) {

    val readData = dao.read(SharedPreferences.read(Constants.USER_ID, "").toString())

    fun insert(data: Item) {
        dao.insert(data)
    }

    suspend fun update(data: Item) {
        dao.update(data)
    }

    suspend fun delete(data: Item) {
        dao.delete(data)
    }
}