package com.example.CryptoAppAndroid.ui

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.CryptoAppAndroid.R
import com.example.CryptoAppAndroid.binance.database.entity.MarketCapDbo
import com.example.CryptoAppAndroid.binance.database.entity.OrderDbo
import com.example.CryptoAppAndroid.databinding.FragmentSecondaryBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.utils.EntryXComparator
import java.util.*
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import com.example.CryptoAppAndroid.worker.WorkManagerScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.math.pow
import kotlin.math.roundToLong


/**
 * A placeholder fragment containing a simple view.
 */
class PlaceholderFragment2 : Fragment() {

    private lateinit var pageViewModel: PageViewModel
    private var _binding: FragmentSecondaryBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageViewModel = ViewModelProvider(this).get(PageViewModel::class.java).apply {
            setIndex(arguments?.getInt(ARG_SECTION_NUMBER) ?: 2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        _binding = FragmentSecondaryBinding.inflate(inflater, container, false)

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.button.setOnClickListener {
            WorkManagerScheduler.cancelWorkerByTag(requireContext(), "syncTradesWorker")
            binding.refresh.isRefreshing = true
            pageViewModel.getAllPricesAtTime(
                startTime = pageViewModel.pickedDateTime.atZone(ZoneOffset.UTC).toEpochSecond().times(1000L),
                limit = null,
                orderId = null,
                endTime = null,
                context = requireContext()
            )
        }

        binding.refresh.setOnRefreshListener {
            WorkManagerScheduler.cancelWorkerByTag(requireContext(), "syncTradesWorker")
            binding.refresh.isRefreshing = true
            pageViewModel.getAllPricesAtTime(
                startTime = pageViewModel.pickedDateTime.atZone(ZoneOffset.UTC).toEpochSecond().times(1000L),
                limit = null,
                orderId = null,
                endTime = null,
                context = requireContext()
            )
        }

        binding.dateButton.setOnClickListener {
            popUpDatePicker()
        }

        pageViewModel.allOrders.observeForever(Observer { orders ->

            if (!orders.isNullOrEmpty()) {

                GlobalScope.launch(Dispatchers.IO) {

                    val sortedOrders = orders.sortedBy { it.order.time }

                    val lastOrder = sortedOrders.last()
                    val firstOrder = sortedOrders.first()

                    val currentValue = lastOrder.valueUsd
//                    Log.d("StonksDebug", "currentvalue = $currentValue")
                    val coinA = pageViewModel.getPairFromSymbol(lastOrder.order.symbol, requireContext()).split("-")[0]
//                    Log.d("StonksDebug", "coinA = $coinA")
                    val coinB = pageViewModel.getPairFromSymbol(lastOrder.order.symbol, requireContext()).split("-")[1]
//                    Log.d("StonksDebug", "coinB = $coinB")
                    val side = lastOrder.order.side
//                    Log.d("StonksDebug", "side = $side")
                    val currentCoin = if (side == "BUY") {coinA} else {coinB}
//                    Log.d("StonksDebug", "currentCoin = $currentCoin")
                    val numberOfTrades = sortedOrders.size
//                    Log.d("StonksDebug", "numberOfTrades = $numberOfTrades")
                    val lastOrderTime = lastOrder.order.time
//                    Log.d("StonksDebug", "lastOrderTime = $lastOrderTime")
                    val firstOrderTime = firstOrder.order.time
//                    Log.d("StonksDebug", "firstOrderTime = $firstOrderTime")
                    val days = (lastOrderTime.minus(firstOrderTime.toDouble())).div(1000.times(60.times(60.times(24))))
//                    Log.d("StonksDebug", "days = $days")
                    val tradesPerDay = numberOfTrades.div(days).toInt()
//                    Log.d("StonksDebug", "tradesPerDay = $tradesPerDay")
                    val averagePercent = sortedOrders.map {it.percentChangeUsd}.average()
//                    Log.d("StonksDebug", "averagePercent = $averagePercent")
                    val intervalPercent = lastOrder.cumulativePercentUsd
//                    Log.d("StonksDebug", "intervalPercent = $intervalPercent")
                    val intervalLength = lastOrderTime.minus(firstOrderTime)
//                    Log.d("StonksDebug", "intervalLength = $intervalLength")
                    val tradesInInterval = numberOfTrades
//                    Log.d("StonksDebug", "tradesInInterval = $tradesInInterval")
                    val intervalsInYear = 365.24.times(24.times(60.times(60.times(1000)))).div(intervalLength)
//                    Log.d("StonksDebug", "intervalsInYear = $interva/lsInYear")
                    val yearProjection = currentValue.times((100.plus(intervalPercent).div(100)).pow(intervalsInYear))
//                    Log.d("StonksDebug", "yearProjection = $yearProjection")

                    requireActivity().runOnUiThread { binding.projection.text = "Current holdings = ${(currentValue * 100).roundToLong() / 100.0}$currentCoin, projection after 1 year = ${(yearProjection * 100).roundToLong() / 100.0}USD" }

                }

                if (!pageViewModel.allMarketCaps.value.isNullOrEmpty()) {
                    updateChart(orders, pageViewModel.allMarketCaps.value!!)
                }
            }

        })

        pageViewModel.allMarketCaps.observeForever(Observer { marketCaps ->

            if (!pageViewModel.allOrders.value.isNullOrEmpty() && !marketCaps.isNullOrEmpty()) {
                updateChart(pageViewModel.allOrders.value!!, marketCaps)
            }
        })
    }

    private fun updateChart(orders: List<OrderDbo>, marketCaps: List<MarketCapDbo>) {

        binding.refresh.isRefreshing = false
        val orderEntries = orders.map { order ->
            Entry(order.order.time.toFloat(), order.cumulativePercentUsd.toFloat())
        }

        val marketCapEntries = marketCaps.map { marketCap ->
            Entry(marketCap.time.toFloat(), marketCap.cumulativePercent.toFloat())
        }

        Collections.sort(orderEntries, EntryXComparator())
        val orderDataSet = LineDataSet(orderEntries, "Order Cumulative %")
        orderDataSet.axisDependency = YAxis.AxisDependency.LEFT
        Log.d("StonksDebug", "orderEntries = $orderEntries")

        orderDataSet.color = R.color.red
        orderDataSet.fillColor = R.color.red
        orderDataSet.highLightColor = R.color.red
        orderDataSet.circleColors = listOf(R.color.red)

        Collections.sort(marketCapEntries, EntryXComparator())
        val marketCapDataSet = LineDataSet(marketCapEntries, "Market Cap Cumulative %")
        marketCapDataSet.axisDependency = YAxis.AxisDependency.LEFT

        val lineChartData = LineData(orderDataSet, marketCapDataSet)
        val xAxis = binding.lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = MyXAxisValueFormatter()
//        Log.d("StonksDebug", "marketCapEntries = $marketCapEntries")

        marketCapDataSet.color = R.color.black
        marketCapDataSet.fillColor = R.color.black
        marketCapDataSet.highLightColor = R.color.black
        marketCapDataSet.circleColors = listOf(R.color.black)
        binding.lineChart.data = lineChartData
        if (orderEntries.isNotEmpty()) {
            xAxis.axisMinimum = orderEntries.first().x.minus(24.times(60.times(60.times(1000))))
            xAxis.axisMaximum = orderEntries.last().x.plus(24.times(60.times(60.times(1000))))
        } else {
            xAxis.axisMinimum = marketCapEntries.first().x.minus(24.times(60.times(60.times(1000))))
            xAxis.axisMaximum = System.currentTimeMillis().toFloat()
        }
        xAxis.labelRotationAngle = -45F
        binding.lineChart.invalidate()

    }

    companion object {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private const val ARG_SECTION_NUMBER = "section_number"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        @JvmStatic
        fun newInstance(sectionNumber: Int): PlaceholderFragment2 {
            return PlaceholderFragment2().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun popUpTimePicker() {
        val onTimeSetListener =
            OnTimeSetListener { _, selectedHour, selectedMinute ->
                pageViewModel.hour = selectedHour
                pageViewModel.minute = selectedMinute
                val pickedDateTime = LocalDateTime.of(pageViewModel.year, pageViewModel.month, pageViewModel.day, pageViewModel.hour, pageViewModel.minute)
                pageViewModel.pickedDateTime = pickedDateTime
                binding.dateButton.text = pickedDateTime.format(DateTimeFormatter.ofLocalizedDateTime(
                    FormatStyle.LONG, FormatStyle.SHORT)
                )
                pageViewModel.deleteAll()
            }

        // int style = AlertDialog.THEME_HOLO_DARK;
        val timePickerDialog =
            TimePickerDialog(requireContext(),  /*style,*/onTimeSetListener, pageViewModel.hour, pageViewModel.minute, true)
        timePickerDialog.setTitle("Select Time")
        timePickerDialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun popUpDatePicker() {
        val dateSetListener =
            OnDateSetListener { _, year, month, day ->
                pageViewModel.year = year
                pageViewModel.month = month.plus(1)
                pageViewModel.day = day
                popUpTimePicker()
            }
        val style: Int = AlertDialog.THEME_HOLO_LIGHT
        val datePickerDialog = DatePickerDialog(requireContext(), style, dateSetListener, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
        datePickerDialog.setTitle("Select Date")
        datePickerDialog.show()
    }

}