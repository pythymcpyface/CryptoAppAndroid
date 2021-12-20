package com.example.CryptoAppAndroid.binance

import com.example.CryptoAppAndroid.binance.dto.*
import com.example.CryptoAppAndroid.BuildConfig
import com.example.CryptoAppAndroid.binance.security.AuthenticationInterceptor
import com.example.CryptoAppAndroid.binance.dto.exchange.ExchangeInfo
import com.example.CryptoAppAndroid.binance.dto.price.KLines
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import java.util.concurrent.TimeUnit


interface BinanceApi {

    @GET("/api/v3/ticker/price")
    fun getSymbolPriceTicker(
        @Query("symbol") symbol: String?
    ): Call<SymbolExchangeRate>

    @GET("/api/v3/exchangeInfo")
    fun getExchangeInfo(): Call<ExchangeInfo>

    @Headers(BinanceApiConstants.ENDPOINT_SECURITY_TYPE_SIGNED_HEADER)
    @GET("/api/v3/account")
    suspend fun getAccountInfo(
        @Query("recvWindow") recvWindow: Long?,
        @Query("timestamp") timestamp: Long
    ): Response<AccountInfo>

    @Headers(BinanceApiConstants.ENDPOINT_SECURITY_TYPE_SIGNED_HEADER)
    @GET("/sapi/v1/capital/config/getall")
    suspend fun getAllCoinsInformation(
        @Query("recvWindow") recvWindow: Long?,
        @Query("timestamp") timestamp: Long
    ): Response<List<CoinInfo>>

    @Headers(BinanceApiConstants.ENDPOINT_SECURITY_TYPE_APIKEY_HEADER)
    @GET("/api/v3/klines")
    fun getKLines(
        @Query("symbol") pair: String,
        @Query("startTime") startTime: String,
        @Query("endTime") endTime: String,
        @Query("interval") interval: String = "1m"
    ): Call<KLines>

    @Headers(BinanceApiConstants.ENDPOINT_SECURITY_TYPE_SIGNED_HEADER)
    @GET("/api/v3/allOrders")
    fun getOrders(
        @Query("symbol") symbol: String,
        @Query("orderId") orderId: Long?,
        @Query("startTime") startTime: Long?,
        @Query("endTime") endTime: Long?,
        @Query("limit") limit: Int?,
        @Query("recvWindow") recvWindow: Long?,
        @Query("timestamp") timestamp: Long
    ): Call<List<Order>>?

    @GET("/api/v3/time")
    fun getServerTime(): Call<ServerTime>?


    companion object {
        private const val apiKey = BuildConfig.APIKEY
        private const val apiSecret = BuildConfig.APISECRET
        private val authenticationInterceptor = AuthenticationInterceptor(apiKey, apiSecret)

        private val okHttpClient = OkHttpClient().newBuilder()
            .addInterceptor(authenticationInterceptor)
            .connectTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()

        private const val BASE_URL = "https://api.binance.com"

        fun getData(): BinanceApi {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient).build()
                .create(BinanceApi::class.java)
        }
    }

}