package com.example.CryptoAppAndroid.binance.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.CryptoAppAndroid.binance.database.entity.MarketCapDbo
import com.example.CryptoAppAndroid.binance.database.entity.OrderDbo
import drewcarlson.coingecko.models.shared.Market
import kotlinx.coroutines.flow.Flow

@Dao
interface MarketCapDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMarketCap(marketCap: MarketCapDbo)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMarketCaps(marketCaps: List<MarketCapDbo>)

    @Query("SELECT * FROM market_cap_table")
    fun selectAllMarketCaps(): LiveData<List<MarketCapDbo>>

    @Query("SELECT * FROM market_cap_table")
    fun selectAllMarketCapsFlow(): Flow<List<MarketCapDbo>>

    @Query("DELETE FROM market_cap_table")
    suspend fun deleteAll()

}