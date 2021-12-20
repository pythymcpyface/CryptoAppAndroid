package com.example.CryptoAppAndroid.binance.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.CryptoAppAndroid.binance.database.entity.OrderDbo
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrder(order: OrderDbo)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrders(orders: List<OrderDbo>)

    @Query("SELECT * FROM order_table LIMIT 1")
    suspend fun selectMostRecentOrder(): OrderDbo?

    @Query("SELECT * FROM order_table")
    suspend fun selectAllOrders(): List<OrderDbo>?

    @Query("SELECT * FROM order_table")
    fun selectAllOrdersFlow(): Flow<List<OrderDbo>>

    @Query("DELETE FROM order_table")
    suspend fun deleteAll()

}