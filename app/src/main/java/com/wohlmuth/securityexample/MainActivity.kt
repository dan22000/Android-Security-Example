package com.wohlmuth.securityexample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import kotlinx.android.synthetic.main.activity_main.*
import java.security.MessageDigest

class MainActivity : AppCompatActivity() {

    companion object {
        const val PREFERENCES_FILE_NAME = "user"
        const val PREFERENCE_PASSWORD_KEY = "password"
    }

    private lateinit var sharedPreferences: EncryptedSharedPreferences
    private val digest = MessageDigest.getInstance("MD5")

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
        // Generate MD5 Hash
        val md5TypedArray = digest.digest(etPassword.text.toString().toByteArray()).toTypedArray()
        val hashString = byteArrayToHexString(md5TypedArray)

        // Save data to the EncryptedSharedPreferences
        sharedPreferences.edit()
                .putString(PREFERENCE_PASSWORD_KEY, hashString)
                .apply()
    }

    private fun loadHash() {
        // Read data from EncryptedSharedPreferences
        val value = sharedPreferences.getString(PREFERENCE_PASSWORD_KEY, "")
        tvHash.text = value
    }

    private fun byteArrayToHexString( array: Array<Byte> ): String {
        val result = StringBuilder(array.size * 2)
        for ( byte in array ) {
            val toAppend = String.format("%2X", byte).replace(" ", "0") // hexadecimal
            result.append(toAppend).append("-")
        }
        result.setLength(result.length - 1) // remove last '-'

        return result.toString()
    }
}
