package com.example.CryptoAppAndroid.binance.dto

data class AccountInfo(
    val accountType: String,
    var balances: List<AssetBalance>,
    val buyerCommission: Int,
    val canDeposit: Boolean,
    val canTrade: Boolean,
    val canWithdraw: Boolean,
    val makerCommission: Int,
    val permissions: List<String>,
    val sellerCommission: Int,
    val takerCommission: Int,
    val updateTime: Long
)