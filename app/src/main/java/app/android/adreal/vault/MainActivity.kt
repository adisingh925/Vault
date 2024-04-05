package app.android.adreal.vault

import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import app.android.adreal.vault.databinding.ActivityMainBinding
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        SharedPreferences.init(this)

        if (SharedPreferences.read(Constants.USER_ID, "").toString().isEmpty()) {
            val androidId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID)
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