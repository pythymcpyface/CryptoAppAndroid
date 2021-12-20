package com.example.CryptoAppAndroid.binance.dto

/**
 * An asset Binance supports.
 */
data class ChartData (
	val marketCapData: Map<Long, Double>,
	val orderMap: Map<Long, Double>
)