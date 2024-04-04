package app.android.adreal.vault

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import app.android.adreal.vault.databinding.ActivityMainBinding
import app.android.adreal.vault.encryption.EncryptionHandler
import app.android.adreal.vault.sharedpreferences.SharedPreferences
import app.android.adreal.vault.utils.Constants
import com.google.android.material.snackbar.Snackbar
import com.onesignal.OneSignal
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        SharedPreferences.init(this)

        if (SharedPreferences.read(Constants.USER_ID, "").toString().isEmpty()) {
            val uuid = UUID.randomUUID().toString()
            Log.d("MainActivity", "UUID: $uuid")
            SharedPreferences.write(Constants.USER_ID, uuid)

            OneSignal.User.addTag(
                Constants.USER_ID,
                uuid
            )
        } else {
            Log.d(
                "MainActivity",
                "UUID: Already Exists! -> ${SharedPreferences.read(Constants.USER_ID, "")}"
            )
        }
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
}