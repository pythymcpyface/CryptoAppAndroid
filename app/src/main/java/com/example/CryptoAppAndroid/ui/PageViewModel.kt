package com.example.CryptoAppAndroid.ui

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.CryptoAppAndroid.binance.database.entity.MarketCapDbo
import com.example.CryptoAppAndroid.binance.database.entity.OrderDbo
import com.example.CryptoAppAndroid.repository.Repository
import kotlinx.coroutines.*
import java.time.LocalDateTime
import java.util.*

class PageViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = Repository(application.applicationContext)
    val allOrders: LiveData<List<OrderDbo>> = repository.allOrders.asLiveData()
    val allMarketCaps: LiveData<List<MarketCapDbo>> = repository.allMarketCaps.asLiveData()
    var hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    var minute = Calendar.getInstance().get(Calendar.MINUTE)
    var day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    var month = Calendar.getInstance().get(Calendar.MONTH)
    var year = Calendar.getInstance().get(Calendar.YEAR)
    @RequiresApi(Build.VERSION_CODES.O)
    var pickedDateTime: LocalDateTime = LocalDateTime.of(2012, 12, 11, 16, 0)
    private val _index = MutableLiveData<Int>()
    var filter: String = ""

    internal val syncDataWorkInfoItems: LiveData<List<WorkInfo>> =
        WorkManager.getInstance(application).getWorkInfosByTagLiveData("syncDataWorker")

    internal val syncTradesWorkInfoItems: LiveData<List<WorkInfo>> =
        WorkManager.getInstance(application).getWorkInfosByTagLiveData("syncTradesWorker")

    val recyclerData: MutableLiveData<RecyclerData> by lazy {
        MutableLiveData<RecyclerData>()
    }

    fun setIndex(index: Int) {
        _index.value = index
    }

    fun getRecyclerData(context: Context, handler: CoroutineExceptionHandler) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val elos = repository.getElos(context)
                val prices = repository.getPrices(context)
                val coinList = repository.getCoinList(elos)
                val stats = repository.getStats(elos)

                recyclerData.postValue(RecyclerData(elos, stats, prices, coinList))
            } catch (e: Exception) {
                handler.handleException(this.coroutineContext, e)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun getAllPricesAtTime(
        orderId: Long?,
        startTime: Long?,
        endTime: Long?,
        limit: Int?,
        context: Context
    ) {
        repository.getAllPricesAtTime(
            orderId = orderId,
            startTime = startTime,
            endTime = endTime,
            limit = limit,
            context = context,
            scope = viewModelScope
        )
    }

    fun deleteAll() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteMarketCaps()
            repository.deleteOrders()
        }
    }

    suspend fun getPairFromSymbol(symbol: String, context: Context) = repository.getPairFromSymbol(symbol, context)

}