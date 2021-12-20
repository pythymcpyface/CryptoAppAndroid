package com.example.CryptoAppAndroid.binance.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "market_cap_table")
data class MarketCapDbo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val time: Long,
    val marketCap: Double,
    val cumulativeMarketCap: Double,
    val percentChangeMarketCap: Double,
    val cumulativePercent: Double
    )