package com.example.CryptoAppAndroid.binance

/**
 * Constants used throughout Binance's API.
 */
object BinanceApiConstants {

    /**
     * HTTP Header to be used for API-KEY authentication.
     */
    const val API_KEY_HEADER = "X-MBX-APIKEY"

    /**
     * Decorator to indicate that an endpoint requires an API key.
     */
    const val ENDPOINT_SECURITY_TYPE_APIKEY = "APIKEY"
    const val ENDPOINT_SECURITY_TYPE_APIKEY_HEADER = "$ENDPOINT_SECURITY_TYPE_APIKEY: #"

    /**
     * Decorator to indicate that an endpoint requires a signature.
     */
    const val ENDPOINT_SECURITY_TYPE_SIGNED = "SIGNED"
    const val ENDPOINT_SECURITY_TYPE_SIGNED_HEADER = "$ENDPOINT_SECURITY_TYPE_SIGNED: #"

    /**
     * Default receiving window.
     */
    const val DEFAULT_RECEIVING_WINDOW = 60000L
}