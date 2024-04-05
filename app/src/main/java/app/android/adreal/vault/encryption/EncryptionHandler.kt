package app.android.adreal.vault.encryption

import android.content.Context
import android.security.keystore.KeyProperties
import app.android.adreal.vault.sharedpreferences.SharedPreferences
import app.android.adreal.vault.utils.Constants
import com.lambdapioneer.argon2kt.Argon2Kt
import com.lambdapioneer.argon2kt.Argon2KtResult
import com.lambdapioneer.argon2kt.Argon2Mode
import com.lambdapioneer.argon2kt.Argon2Version
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class EncryptionHandler(private val context: Context) {

    private val argon2 by lazy {
        Argon2Kt()
    }

    private fun generateAESKeyFromHash(hash: String): SecretKey {
        val keyBytes: ByteArray = hash.substring(0, AES_KEY_LENGTH / 8).toByteArray()
        return SecretKeySpec(keyBytes, ALGORITHM)
    }

    fun generateAESKeyFromPassword(password: String) {
        val random = SecureRandom()
        val salt = ByteArray(16) // 16 bytes salt
        random.nextBytes(salt)

        SharedPreferences.write(Constants.SALT, byteArrayToHexString(salt))

        val hash = generateArgon2Hash(
            Argon2Mode.ARGON2_ID,
            password.toByteArray(),
            salt,
            65536,
            10,
            Argon2Version.V13,
            32,
            8
        ).rawHashAsHexadecimal(true)

        SharedPreferences.write(Constants.HASH, hash)
    }

    private fun getKey(): SecretKey {
        val hash = SharedPreferences.read(Constants.HASH, "")
        return generateAESKeyFromHash(hash.toString())
    }

    fun encrypt(bytes: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, getKey(), SecureRandom())
        }

        val iv = cipher.iv
        val encryptedData = cipher.doFinal(bytes)

        val combined = ByteArray(iv.size + encryptedData.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(encryptedData, 0, combined, iv.size, encryptedData.size)

        return combined
    }

    fun decrypt(data: ByteArray): ByteArray {
        val ivSize = 16 // Assuming IV size is 16 bytes (AES block size)
        val iv = ByteArray(ivSize)
        val encryptedData = ByteArray(data.size - ivSize)

        System.arraycopy(data, 0, iv, 0, ivSize)
        System.arraycopy(data, ivSize, encryptedData, 0, encryptedData.size)

        // Initialize decryption cipher with IV
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, getKey(), IvParameterSpec(iv))
        }

        // Decrypt the encrypted data (excluding IV)
        return cipher.doFinal(encryptedData)
    }

    fun hexStringToByteArray(hexString: String): ByteArray {
        val result = ByteArray(hexString.length / 2)
        for (i in hexString.indices step 2) {
            val byteValue = hexString.substring(i, i + 2).toInt(16)
            result[i / 2] = byteValue.toByte()
        }
        return result
    }

    fun byteArrayToHexString(byteArray: ByteArray): String {
        return byteArray.joinToString("") { "%02x".format(it) }
    }

    private fun generateArgon2Hash(
        argonMode: Argon2Mode,
        passwordByteArray: ByteArray,
        saltByteArray: ByteArray,
        memoryCost: Int,
        iteration: Int,
        version: Argon2Version,
        hashLength: Int,
        parallelism: Int
    ): Argon2KtResult {
        return argon2.hash(
            mode = argonMode,
            password = passwordByteArray,
            salt = saltByteArray,
            tCostInIterations = iteration,
            mCostInKibibyte = memoryCost,
            version = version,
            hashLengthInBytes = hashLength,
            parallelism = parallelism
        )
    }

    companion object {
        private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
        private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"
        private const val AES_KEY_LENGTH = 256
    }
}