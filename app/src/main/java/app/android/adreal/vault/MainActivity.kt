package app.android.adreal.vault

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import app.android.adreal.vault.databinding.ActivityMainBinding
import app.android.adreal.vault.databinding.CreatePasswordDialogBinding
import app.android.adreal.vault.encryption.EncryptionHandler
import app.android.adreal.vault.model.Contents
import app.android.adreal.vault.model.Data
import app.android.adreal.vault.model.Filter
import app.android.adreal.vault.model.NotificationRequest
import app.android.adreal.vault.model.NotificationResponse
import app.android.adreal.vault.retrofit.ApiClient
import app.android.adreal.vault.sharedpreferences.SharedPreferences
import app.android.adreal.vault.utils.Constants
import app.android.adreal.vault.utils.GlobalFunctions
import app.android.adreal.vault.viewmodel.MainViewModel
import com.google.android.material.snackbar.Snackbar
import com.onesignal.OneSignal
import retrofit2.Call
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val viewModel by lazy {
        ViewModelProvider(this)[MainViewModel::class.java]
    }

    private val dialog by lazy {
        Dialog(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE
        )
        SharedPreferences.init(this)
        initDialog()

        if (SharedPreferences.read(Constants.USER_ID, "").toString().isEmpty()) {
            val androidId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID)
            Log.d("MainActivity", "UUID: $androidId")
            SharedPreferences.write(Constants.USER_ID, androidId)

            OneSignal.User.addTag(
                Constants.USER_ID,
                androidId
            )

            OneSignal.User.addTag(
                Constants.ONE_SIGNAL_GENERAL_TAG,
                Constants.ONE_SIGNAL_GENERAL_TAG
            )

            viewModel.fetchAndStoreData()
        } else {
            Log.d(
                "MainActivity",
                "UUID: Already Exists! -> ${SharedPreferences.read(Constants.USER_ID, "")}"
            )
            SharedPreferences.write(Constants.HASH, "")
        }

        GlobalFunctions().deviceBroadcast()
    }

    private fun initDialog() {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    }

    fun showSnackbar(message: String) {
        val snackbar = Snackbar.make(
            binding.root,
            message,
            Snackbar.LENGTH_LONG
        )

        snackbar.setAction("Dismiss") {
            snackbar.dismiss()
        }

        snackbar.show()
    }

    private fun showSetPasswordDialog() {
        try {
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val bind = CreatePasswordDialogBinding.inflate(layoutInflater)
            dialog.setContentView(bind.root)
            dialog.setCancelable(false)
            bind.save.isEnabled = false

            if (SharedPreferences.read(Constants.SALT, "").toString().isNotEmpty()) {
                Log.d("MainActivity", "Salt Found In Local!")
                bind.save.isEnabled = true
            } else {
                viewModel.salt.observe(this) {
                    if (!it) {
                        Log.d("MainActivity", "Salt Not Found In Firestore!")
                        val salt = EncryptionHandler(this).generateSalt()
                        viewModel.saveSaltInFirestore(salt)
                    } else {
                        Log.d("MainActivity", "Salt Found In Firestore!")
                    }

                    bind.save.isEnabled = true
                    viewModel.salt.removeObservers(this)
                }
            }

            bind.save.setOnClickListener {
                bind.password.error = null

                if (bind.passwordInputField.text.toString().isEmpty()
                ) {
                    bind.password.error = "Please fill all the fields!"
                    return@setOnClickListener
                }

                EncryptionHandler(this).generateAESKeyFromPassword(bind.passwordInputField.text.toString())
                val intent = Intent(Constants.DECRYPT)
                this.sendBroadcast(intent)
                dialog.dismiss()
            }

            dialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPause() {
        super.onPause()
        val intent = Intent(Constants.ENCRYPT)
        this.sendBroadcast(intent)
        SharedPreferences.write(Constants.HASH, "")
    }

    override fun onStart() {
        super.onStart()
        if (SharedPreferences.read(Constants.HASH, "").toString().isEmpty()) {
            showSetPasswordDialog()
        }
    }
}