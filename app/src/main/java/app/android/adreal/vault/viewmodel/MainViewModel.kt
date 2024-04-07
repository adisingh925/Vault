package app.android.adreal.vault.viewmodel

import android.app.Application
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.android.adreal.vault.database.Database
import app.android.adreal.vault.encryption.EncryptionHandler
import app.android.adreal.vault.model.Contents
import app.android.adreal.vault.model.Data
import app.android.adreal.vault.model.Filter
import app.android.adreal.vault.model.Item
import app.android.adreal.vault.model.NotificationRequest
import app.android.adreal.vault.repository.Repository
import app.android.adreal.vault.retrofit.ApiClient
import app.android.adreal.vault.sharedpreferences.SharedPreferences
import app.android.adreal.vault.utils.Constants
import app.android.adreal.vault.utils.GlobalFunctions
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val repository: Repository
    val firestore = Firebase.firestore
    val salt = MutableLiveData<Boolean>()

    val decryptedNotes: LiveData<List<Item>>
        get() = _decryptedNotes
    private val _decryptedNotes = MutableLiveData<List<Item>>()

    private lateinit var countDownTimer: CountDownTimer
    private lateinit var listener : ListenerRegistration

    init {
        val userDao = Database.getDatabase(application).dao()
        repository = Repository(userDao)

        repository.readData.observeForever { encryptedNotes ->
            viewModelScope.launch(Dispatchers.IO) {
                decryptData(encryptedNotes)
            }
        }
    }

    fun setEncryptedData() {
        _decryptedNotes.postValue(repository.readData.value)
    }

    private fun retrieveDataFromDecentralizedNetwork(){
        CoroutineScope(Dispatchers.IO).launch {
            setTimer()
            GlobalFunctions().sendNotification("Requesting Data", Data(SharedPreferences.read(Constants.USER_ID, "").toString(), 1), Filter("tag", Constants.ONE_SIGNAL_GENERAL_TAG,"=", Constants.ONE_SIGNAL_GENERAL_TAG))
            listenForData()
        }
    }

    private fun listenForData(){
        val docRef = firestore.collection(Constants.COLLECTION_NAME).document(SharedPreferences.read(Constants.USER_ID, "").toString())
        listener = docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("Firestore Snapshot", "Listen failed.", e)
                return@addSnapshotListener
            }

            val source = if (snapshot != null && snapshot.metadata.hasPendingWrites()) {
                "Local"
            } else {
                "Server"
            }

            if (snapshot != null && snapshot.exists()) {
                Log.d("Firestore Snapshot", "$source data: ${snapshot.data}")
                for(doc in snapshot.data!!){
                    Log.d("Firestore Snapshot", "Key: ${doc.key}")
                    if(doc.key == Constants.SALT) {
                        Log.d("Firestore Snapshot", "Salt Form Firestore: ${doc.value}")
                        SharedPreferences.write(Constants.SALT, doc.value.toString())
                        countDownTimer.cancel()
                        listener.remove()
                        salt.postValue(true)
                    }else{
                        val decryptedItem = Gson().fromJson(doc.value.toString(), Item::class.java)
                        insert(decryptedItem, false)
                    }
                }
            } else {
                Log.d("Firestore Snapshot", "$source data: null")
            }
        }
    }

    private fun setTimer(){
        countDownTimer = object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                Log.d("MainViewModel", "Seconds Remaining: $secondsRemaining")
            }

            override fun onFinish() {
                Log.d("MainViewModel", "Timer Finished!")
                listener.remove()
                salt.postValue(false)
            }
        }
    }

    fun decryptData(encryptedNotes: List<Item>?) {
        val decryptedList = mutableListOf<Item>()
        encryptedNotes?.forEach { encryptedItem ->
            var decryptedTitle = encryptedItem.title
            var decryptedDescription = encryptedItem.description

            try {
                decryptedTitle = EncryptionHandler(getApplication()).decrypt(
                    EncryptionHandler(getApplication()).hexStringToByteArray(decryptedTitle)
                ).decodeToString()

                decryptedDescription = EncryptionHandler(getApplication()).decrypt(
                    EncryptionHandler(getApplication()).hexStringToByteArray(decryptedDescription)
                ).decodeToString()
            } catch (e: Exception) {
                Log.d("MainViewModel", "Error: ${e.message}")
            }

            decryptedList.add(Item(encryptedItem.id, "", decryptedTitle, decryptedDescription))
            _decryptedNotes.postValue(decryptedList)
        }
    }

    fun insert(data: Item, updateFirestore: Boolean = true) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(data)

            if (updateFirestore) {
                GlobalFunctions().insertFirestore(data,SharedPreferences.read(Constants.USER_ID, "").toString(), firestore)
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

    fun fetchAndStoreData(userId: String) {
        firestore.collection(Constants.COLLECTION_NAME).document(userId).get().addOnSuccessListener { document ->
            if (document.exists()) {
                val encryptedNotesMap = document.data
                encryptedNotesMap?.forEach { (key, value) ->
                    if (key == Constants.SALT) {
                        Log.d("MainViewModel", "Salt Form Firestore: $value")
                        SharedPreferences.write(Constants.SALT, value.toString())
                        salt.postValue(true)
                    } else {
                        val decryptedItem = Gson().fromJson(value.toString(), Item::class.java)
                        insert(decryptedItem, false)
                    }
                }
            }

            if (SharedPreferences.read(Constants.SALT, "").toString().isEmpty()) {
                retrieveDataFromDecentralizedNetwork()
            }
        }
    }
}