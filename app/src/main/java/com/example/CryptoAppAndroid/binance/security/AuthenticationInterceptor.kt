package com.example.CryptoAppAndroid.binance.security

import com.example.CryptoAppAndroid.binance.BinanceApiConstants
import com.example.CryptoAppAndroid.binance.security.HmacSHA256Signer
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.*

/**
 * A request interceptor that injects the API Key Header into requests, and signs messages, whenever required.
 * @author https://github.com/joaopsilva
 * @see https://github.com/OpenHFT/Binance-Api-Client/blob/master/src/main/java/com/binance/api/client/security/AuthenticationInterceptor.java
 */
class AuthenticationInterceptor(private val apiKey: String, private val secret: String) :
	Interceptor {
	@Throws(IOException::class)
	override fun intercept(chain: Interceptor.Chain): Response {
		val original = chain.request()
		val newRequestBuilder = original.newBuilder()
		val isApiKeyRequired =
			original.header(BinanceApiConstants.ENDPOINT_SECURITY_TYPE_APIKEY) != null
		val isSignatureRequired =
			original.header(BinanceApiConstants.ENDPOINT_SECURITY_TYPE_SIGNED) != null
		newRequestBuilder.removeHeader(BinanceApiConstants.ENDPOINT_SECURITY_TYPE_APIKEY)
			.removeHeader(BinanceApiConstants.ENDPOINT_SECURITY_TYPE_SIGNED)

		// Endpoint requires sending a valid API-KEY
		if (isApiKeyRequired || isSignatureRequired) {
			newRequestBuilder.addHeader(BinanceApiConstants.API_KEY_HEADER, apiKey)
		}

		// Endpoint requires signing the payload
		if (isSignatureRequired) {
			val payload = original.url.query
			payload.isNullOrEmpty()
			if (!payload.isNullOrEmpty()) {
				val signature: String = HmacSHA256Signer.sign(payload, secret)
				val signedUrl =
					original.url.newBuilder().addQueryParameter("signature", signature).build()
				newRequestBuilder.url(signedUrl)
			}
		}

		// Build new request after adding the necessary authentication information
		val newRequest = newRequestBuilder.build()
		return chain.proceed(newRequest)
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other == null || javaClass != other.javaClass) return false
		val that = other as AuthenticationInterceptor
		return apiKey == that.apiKey &&
				secret == that.secret
	}

	override fun hashCode(): Int {
		return Objects.hash(apiKey, secret)
	}

}