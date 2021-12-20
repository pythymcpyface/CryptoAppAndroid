package com.example.CryptoAppAndroid.binance.dto

data class Order(
    val orderId: Long,
    val symbol: String,
    val clientOrderId: String,
    val cummulativeQuoteQty: String,
    val executedQty: Float,
    val icebergQty: String,
    val isWorking: Boolean,
    val orderListId: Int,
    val origQty: Float,
    val origQuoteOrderQty: String,
    val price: Float,
    val side: String,
    val status: String,
    val stopPrice: String,
    val time: Long,
    val timeInForce: String,
    val type: String,
    val updateTime: Long
) {
    fun getTypeSide(): String {
        return "$type/$side"
    }
}