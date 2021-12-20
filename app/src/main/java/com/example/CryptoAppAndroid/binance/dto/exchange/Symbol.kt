package com.example.CryptoAppAndroid.binance.dto.exchange

import com.example.CryptoAppAndroid.binance.dto.exchange.Filter

data class Symbol(
    val baseAsset: String,
    val baseAssetPrecision: Int,
    val baseCommissionPrecision: Int,
    val filters: List<Filter>,
    val icebergAllowed: Boolean,
    val isMarginTradingAllowed: Boolean,
    val isSpotTradingAllowed: Boolean,
    val ocoAllowed: Boolean,
    val orderTypes: List<String>,
    val permissions: List<String>,
    val quoteAsset: String,
    val quoteAssetPrecision: Int,
    val quoteCommissionPrecision: Int,
    val quoteOrderQtyMarketAllowed: Boolean,
    val quotePrecision: Int,
    val status: String,
    val symbol: String
)