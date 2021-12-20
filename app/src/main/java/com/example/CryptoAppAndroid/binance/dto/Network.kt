package com.example.CryptoAppAndroid.binance.dto

data class Network(
    val addressRegex: String,
    val coin: String,
    val depositDesc: String,
    val depositEnable: Boolean,
    val isDefault: Boolean,
    val memoRegex: String,
    val minConfirm: Int,
    val name: String,
    val network: String,
    val resetAddressStatus: Boolean,
    val sameAddress: Boolean,
    val specialTips: String,
    val specialWithdrawTips: String,
    val unLockConfirm: Int,
    val withdrawDesc: String,
    val withdrawEnable: Boolean,
    val withdrawFee: String,
    val withdrawIntegerMultiple: String,
    val withdrawMax: String,
    val withdrawMin: String
)