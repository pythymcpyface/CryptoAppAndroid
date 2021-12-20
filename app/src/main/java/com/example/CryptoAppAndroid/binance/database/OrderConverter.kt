package com.example.CryptoAppAndroid.binance.database

import androidx.room.TypeConverter
import com.example.CryptoAppAndroid.binance.dto.Order
import com.google.gson.reflect.TypeToken
import com.google.gson.Gson
import java.lang.reflect.Type


class OrderConverter {
    @TypeConverter
    fun fromOrder(order: Order?): String? {
        if (order == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<Order?>() {}.type
        return gson.toJson(order, type)
    }

    @TypeConverter
    fun toOrder(orderString: String?): Order? {
        if (orderString == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<Order?>() {}.type
        return gson.fromJson<Order>(orderString, type)
    }
}