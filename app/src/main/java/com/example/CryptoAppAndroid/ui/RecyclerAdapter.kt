package com.example.CryptoAppAndroid.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.CryptoAppAndroid.R
import com.example.CryptoAppAndroid.databinding.EloViewBinding
import com.example.CryptoAppAndroid.repository.Repository
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.utils.EntryXComparator
import java.util.*

class RecyclerAdapter (
    private val recyclerData: RecyclerData,
    private val quoteAsset: String,
    private val stdDevs: Int,
    private val context: Context
    ) : RecyclerView.Adapter<RecyclerAdapter.MyViewHolder>() {

        class MyViewHolder(val binding: EloViewBinding) : RecyclerView.ViewHolder(binding.root)


        // Create new views (invoked by the layout manager)
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): MyViewHolder {
            // create a new view
            val binding = EloViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            // set the view's size, margins, paddings and layout parameters

            return MyViewHolder(binding)
        }

        // Replace the contents of a view (invoked by the layout manager)
        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
//            val eloTimeMap = eloChartData.eloTimeMap
//            val coinList = eloChartData.coinList
//            val priceMap = eloChartData.priceMap
//            val statsMap = eloChartData.statsMap

            val statsList = recyclerData.stats
            val elos = recyclerData.elos
            val prices = recyclerData.prices
            val coinList = recyclerData.coinList

            val now = System.currentTimeMillis()
            val millisInMin = 1000.times(60)

            val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            params.height = 20

