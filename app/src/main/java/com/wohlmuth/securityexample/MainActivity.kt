package com.wohlmuth.securityexample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.crypto.tink.subtle.Hex
import kotlinx.android.synthetic.main.activity_main.*
import java.security.MessageDigest


class MainActivity : AppCompatActivity() {

    companion object {
        const val PREFERENCES_FILE_NAME = "user"
        const val PREFERENCE_PASSWORD_KEY = "password"
    }

    private lateinit var sharedPreferences: EncryptedSharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnSave.setOnClickListener { savePassword() }
        btnLoad.setOnClickListener { loadHash() }

        // Create or retrieve the Master Key for encryption/decryption
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        // Initialize an instance of EncryptedSharedPreferences
        sharedPreferences = EncryptedSharedPreferences.create(
                PREFERENCES_FILE_NAME,
                masterKeyAlias,
                applicationContext,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM) as EncryptedSharedPreferences
    }

    private fun savePassword() {
        // Save data to the EncryptedSharedPreferences
        sharedPreferences.edit()
                .putString(PREFERENCE_PASSWORD_KEY, sha256AsHexString(etPassword.text.toString()))
                .apply()
    }

    private fun loadHash() {
        // Read data from EncryptedSharedPreferences
        val value = sharedPreferences.getString(PREFERENCE_PASSWORD_KEY, "")
        tvHash.text = value
    }

    private fun sha256AsHexString(message: String?): String? {
        require(!(message == null || message.isEmpty())) { "Invalid message string!" }
        val bytes = sha256(message)
        return Hex.encode(bytes)
    }


    private fun sha256(message: String): ByteArray {
        return try {
            val algorithm = MessageDigest.getInstance("SHA-256")
            algorithm.update(message.toByteArray(charset("UTF-8")))
            algorithm.digest()
        } catch (e: Exception) {
            throw IllegalStateException("Can't calculate SHA-256 for the given message!", e)
        }
    }
}
