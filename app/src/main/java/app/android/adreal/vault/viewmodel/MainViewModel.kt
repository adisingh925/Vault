package app.android.adreal.vault.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.android.adreal.vault.database.Database
import app.android.adreal.vault.encryption.EncryptionHandler
import app.android.adreal.vault.model.Item
import app.android.adreal.vault.repository.Repository
import app.android.adreal.vault.sharedpreferences.SharedPreferences
import app.android.adreal.vault.utils.Constants
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: Repository
    private val firestore = Firebase.firestore

    val decryptedNotes: LiveData<List<Item>>
        get() = _decryptedNotes
    private val _decryptedNotes = MutableLiveData<List<Item>>()

    init {
        val userDao = Database.getDatabase(application).dao()
        repository = Repository(userDao)

        repository.readData.observeForever { encryptedNotes ->
            viewModelScope.launch(Dispatchers.IO) {
                val decryptedList = mutableListOf<Item>()
                encryptedNotes?.forEach { encryptedItem ->
                    val decryptedTitle = EncryptionHandler(application).decrypt(
                        EncryptionHandler(application).hexStringToByteArray(encryptedItem.title)
                    ).decodeToString()

                    val decryptedDescription = EncryptionHandler(application).decrypt(
                        EncryptionHandler(application).hexStringToByteArray(encryptedItem.description)
                    ).decodeToString()

                    decryptedList.add(Item(encryptedItem.id, decryptedTitle, decryptedDescription))
                    _decryptedNotes.postValue(decryptedList)
                }
            }
        }
    }

    fun insert(data: Item) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(data)
            insertFirestore(data)
        }
    }

    fun update(data: Item) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.update(data)
            updateFirestore(data)
        }
    }

    fun delete(data: Item) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(data)
            deleteFirestore(data)
        }
    }

    private fun insertFirestore(data: Item) {
        val userId = SharedPreferences.read(Constants.USER_ID, "").toString()
        val encryptedNotes = Gson().toJson(data)
        val encryptedNotesMap = mapOf(data.id to encryptedNotes)
        firestore.collection(Constants.COLLECTION_NAME).document(userId).set(encryptedNotesMap, SetOptions.merge())
    }

    private fun updateFirestore(data: Item) {
        val userId = SharedPreferences.read(Constants.USER_ID, "").toString()
        val encryptedNotes = Gson().toJson(data)
        val encryptedNotesMap = hashMapOf<String, Any>(data.id.toString() to encryptedNotes)
        firestore.collection(Constants.COLLECTION_NAME).document(userId).update(encryptedNotesMap)
    }

    private fun deleteFirestore(data: Item) {
        val userId = SharedPreferences.read(Constants.USER_ID, "").toString()
        val updates = hashMapOf<String, Any>(data.id.toString() to FieldValue.delete())
        firestore.collection(Constants.COLLECTION_NAME).document(userId).update(updates)
    }
}