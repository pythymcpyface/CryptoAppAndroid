package com.example.CryptoAppAndroid.binance.dto

data class Trade(
    val symbol: String,
    val id: Long,
    val orderId: Long,
    val orderListId: Int,
    val price: Float,
    val qty: Float,
    val quoteQty: Float,
    val commission: Float,
    val commissionAsset: String,
    val time: Long,
    val isBuyer: Boolean,
    val isMaker: Boolean,
    val isBestMatch: Boolean
)