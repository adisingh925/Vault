package app.android.adreal.vault

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import app.android.adreal.vault.databinding.ActivityMainBinding
import app.android.adreal.vault.databinding.CreatePasswordDialogBinding
import app.android.adreal.vault.encryption.EncryptionHandler
import app.android.adreal.vault.sharedpreferences.SharedPreferences
import app.android.adreal.vault.utils.Constants
import app.android.adreal.vault.viewmodel.MainViewModel
import com.google.android.material.snackbar.Snackbar
import com.onesignal.OneSignal
import java.util.UUID

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
        SharedPreferences.init(this)

        if (SharedPreferences.read(Constants.HASH, "").toString().isEmpty()) {
            initDialog()
            showSetPasswordDialog()
        }

        if (SharedPreferences.read(Constants.USER_ID, "").toString().isEmpty()) {
            val androidId =
                Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID)
            Log.d("MainActivity", "UUID: $androidId")
            SharedPreferences.write(Constants.USER_ID, androidId)

            OneSignal.User.addTag(
                Constants.USER_ID,
                androidId
            )

            viewModel.fetchAndStoreData()
        } else {
            Log.d(
                "MainActivity",
                "UUID: Already Exists! -> ${SharedPreferences.read(Constants.USER_ID, "")}"
            )
        }
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

            bind.save.setOnClickListener {
                bind.password.error = null
                bind.retypePassword.error = null

                if (bind.passwordInputField.text.toString().isEmpty()
                ) {
                    bind.password.error = "Please fill all the fields!"
                    return@setOnClickListener
                }

                if(bind.retypePasswordInputField.text.toString().isEmpty()){
                    bind.retypePassword.error = "Please fill all the fields!"
                    return@setOnClickListener
                }

                if(bind.passwordInputField.text.toString() != bind.retypePasswordInputField.text.toString()){
                    bind.retypePassword.error = "Passwords do not match!"
                    return@setOnClickListener
                }

                if(bind.passwordInputField.text.toString() == bind.retypePasswordInputField.text.toString()){
                    EncryptionHandler(this).generateAESKeyFromPassword(bind.passwordInputField.text.toString())
                    dialog.dismiss()
                }
            }

            dialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}