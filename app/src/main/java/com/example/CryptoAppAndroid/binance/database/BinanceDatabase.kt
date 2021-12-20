package com.example.CryptoAppAndroid.binance.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.CryptoAppAndroid.binance.database.dao.MarketCapDao
import com.example.CryptoAppAndroid.binance.database.dao.OrderDao
import com.example.CryptoAppAndroid.binance.database.entity.MarketCapDbo
import com.example.CryptoAppAndroid.binance.database.entity.OrderDbo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [
    OrderDbo::class,
    MarketCapDbo::class
                     ], version = 2, exportSchema = false)
@TypeConverters(DateConverters::class, OrderConverter::class)
abstract class BinanceDatabase : RoomDatabase() {

    abstract fun orderDao(): OrderDao
    abstract fun marketCapDao(): MarketCapDao

    private class BinanceDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.orderDao(), database.marketCapDao())
                }
            }
        }

        suspend fun populateDatabase(
            orderDao: OrderDao,
            marketCapDao: MarketCapDao
        ) {
//            orderDao.deleteAll()
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: BinanceDatabase? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): BinanceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BinanceDatabase::class.java,
                    "binance_database"
                ).addMigrations(MIGRATION_1_2)
                .addCallback(BinanceDatabaseCallback(scope))
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS " +
                "`market_cap_table` (`id` INTEGER NOT NULL, `time` INTEGER NOT NULL, `marketCap` " +
                "DOUBLE NOT NULL, `cumulativeMarketCap` DOUBLE NOT NULL, `percentChangeMarketCap`" +
                " DOUBLE NOT NULL, `cumulativePercent` DOUBLE NOT NULL, PRIMARY KEY(`id`))")
    }
}