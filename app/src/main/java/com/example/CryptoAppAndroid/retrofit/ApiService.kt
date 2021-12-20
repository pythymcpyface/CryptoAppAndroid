package com.example.CryptoAppAndroid.retrofit

import com.example.CryptoAppAndroid.model.Constant
import com.example.CryptoAppAndroid.model.Elo
import com.example.CryptoAppAndroid.model.Price
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

interface ApiService {
    @GET("/Elo")
    fun getElos(): Call<List<Elo>>

    @GET("/Price")
    fun getPrices(): Call<List<Price>>

    @GET("/Constants")
    fun getConstants(): Call<List<Constant>>

    companion object {
        private val okHttpClient = OkHttpClient().newBuilder()
            .connectTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()

        private val BASE_URL = "http://192.168.1.11:5000"

        fun getData(): ApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient).build()
                .create(ApiService::class.java)
        }
    }
}