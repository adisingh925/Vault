package app.android.adreal.vault

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import app.android.adreal.vault.databinding.ActivityMainBinding
import app.android.adreal.vault.encryption.EncryptionHandler
import app.android.adreal.vault.sharedpreferences.SharedPreferences
import app.android.adreal.vault.utils.Constants
import com.google.android.material.snackbar.Snackbar
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        SharedPreferences.init(this)

        val encryptedData = EncryptionHandler(this).encrypt("hello there".encodeToByteArray()).joinToString("") { "%02x".format(it) }
        val encr = EncryptionHandler(this).hexStringToByteArray(encryptedData)

        val decryptedData =
            EncryptionHandler(this).decrypt(encr)

        Log.d("Decrypted Data", decryptedData.decodeToString())
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