//            Log.d("StonksDebug", "Elos = $elos")
//            Log.d("StonksDebug", "Prices = $prices")

            with(holder) {
                with(coinList[position]) {
                    binding.coin.text = this
                    val lineChart = binding.lineChart
                    val eloChartEntries = mutableListOf<Entry>()
                    val priceChartEntries = mutableListOf<Entry>()
                    val upperChartEntries = mutableListOf<Entry>()
                    val lowerChartEntries = mutableListOf<Entry>()
                    val avgChartEntries = mutableListOf<Entry>()
                    val percentChartEntries = mutableListOf<Entry>()

//                    Log.d("StonksDebug", "Coin = $this")

                    for (elo in elos) {
                        val coin = elo.coin

                        if (coin == this) {
                            val eloRating = elo.elo
                            val timestamp = elo.endTime
//                    Subtracted other way round to get negative number to invert x axis
                            val millisFromNow = timestamp.minus(now)
                            val minsFromNow = millisFromNow.div(millisInMin)
                            val eloChartEntry = Entry(minsFromNow.toFloat(), eloRating.toFloat())
//                            Log.d("StonksDebug", "Elo = $elo, inserting entry $eloChartEntry")
                            if (minsFromNow > (-60).times(1)) {
                                eloChartEntries.add(eloChartEntry)
                            }
                        }
                    }

                    for (price in prices) {
                        val pair = price.pair
                        val timestamp = price.endTime
                        val millisFromNow = timestamp.minus(now)
                        val minsFromNow = millisFromNow.div(millisInMin)
                        val closePrice = price.closePrice
                        val coinA = pair.split("-")[0]
                        val coinB = pair.split("-")[1]

                        if (coinA == this && coinB == quoteAsset && coinA != quoteAsset) {
                            val times = Repository(context).getTimes(elos)
                            val sortedTimes = times.sorted()
                            for (time in sortedTimes) {
                                if (time == timestamp) {
                                    val indexOfTime = sortedTimes.indexOf(time)
                                    val timeFiveMinsAgo = sortedTimes[indexOfTime.minus(5)]
                                    for (priceB in prices) {
                                        val timestampB = priceB.endTime
                                        if (timestampB == timeFiveMinsAgo) {
                                            val priceFiveMinsAgo = priceB.closePrice
                                            val percentChartEntry = Entry(minsFromNow.toFloat(), priceFiveMinsAgo.toFloat())
                                            percentChartEntries.add(percentChartEntry)
                                        }
                                    }
                                }
                            }
//                    Subtracted other way round to get negative number to invert x axis
                            val priceChartEntry = Entry(minsFromNow.toFloat(), closePrice.toFloat())
//                            Log.d("StonksDebug", "Price = $price, inserting entry $priceChartEntry")
                            if (minsFromNow > (-60).times(1)) {
                                priceChartEntries.add(priceChartEntry)
                            }
                        }
                    }

                    for (stats in statsList) {
                        val average = stats.average
                        val stdDev = stats.stdDev
                        val time = stats.time
                        val millisFromNow = time.minus(now)
                        val minsFromNow = millisFromNow.div(millisInMin)
                        val upperLimit = average.plus(stdDev.times(stdDevs))
                        val lowerLimit = average.minus(stdDev.times(stdDevs))
                        val upperChartEntry = Entry(minsFromNow.toFloat(), upperLimit.toFloat())
                        val lowerChartEntry = Entry(minsFromNow.toFloat(), lowerLimit.toFloat())
                        val avgChartEntry = Entry(minsFromNow.toFloat(), average.toFloat())
                        if (minsFromNow > (-60).times(1)) {
                            upperChartEntries.add(upperChartEntry)
                            lowerChartEntries.add(lowerChartEntry)
                            avgChartEntries.add(avgChartEntry)
                        }

                    }

                    Collections.sort(eloChartEntries, EntryXComparator())
                    Collections.sort(priceChartEntries, EntryXComparator())
                    Collections.sort(upperChartEntries, EntryXComparator())
                    Collections.sort(lowerChartEntries, EntryXComparator())
                    Collections.sort(avgChartEntries, EntryXComparator())
                    Collections.sort(percentChartEntries, EntryXComparator())
                    val eloChartDataSet = LineDataSet(eloChartEntries, "Elo rating")
                    eloChartDataSet.axisDependency = YAxis.AxisDependency.LEFT
                    val priceChartDataSet = LineDataSet(priceChartEntries, "Price /$quoteAsset")
                    priceChartDataSet.axisDependency = YAxis.AxisDependency.RIGHT
                    val upperChartDataSet = LineDataSet(upperChartEntries, "Mean+${stdDevs}SD")
                    upperChartDataSet.axisDependency = YAxis.AxisDependency.LEFT
                    val lowerChartDataSet = LineDataSet(lowerChartEntries, "Mean-${stdDevs}SD")
                    lowerChartDataSet.axisDependency = YAxis.AxisDependency.LEFT
                    val avgChartDataSet = LineDataSet(avgChartEntries, "Mean")
                    avgChartDataSet.axisDependency = YAxis.AxisDependency.LEFT
                    val percentChartDataSet = LineDataSet(percentChartEntries, "Price Change After 5 mins (%)")
                    percentChartDataSet.axisDependency = YAxis.AxisDependency.LEFT
                    priceChartDataSet.color = R.color.red
                    priceChartDataSet.fillColor = R.color.red
                    priceChartDataSet.highLightColor = R.color.red
                    priceChartDataSet.circleColors = listOf(R.color.red)
                    upperChartDataSet.color = R.color.black
                    upperChartDataSet.fillColor = R.color.black
                    upperChartDataSet.highLightColor = R.color.black
                    upperChartDataSet.circleColors = listOf(R.color.black)
                    lowerChartDataSet.color = R.color.black
                    lowerChartDataSet.fillColor = R.color.black
                    lowerChartDataSet.highLightColor = R.color.black
                    lowerChartDataSet.circleColors = listOf(R.color.black)
                    avgChartDataSet.color = R.color.black
                    avgChartDataSet.fillColor = R.color.black
                    avgChartDataSet.highLightColor = R.color.black
                    avgChartDataSet.circleColors = listOf(R.color.black)
                    percentChartDataSet.color = R.color.red
                    percentChartDataSet.fillColor = R.color.red
                    percentChartDataSet.highLightColor = R.color.red
                    percentChartDataSet.circleColors = listOf(R.color.red)
                    val lineChartData = LineData(
                        eloChartDataSet,
                        priceChartDataSet,
                        upperChartDataSet,
                        lowerChartDataSet,
                        avgChartDataSet,
                        percentChartDataSet
                    )
                    lineChart.data = lineChartData
//                lineChart.axisLeft.axisMinimum = 1100F
//                lineChart.axisLeft.axisMaximum = 1900F
                    lineChart.invalidate()
                }
            }
        }

        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount(): Int {
//            val eloMap = eloChartData.eloTimeMap
//            val coins = mutableListOf<String>()
//            for (eloEntry in eloMap) {
//                val coin = eloEntry.key
//                if (!coins.contains(coin) && coin != quoteAsset) {
//                    coins.add(coin)
//                }
//            }
            return recyclerData.coinList.size
        }
    }