package com.example.CryptoAppAndroid.model

import com.google.gson.annotations.SerializedName

data class Price(
    val pair: String,
    @SerializedName("start_time") val startTime: Long,
    @SerializedName("end_time") val endTime: Long,
    @SerializedName("open_price") val openPrice: Double,
    @SerializedName("close_price") val closePrice: Double
)
