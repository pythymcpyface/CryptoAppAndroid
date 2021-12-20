package com.example.CryptoAppAndroid.repository

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.example.CryptoAppAndroid.binance.BinanceApi
import com.example.CryptoAppAndroid.binance.database.BinanceDatabase
import com.example.CryptoAppAndroid.binance.database.entity.MarketCapDbo
import com.example.CryptoAppAndroid.binance.database.entity.OrderDbo
import com.example.CryptoAppAndroid.binance.dto.ErrorMessage
import com.example.CryptoAppAndroid.binance.dto.Order
import com.example.CryptoAppAndroid.binance.dto.ServerTime
import com.example.CryptoAppAndroid.binance.dto.exchange.ExchangeInfo
import com.example.CryptoAppAndroid.binance.dto.price.KLines
import com.example.CryptoAppAndroid.model.Elo
import com.example.CryptoAppAndroid.model.Price
import com.example.CryptoAppAndroid.model.Stats
import com.example.CryptoAppAndroid.retrofit.ApiService
import com.example.CryptoAppAndroid.ui.createChannel
import com.example.CryptoAppAndroid.ui.sendNotification
import com.google.gson.Gson
import drewcarlson.coingecko.CoinGeckoClient
import drewcarlson.coingecko.models.coins.CoinList
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import kotlin.coroutines.resumeWithException
import kotlin.math.pow
import kotlin.math.roundToLong
import kotlin.math.sqrt

class Repository(context: Context) {

    private val recvWindow = 60000L
    private var retryAfter: Long = 30000L
    private var coinGecko = CoinGeckoClient.create()
    private val binanceDatabase: BinanceDatabase = BinanceDatabase.getDatabase(context, GlobalScope)
    private val notificationManager = ContextCompat.getSystemService(
        context,
        NotificationManager::class.java
    ) as NotificationManager

    val allOrders: Flow<List<OrderDbo>> = binanceDatabase.orderDao().selectAllOrdersFlow()
    val allMarketCaps: Flow<List<MarketCapDbo>> = binanceDatabase.marketCapDao().selectAllMarketCapsFlow()

    private suspend fun getServerTime(context: Context): ServerTime? =
        BinanceApi.getData().getServerTime()?.await(context)

