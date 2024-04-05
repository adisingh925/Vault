package app.android.adreal.vault.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
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
import app.android.adreal.vault.utils.GlobalFunctions
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val repository: Repository
    private val firestore = Firebase.firestore
    val salt = MutableLiveData<Boolean>()

    val decryptedNotes: LiveData<List<Item>>
        get() = _decryptedNotes
    private val _decryptedNotes = MutableLiveData<List<Item>>()

    init {
        val userDao = Database.getDatabase(application).dao()
        repository = Repository(userDao)

        repository.readData.observeForever { encryptedNotes ->
            viewModelScope.launch(Dispatchers.IO) {
                decryptData(encryptedNotes)
            }
        }
    }

    fun decryptData(encryptedNotes: List<Item>?) {
        val decryptedList = mutableListOf<Item>()
        encryptedNotes?.forEach { encryptedItem ->
            var decryptedTitle = ""
            var decryptedDescription = ""

            if (SharedPreferences.read(Constants.HASH, "").toString().isEmpty()) {
                decryptedTitle = encryptedItem.title
                decryptedDescription = encryptedItem.description
            } else {
                try {
                    decryptedTitle = EncryptionHandler(getApplication()).decrypt(
                        EncryptionHandler(getApplication()).hexStringToByteArray(encryptedItem.title)
                    ).decodeToString()

                    decryptedDescription = EncryptionHandler(getApplication()).decrypt(
                        EncryptionHandler(getApplication()).hexStringToByteArray(encryptedItem.description)
                    ).decodeToString()
                } catch (e: Exception) {
                    Toast.makeText(getApplication(), "Error decrypting data", Toast.LENGTH_SHORT)
                        .show()
                    return
                }
            }

            decryptedList.add(Item(encryptedItem.id, decryptedTitle, decryptedDescription))
            _decryptedNotes.postValue(decryptedList)
        }
    }

    fun insert(data: Item, updateFirestore: Boolean = true) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(data)

            if (updateFirestore) {
                insertFirestore(data)
            }
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
        val encryptedNotesMap = mapOf(data.id.toString() to encryptedNotes)
        firestore.collection(Constants.COLLECTION_NAME).document(userId)
            .set(encryptedNotesMap, SetOptions.merge())
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

    fun saveSaltInFirestore(salt: String) {
        val userId = SharedPreferences.read(Constants.USER_ID, "").toString()
        val saltMap = mapOf(Constants.SALT to salt)
        firestore.collection(Constants.COLLECTION_NAME).document(userId)
            .set(saltMap, SetOptions.merge())
    }

    fun fetchAndStoreData() {
        val userId = SharedPreferences.read(Constants.USER_ID, "").toString()
        firestore.collection(Constants.COLLECTION_NAME).document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val encryptedNotesMap = document.data
                    var currentPrimaryKey = -1
                    encryptedNotesMap?.forEach { (key, value) ->
                        if (key == Constants.SALT) {
                            Log.d("MainViewModel", "Salt: $value")
                            SharedPreferences.write(Constants.SALT, value.toString())
                            salt.postValue(true)
                        } else {
                            val decryptedItem = Gson().fromJson(value.toString(), Item::class.java)
                            if (decryptedItem.id > currentPrimaryKey) {
                                currentPrimaryKey = decryptedItem.id
                            }
                            Log.d("MainViewModel", "Decrypted Item: $decryptedItem")
                            insert(decryptedItem, false)
                        }
                    }

                    if (currentPrimaryKey != -1) {
                        GlobalFunctions().setCurrentPrimaryKey(currentPrimaryKey)
                    }
                }

                if (SharedPreferences.read(Constants.SALT, "").toString().isEmpty()) {
                    salt.postValue(false)
                }
            }
    }
}