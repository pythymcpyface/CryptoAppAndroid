package com.example.CryptoAppAndroid.model

import com.google.gson.annotations.SerializedName

data class Constant(
    @SerializedName("idconstants") val id: Int,
    @SerializedName("standard_deviations") val standardDeviations: Int,
    @SerializedName("minutes") val minutes: Int,
    @SerializedName("percent_change") val percentChange: Double,
    @SerializedName("base") val base: String,
    @SerializedName("moving_average_n") val movingAverage: Int,
    @SerializedName("pairs_per_coin") val pairsPerCoin: Int,
    @SerializedName("r_value") val rValue: Double,
    @SerializedName("p_value") val pValue: Double
)