    private suspend fun getPriceAtTime(
        symbol: String,
        time: Long,
        context: Context,
    ): KLines {
        return retryIO(initialDelay = retryAfter) {
            BinanceApi.getData().getKLines(
                symbol,
                time.minus(60000L).toString(),
                time.toString()
            ).await(context)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun getAllPricesAtTime(
        orderId: Long?,
        startTime: Long?,
        endTime: Long?,
        limit: Int?,
        context: Context,
        scope: CoroutineScope,
    ) {

        scope.launch(Dispatchers.IO) {
            withContext(Dispatchers.IO) {

                val lastEntry = binanceDatabase.orderDao().selectMostRecentOrder()
                val lastTime = lastEntry?.order?.time ?: startTime

//                Log.d("Stonksdebug", "Starting getAllPrices")
                val allOrders = getAllOrders(
                    orderId,
                    lastTime,
                    endTime,
                    limit,
                    context
                ).flatten()

                val allDbOrders = binanceDatabase.orderDao().selectAllOrders()?.map { it.order } ?: mutableListOf()

                val allOrdersCombined = allDbOrders.plus(allOrders)

                val orderMap = allOrdersCombined.map { order ->
                        Log.i("Stonksdebug", "order = $order")
                        var value = 0.0
                        val pair = getPairFromSymbol(order.symbol, context)
                        Log.i("Stonksdebug", "pair = $pair")
                        val coin = pair.split("-")[0]
                        if (coin != "USDT") {
                            val kline = getPriceAtTime(
                                "${pair.split("-")[0]}USDT",
                                order.time, context)
                            Log.i("Stonksdebug", "kline = $kline")
                            val price = if (!kline.isEmpty()) { kline[0][4] } else { 0.0 }
                            Log.i("Stonksdebug", "price = $price")
                            val qty = order.origQty
                            value = price.times(qty)
                            Log.i("Stonksdebug", "value = $value")
                        }

                        order to value
                    }.toMap().toSortedMap(compareBy { it.time })

                Log.d("StonksDebug", "orderMap = $orderMap")

                val cumulativeSortedOrderMap = convertOrderMapToSortedCumulativeMap(orderMap)
                val marketCapMap = getMarketCap(cumulativeSortedOrderMap, context)
                val cumulativeSortedMarketCapMap = convertMarketCapMapToSortedCumulativeMap(marketCapMap)
                Log.d("StonksDebug", "sortedOrderMap = $cumulativeSortedOrderMap, marketcapmap = $cumulativeSortedMarketCapMap")

                binanceDatabase.orderDao().deleteAll()

                binanceDatabase.orderDao().insertOrders(
                    cumulativeSortedOrderMap.map { (order, stats) ->
                    Log.i("Stonksdebug", "order stats = $stats")
                    OrderDbo(
                        order=order,
                        valueUsd=stats[0],
                        cumulativeValueUsd=stats[1],
                        percentChangeUsd=stats[2],
                        cumulativePercentUsd=stats[3]
                    )
                }
                )

                cumulativeSortedOrderMap.map { (order, stats) ->

                    val now = System.currentTimeMillis()
                    val millisInMin = 1000.times(60)
                    val millisFromNow = now.minus(order.time)
                    val minsFromNow = millisFromNow.div(millisInMin)

//                    Log.d("StonksDebug", "now = $now, millisfromnow = $millisFromNow, minsfromnow = $minsFromNow")

                    if (minsFromNow < 30) {
                        val channelId = "${order.symbol}_${order.side}_channel_id"
                        val channel = "${order.symbol}_${order.side}_channel"
                        notificationManager.createChannel(channelId, channel)
                        notificationManager.sendNotification(
                            "${order.side} order for ${order.symbol} $minsFromNow " +
                                    "mins ago. Cumulative % = " +
                                    "${(stats[3] * 100).roundToLong() / 100.0}, current value = " +
                                    "${(stats[0] * 100).roundToLong() / 100.0}",
                            context, channelId, order.orderId.toInt()
                        )
                    }
                }

                binanceDatabase.marketCapDao().deleteAll()

                binanceDatabase.marketCapDao().insertMarketCaps(
                cumulativeSortedMarketCapMap.map { (time, stats) ->
//                    Log.i("Stonksdebug", "market stats = $stats")
                    MarketCapDbo(
                            time=time,
                            marketCap=stats[0],
                            cumulativeMarketCap=stats[1],
                            percentChangeMarketCap=stats[2],
                            cumulativePercent=stats[3]
                    )
                }
                )
            }
        }
    }

    private suspend fun getMarketCap(
        cumulativeSortedValueMap: Map<Order, List<Double>>,
        context: Context,
    ): Map<Long, Long> {

        return cumulativeSortedValueMap.mapNotNull { (order, _) ->
            val time = order.time
            val millisInDay = 24.times(60.times(60.times(1000)))
            val rounded = time.div(millisInDay).toDouble().roundToLong()
            val roundedToDay = rounded.times(millisInDay)
//            Log.d("StonksDebug",
//                "time = $time, millis in hour = $millisInDay, time/millisinhour = ${
//                    time.div(millisInDay).toDouble()
//                }, rounded = $rounded, roundedtoday = $roundedToDay")
            roundedToDay }.toList().distinct()
            .map { time -> (time to getMarketCapAtTime(time, context)) }.toMap()
    }

    private suspend fun getMarketCapAtTime(time: Long, context: Context): Long {
        val exchangeInfo = retryIO { BinanceApi.getData().getExchangeInfo().await(context) }
        val pairs = getPairs(exchangeInfo)
        val coins = getCoins(pairs)
        val pairsPerCoin = getPairsPerCoin(coins, pairs)
        val wantedCoins = getWantedCoins(pairsPerCoin)

        val cgCoins = retryIO { coinGecko.getCoinList() }
//        Log.d("StonksDebug", "cgCoins = $cgCoins")

        val date = SimpleDateFormat("dd-MM-yyyy").format(Date(time))

//        Log.d("StonksDebug", "time = $time, date = $date")

        val marketData = wantedCoins.mapNotNull { coin ->
            getIdFromCoin(cgCoins, coin.lowercase())?.let {
                retryIO {
                    with(coinGecko.getCoinHistoryById(it, date)) {
//                        Log.d("StonksDebug", "coinhistory for $coin = $this")
                        this
                    }
                }
            }
        }
//        Log.d("StonksDebug", "marketData = $marketData")

        val marketCap = marketData.map { it.marketData?.marketCap }
//        Log.d("StonksDebug", "marketCap = $marketCap")
        val totalMarketCap =
            marketCap.mapNotNull { it?.filter { (key, _) -> key == "usd" }?.values?.sum() }
                .sumOf { it }.toLong()
//        Log.d("StonksDebug", "totalMarketCap = $totalMarketCap")
        return totalMarketCap

    }

    private fun convertOrderMapToSortedCumulativeMap(map: Map<Order, Double>): Map<Order, List<Double>> {
        with(map.toSortedMap(compareBy { it.time }).toList()) {
            return this.associate { (order, valueUsd) ->
                (order to listOf(
                    valueUsd,
                    valueUsd.minus(this[0].second),
                    100.times(valueUsd.minus(this.last().second).div(this.last().second)),
                    100.times(valueUsd.minus(this[0].second)).div(this[0].second)
                )
                        )
            }
        }
    }

    private fun convertMarketCapMapToSortedCumulativeMap(map: Map<Long, Long>): Map<Long, List<Double>> {

        with(map.toSortedMap(compareBy { it }).toList()) {

            return this.associate { (key, value) ->
                (key to listOf(
                    value,
                    value.minus(this[0].second.toDouble()),
                    100.0.times(value.minus(this.last().second.toDouble())).div(this.last().second),
                    100.0.times(value.minus(this[0].second.toDouble())).div(this[0].second)
                ).map {
                    it.toDouble()
                })
            }
        }
    }

    private fun getCoinIdDict(cgCoins: List<CoinList>): Map<String, String> {

        return cgCoins.associate {
//            Log.d("StonksDebug", "coiniddict symbol = ${it.symbol}, id = ${it.id}")
            Pair(it.symbol, it.id)
        }
    }

    private fun getIdFromCoin(cgCoins: List<CoinList>, coin: String): String? {
        val coinIdDict = getCoinIdDict(cgCoins)
//        Log.d("StonksDebug", "id = ${coinIdDict[coin]}, coinIdDict = $coinIdDict")

        return coinIdDict[coin]

    }

    private suspend fun getAllOrders(
        orderId: Long?,
        startTime: Long?,
        endTime: Long?,
        limit: Int?,
        context: Context,
    ): List<List<Order>> {
        return getWantedSymbols(context).mapNotNull { symbol ->
//            Log.d("StonksDebug", "symbol = $symbol")
            getOrdersForSymbol(
                symbol = symbol,
                orderId = orderId,
                startTime = startTime,
                endTime = endTime,
                limit = limit,
                context = context
            )
        }
    }

    private suspend fun getOrdersForSymbol(
        symbol: String,
        orderId: Long?,
        startTime: Long?,
        endTime: Long?,
        limit: Int?,
        context: Context,
    ): List<Order>? {

        return retryIO(initialDelay = retryAfter) {
            BinanceApi.getData().getOrders(
                symbol = symbol,
                orderId = orderId,
                startTime = startTime,
                endTime = endTime,
                limit = limit,
                recvWindow = recvWindow,
                timestamp = getServerTime(context)?.serverTime ?: System.currentTimeMillis()
            )?.await(context)
        }
    }

    suspend fun deleteMarketCaps() = binanceDatabase.marketCapDao().deleteAll()
    suspend fun deleteOrders() = binanceDatabase.orderDao().deleteAll()

    suspend fun getElos(context: Context): List<Elo> {
        val call = ApiService.getData().getElos()

        return call.await(context)
    }

    suspend fun getPrices(context: Context): List<Price> {
        val call = ApiService.getData().getPrices()

        return call.await(context)
    }

    fun getCoinList(elos: List<Elo>): List<String> {
        val coinList = mutableListOf<String>()

        for (elo in elos) {
            val coin = elo.coin
            if (!coinList.contains(coin) && coin != "null") {
                coinList.add(coin)
            }
        }
        return coinList
    }

    fun getStats(elos: List<Elo>): List<Stats> {
        val times = getTimes(elos)
        val statsList = mutableListOf<Stats>()

        for (time in times) {
            val eloList = mutableListOf<Double>()
            for (elo in elos) {
                val eloRating = elo.elo
                if (time == elo.endTime) {
                    eloList.add(eloRating)
                }
            }
            val mean = eloList.average()
            val sd = stdDev(eloList)
            val stats = Stats(mean, sd, time)
            statsList.add(stats)
        }

        return statsList
    }

    fun getTimes(elos: List<Elo>): List<Long> {
        val times = mutableListOf<Long>()

        for (elo in elos) {
            val time = elo.endTime
            if (!times.contains(time)) {
                times.add(time)
            }
        }
        return times
    }

    private fun stdDev(numArray: MutableList<Double>): Double {
        // Calculate standard deviation
        val mean = numArray.average()
        val sd = numArray.fold(0.0, { accumulator, next -> accumulator + (next - mean).pow(2.0) })

        return sqrt(sd / numArray.size)
    }

    private fun getPairs(exchangeInfo: ExchangeInfo): List<String> {
//      Get full list of symbols from exchange in format "AAA-BBB"
        val symbolInfos = exchangeInfo.symbols
        val pairs = mutableListOf<String>()
//      Loop through entry in exchange response
        for (symbolInfo in symbolInfos) {
//          If symbol is trading then add to pairs list
            if (symbolInfo.status == "TRADING") {
                val baseAsset = symbolInfo.baseAsset
                val quoteAsset = symbolInfo.quoteAsset
                val pair = "$baseAsset-$quoteAsset"
                pairs.add(pair)
            }
        }
        return pairs
    }

    private fun getCoins(pairs: List<String>): List<String> {
//      Get full list of coins from exchange by splitting pairs by "-"
        val coins = mutableListOf<String>()
//      Loop through each pair
        for (pair in pairs) {
//          Split pair by "-" to get coins
            val coinA = pair.split("-")[0]
            val coinB = pair.split("-")[1]
//          If coin is not already in coins list then add it
            if (coins.find { coin -> coinA == coin } == null) {
                coins.add(coinA)
            }
            if (coins.find { coin -> coinB == coin } == null) {
                coins.add(coinB)
            }
        }
        return coins
    }

    private fun getPairsPerCoin(coins: List<String>, pairs: List<String>): Map<String, Int> {
//      Get number of pairs which coin trades in
        val pairsPerCoin = mutableMapOf<String, Int>()
//      Loop through each coin
        for (coin in coins) {
//          Reset counter for coin to 0
            var coinCount = 0
//          Loop through each pair
            for (pair in pairs) {
                val coinA = pair.split("-")[0]
                val coinB = pair.split("-")[1]
//              Search for coin in pair
                if (coinA == coin || coinB == coin) {
//                  Add 1 to counter for coin if pair contains coin
                    coinCount += 1
                }
            }
//          Add coin's counter value to map
            pairsPerCoin[coin] = coinCount
        }
        return pairsPerCoin
    }

    private fun getWantedCoins(
        pairsPerCoin: Map<String, Int>,
        coinsTradedWith: Int = 10,
    ): List<String> {
//      Get list of coins which trade in more pairs than required number
        val coins = pairsPerCoin.keys
        val wantedCoins = mutableListOf<String>()
//      Loop through each coin
        for (coin in coins) {
//          Check if number of pairs which coin trades in is higher than required number
            if (pairsPerCoin[coin]!! >= coinsTradedWith) {
//              Add to wanted coins list
                wantedCoins.add(coin)
            }
        }
        return wantedCoins
    }

    private suspend fun getWantedSymbols(context: Context): List<String> {
        val exchangeInfo = retryIO { BinanceApi.getData().getExchangeInfo().await(context) }
//        Log.d("StonksDebug", "Exchange info = $exchangeInfo")
        val pairs = getPairs(exchangeInfo)
        val coins = getCoins(pairs)
        val symbols = pairs.map { pair -> pair.replace("-", "") }
        for (symbol in symbols) {
//            Log.d("StonksDebug", "Exchange symbol = $symbol")
        }
        val pairsPerCoin = getPairsPerCoin(coins, pairs)
        val wantedCoins = getWantedCoins(pairsPerCoin)
//      Convert wanted coins list to pairs by splitting with "-"
        val wantedSymbols = mutableListOf<String>()
//      Loop through coins twice in wanted coins
        for (coinA in wantedCoins) {
            for (coinB in wantedCoins) {
//              Check if this pair of coins is traded in exchange list of symbols
                if ((coinA + coinB) in symbols) {
                    val wantedSymbol = "$coinA$coinB"
                    if (wantedSymbols.find { pair -> pair == wantedSymbol } == null) {
                        wantedSymbols.add(wantedSymbol)
                    }
                } else if ((coinB + coinA) in symbols) {
                    val wantedSymbol = "$coinB$coinA"
                    if (wantedSymbols.find { pair -> pair == wantedSymbol } == null) {
                        wantedSymbols.add(wantedSymbol)
                    }
                }
            }
        }
//        Log.d("StonksDebug", "wantedSymbols = $wantedSymbols")
        return wantedSymbols
    }

    suspend fun getPairFromSymbol(symbol: String, context: Context): String {
        val exchangeInfo = retryIO { BinanceApi.getData().getExchangeInfo().await(context) }
        val pairs = getPairs(exchangeInfo)

        return pairs.associateBy { it.replace("-", "") }[symbol] ?: " - "
    }

    private suspend fun <T> retryIO(
        times: Int = Int.MAX_VALUE,
        initialDelay: Long = 30 * 1000, // 30 seconds
        maxDelay: Long = 60 * 1000,    // 5 minutes
        factor: Double = 1.2,
        block: suspend () -> T,
    ): T {
        var currentDelay = initialDelay
        repeat(times - 1) {
//            Log.d("StonksDebug", "Running retry")
            try {
                return block()
            } catch (e: Exception) {
                // you can log an error here and/or make a more finer-grained
                // analysis of the cause to see if retry is needed
//                Log.d("StonksDebug", "Delaying by $currentDelay due to Error ${e.message}, ${e.cause}, $e, ${e.localizedMessage}")
                if (!e.message?.contains("400")!!) {
                    delay(currentDelay)
                    currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
                }
            }
        }
        return block() // last attempt
    }

    @ExperimentalCoroutinesApi
    suspend fun <T : Any> Call<T>.await(context: Context): T {
        return suspendCancellableCoroutine { continuation ->
            continuation.invokeOnCancellation {
                cancel()
            }
            enqueue(object : Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    if (response.isSuccessful) {
                        val body = response.body()
//                        Log.i("Stonksdebug", "response body = $body")
                        if (body != null) {
                            continuation.resume(body, onCancellation = null)
                        }
                    } else {
                        val error: ErrorMessage = Gson().fromJson(
                            response.errorBody()!!.charStream(),
                            ErrorMessage::class.java
                        )
                        val headers = response.headers()
                        retryAfter = headers["Retry-After"]?.toLong()?.times(1000) ?: 30000L
//                        Log.d("StonksDebug", "Body = ${response.body()}, error = ${error.code}:${error.msg} ,headers = $headers, retryAfter = $retryAfter")

                        Toast.makeText(context, error.msg, Toast.LENGTH_SHORT).show()
                        continuation.resumeWithException(HttpException(response))
                    }
                }

                override fun onFailure(call: Call<T>, t: Throwable) {
                    Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
                    continuation.resumeWithException(t)
                }

            })
        }
    }
}