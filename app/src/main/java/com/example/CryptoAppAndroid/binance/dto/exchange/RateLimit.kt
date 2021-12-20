package com.example.CryptoAppAndroid.binance.dto.exchange

data class RateLimit(
    val interval: String,
    val intervalNum: Int,
    val limit: Int,
    val rateLimitType: String
)