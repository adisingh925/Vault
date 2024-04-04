package app.android.adreal.vault.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.android.adreal.vault.database.Database
import app.android.adreal.vault.encryption.EncryptionHandler
import app.android.adreal.vault.model.Item
import app.android.adreal.vault.repository.Repository
import app.android.adreal.vault.sharedpreferences.SharedPreferences
import app.android.adreal.vault.utils.Constants
import com.google.firebase.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.google.gson.Gson
import com.lambdapioneer.argon2kt.Argon2Mode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: Repository

    val decryptedNotes: LiveData<List<Item>>
        get() = _decryptedNotes
    private val _decryptedNotes = MutableLiveData<List<Item>>()

    init {
        val userDao = Database.getDatabase(application).dao()
        repository = Repository(userDao)

        repository.readData.observeForever { encryptedNotes ->
            val decryptedList = mutableListOf<Item>()
            encryptedNotes?.forEach { encryptedItem ->
                val decryptedTitle = EncryptionHandler(application).decrypt(
                    EncryptionHandler(application).hexStringToByteArray(encryptedItem.title)
                ).decodeToString()

                val decryptedDescription = EncryptionHandler(application).decrypt(
                    EncryptionHandler(application).hexStringToByteArray(encryptedItem.description)
                ).decodeToString()

                decryptedList.add(Item(encryptedItem.id, decryptedTitle, decryptedDescription))
            }
            _decryptedNotes.postValue(decryptedList)
        }
    }

    fun insert(data: Item) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(data)
        }
    }

    fun update(data: Item) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.update(data)
        }
    }

    fun delete(data: Item) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(data)
        }
    }
}