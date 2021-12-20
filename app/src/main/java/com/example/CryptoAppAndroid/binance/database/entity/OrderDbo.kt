package com.example.CryptoAppAndroid.binance.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.CryptoAppAndroid.binance.dto.Order

@Entity(tableName = "order_table")
data class OrderDbo(
    @PrimaryKey(autoGenerate = false)
    val order: Order,
    val valueUsd: Double,
    val cumulativeValueUsd: Double,
    val percentChangeUsd: Double,
    val cumulativePercentUsd: Double
    )