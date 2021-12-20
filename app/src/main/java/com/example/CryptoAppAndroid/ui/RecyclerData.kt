package com.example.CryptoAppAndroid.ui

import com.example.CryptoAppAndroid.model.Elo
import com.example.CryptoAppAndroid.model.Price
import com.example.CryptoAppAndroid.model.Stats

class RecyclerData(
    val elos: List<Elo>,
    val stats: List<Stats>,
    val prices: List<Price>,
    val coinList: List<String>
)
