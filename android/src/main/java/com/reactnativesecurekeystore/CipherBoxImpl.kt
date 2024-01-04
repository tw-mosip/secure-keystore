package com.reactnativesecurekeystore

import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import com.reactnativesecurekeystore.dto.EncryptedOutput
import java.security.Key
import java.security.PrivateKey
import java.security.Signature
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec


const val CIPHER_ALGORITHM =
  "${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_GCM}/${KeyProperties.ENCRYPTION_PADDING_NONE}"
const val GCM_TAG_LEN = 128
const val SIGN_ALGORITHM = "SHA256with${KeyProperties.KEY_ALGORITHM_RSA}"
const val HMAC_ALGORITHM = "HmacSHA256"

class CipherBoxImpl : CipherBox {

  override fun initEncryptCipher(key: Key): Cipher {
    val cipher = Cipher.getInstance(CIPHER_ALGORITHM)
    cipher.init(Cipher.ENCRYPT_MODE, key)
    // iv/nounce: being added by cipher.init library
    return cipher
  }

  override fun encryptData(cipher: Cipher, data: String): EncryptedOutput {
    val encryptedData = cipher.doFinal(data.toByteArray(), 0, data.toByteArray().size)
    Log.i("CipherBoxImpl", "encryptedData len ${data.length}")
    Log.i("CipherBoxImpl", "Crypto provider during encrypt=${cipher.provider}")
    Log.i("CipherBoxImpl", "IV during encrypt=${cipher.iv}")
    return EncryptedOutput(encryptedData, cipher.iv)
  }

  override fun initDecryptCipher(key: Key, encryptedOutput: EncryptedOutput): Cipher {
    val cipher = Cipher.getInstance(CIPHER_ALGORITHM)

    val spec = GCMParameterSpec(GCM_TAG_LEN, encryptedOutput.iv)
    cipher.init(Cipher.DECRYPT_MODE, key, spec)

    return cipher
  }

  override fun decryptData(cipher: Cipher, encryptedOutput: EncryptedOutput): ByteArray {
    try {
      Log.i("cipherBoxImpl", "encryptedData len ${encryptedOutput.encryptedData.size}")
      Log.i("CipherBoxImpl", "Crypto provider during decrypt=${cipher.provider}")
      Log.i("CipherBoxImpl", "IV during encrypt=${cipher.iv}")
      return cipher.doFinal(encryptedOutput.encryptedData, 0, encryptedOutput.encryptedData.size)
    } catch (e: Exception) {
      Log.e("Secure","Exception in Decryption",e)
      throw e
    }
  }

  override fun createSignature(key: PrivateKey, data: String): Signature {
    val bytes = data.toByteArray(charset("UTF8"))
    val signature = Signature.getInstance(SIGN_ALGORITHM).run {
      initSign(key)
      update(bytes)
      this
    }

    return signature
  }

  override fun sign(signature: Signature): String {
    val sign = signature.sign()
    return Base64.encodeToString(sign, Base64.DEFAULT)
  }

  override fun generateHmacSha(key: SecretKey, data: String): ByteArray {
    val mac = Mac.getInstance(HMAC_ALGORITHM);

    try {
      mac.init(key)
    } catch (e: Exception) {
      Log.e("debugging", "Exception in generatehmac", e)
      throw e
    }

    return mac.doFinal(data.toByteArray())
  }
}
