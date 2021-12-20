package com.example.CryptoAppAndroid.ui

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.*

class MyXAxisValueFormatter: IndexAxisValueFormatter() {

    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        return try {
            val sdf = SimpleDateFormat("dd-MMM-yy HH:MM", Locale.UK)
            sdf.format(Date(value.toLong()))
        } catch (e: Exception) {
            value.toString()
        }
    }
}