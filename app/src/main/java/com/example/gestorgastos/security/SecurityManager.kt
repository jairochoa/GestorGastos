package com.example.gestorgastos.security

import android.content.Context
import android.util.Base64
import java.nio.charset.StandardCharsets
import javax.crypto.KeyGenerator
import javax.crypto.Mac
import javax.crypto.SecretKey
import java.security.KeyStore

class SecurityManager(context: Context) {

    private val prefs = context.getSharedPreferences("security_prefs", Context.MODE_PRIVATE)

    fun isPinSet(): Boolean = prefs.contains(KEY_PIN_HMAC)

    fun setPin(pin: String) {
        val h = hmac(pin)
        prefs.edit().putString(KEY_PIN_HMAC, h).apply()
    }

    fun verifyPin(pin: String): Boolean {
        val saved = prefs.getString(KEY_PIN_HMAC, null) ?: return false
        return saved == hmac(pin)
    }

    fun clearPin() {
        prefs.edit().remove(KEY_PIN_HMAC).apply()
    }

    private fun hmac(pin: String): String {
        val mac = Mac.getInstance(HMAC_ALG)
        mac.init(getOrCreateKey())
        val bytes = mac.doFinal(pin.toByteArray(StandardCharsets.UTF_8))
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private fun getOrCreateKey(): SecretKey {
        val ks = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val existing = ks.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        if (existing != null) return existing.secretKey

        val kg = KeyGenerator.getInstance("HmacSHA256", ANDROID_KEYSTORE)
        kg.init(android.security.keystore.KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            android.security.keystore.KeyProperties.PURPOSE_SIGN or android.security.keystore.KeyProperties.PURPOSE_VERIFY
        ).setDigests(android.security.keystore.KeyProperties.DIGEST_SHA256).build())
        return kg.generateKey()
    }

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "gastos_pin_hmac_key"
        private const val HMAC_ALG = "HmacSHA256"
        private const val KEY_PIN_HMAC = "pin_hmac"
    }
}