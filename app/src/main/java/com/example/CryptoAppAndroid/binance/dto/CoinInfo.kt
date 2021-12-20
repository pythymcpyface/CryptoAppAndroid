package com.example.CryptoAppAndroid.binance.dto

import com.fasterxml.jackson.annotation.JsonIgnore

data class CoinInfo(
    val coin: String,
    val depositAllEnable: Boolean,
    val free: String,
    val freeze: String,
    val ipoable: String,
    val ipoing: String,
    val isLegalMoney: Boolean,
    val locked: String,
    val name: String,
    @JsonIgnore
    val networkList: List<Network>?,
    val storage: String,
    val trading: Boolean,
    val withdrawAllEnable: Boolean,
    val withdrawing: String
)