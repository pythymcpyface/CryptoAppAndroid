package com.example.CryptoAppAndroid.binance.dto.exchange

data class Filter(
    val applyToMarket: Boolean,
    val avgPriceMins: Int,
    val filterType: String,
    val limit: Int,
    val maxNumAlgoOrders: Int,
    val maxNumOrders: Int,
    val maxPrice: String,
    val maxQty: String,
    val minNotional: String,
    val minPrice: String,
    val minQty: String,
    val multiplierDown: String,
    val multiplierUp: String,
    val stepSize: String,
    val tickSize: String
)