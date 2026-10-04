package com.example.galaxyguardian.data.repository

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class EncryptedCredentialStore(context: Context) {

    private val sharedPrefs = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply {
        load(null)
    }

    init {
        ensureMasterKey()
    }

    private fun ensureMasterKey() {
        if (!keyStore.containsAlias(MASTER_KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEY_STORE
            )
            val keySpec = KeyGenParameterSpec.Builder(
                MASTER_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()

            keyGenerator.init(keySpec)
            keyGenerator.generateKey()
        }
    }

    private fun getSecretKey(): SecretKey {
        return keyStore.getKey(MASTER_KEY_ALIAS, null) as SecretKey
    }

    fun saveCredential(keyAlias: String, secretValue: String) {
        if (secretValue.isBlank()) {
            clearCredential(keyAlias)
            return
        }

        try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(secretValue.toByteArray(Charsets.UTF_8))

            val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)
            val cipherBase64 = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)

            sharedPrefs.edit()
                .putString("${keyAlias}_iv", ivBase64)
                .putString("${keyAlias}_data", cipherBase64)
                .apply()
        } catch (e: Exception) {
            android.util.Log.e("EncryptedStore", "Failed to encrypt credential for $keyAlias", e)
        }
    }

    fun getCredential(keyAlias: String): String? {
        val ivBase64 = sharedPrefs.getString("${keyAlias}_iv", null) ?: return null
        val cipherBase64 = sharedPrefs.getString("${keyAlias}_data", null) ?: return null

        return try {
            val iv = Base64.decode(ivBase64, Base64.NO_WRAP)
            val cipherText = Base64.decode(cipherBase64, Base64.NO_WRAP)

            val spec = GCMParameterSpec(128, iv)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)

            val decryptedBytes = cipher.doFinal(cipherText)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            android.util.Log.e("EncryptedStore", "Failed to decrypt credential for $keyAlias", e)
            null
        }
    }

    fun clearCredential(keyAlias: String) {
        sharedPrefs.edit()
            .remove("${keyAlias}_iv")
            .remove("${keyAlias}_data")
            .apply()
    }

    // Convenience accessors
    fun getCustomApiKey(): String = getCredential(KEY_CUSTOM_API_KEY) ?: ""
    fun saveCustomApiKey(key: String) = saveCredential(KEY_CUSTOM_API_KEY, key)

    fun getOllamaBaseUrl(): String = sharedPrefs.getString(KEY_OLLAMA_BASE_URL, "http://10.0.2.2:11434") ?: "http://10.0.2.2:11434"
    fun saveOllamaBaseUrl(url: String) = sharedPrefs.edit().putString(KEY_OLLAMA_BASE_URL, url.trim()).apply()

    fun getOllamaModelName(): String = sharedPrefs.getString(KEY_OLLAMA_MODEL_NAME, "qwen2.5-coder:7b") ?: "qwen2.5-coder:7b"
    fun saveOllamaModelName(name: String) = sharedPrefs.edit().putString(KEY_OLLAMA_MODEL_NAME, name.trim()).apply()

    companion object {
        private const val PREFS_NAME = "galaxy_secure_vault"
        private const val ANDROID_KEY_STORE = "AndroidKeyStore"
        private const val MASTER_KEY_ALIAS = "galaxy_guardian_master_key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"

        private const val KEY_CUSTOM_API_KEY = "custom_gemini_api_key"
        private const val KEY_OLLAMA_BASE_URL = "ollama_base_url"
        private const val KEY_OLLAMA_MODEL_NAME = "ollama_model_name"
    }
}
