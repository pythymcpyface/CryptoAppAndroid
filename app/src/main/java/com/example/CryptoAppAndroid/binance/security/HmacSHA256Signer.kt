package com.example.CryptoAppAndroid.binance.security

import org.apache.commons.codec.binary.Hex
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Utility class to sign messages using HMAC-SHA256.
 * @author https://github.com/VaultDeveloper
 * @see https://github.com/OpenHFT/Binance-Api-Client/blob/master/src/main/java/com/binance/api/client/security/HmacSHA256Signer.java
 */
object HmacSHA256Signer {

	/**
	 * Sign the given message using the given secret.
	 * @param message message to sign
	 * @param secret secret key
	 * @return a signed message
	 */
	fun sign(message: String?, secret: String): String {
		return try {
			val sha256HMAC = Mac.getInstance("HmacSHA256")
			val secretKeySpec =
				SecretKeySpec(secret.toByteArray(), "HmacSHA256")
			sha256HMAC.init(secretKeySpec)
			String(Hex.encodeHex(sha256HMAC.doFinal(message!!.toByteArray())))
		} catch (e: Exception) {
			throw RuntimeException("Unable to sign message.", e)
		}
	}
}