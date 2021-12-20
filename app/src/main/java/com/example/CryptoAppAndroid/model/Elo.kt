package com.example.CryptoAppAndroid.model

import com.google.gson.annotations.SerializedName

data class Elo(
    val coin: String,
    @SerializedName("start_time") val startTime: Long,
    @SerializedName("elo_rating") val elo: Double,
    @SerializedName("end_time") val endTime: Long
)
