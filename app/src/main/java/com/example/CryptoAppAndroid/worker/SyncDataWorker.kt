package com.example.CryptoAppAndroid.worker

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.CryptoAppAndroid.ui.createChannel
import com.example.CryptoAppAndroid.repository.Repository
import com.example.CryptoAppAndroid.ui.sendNotification
import kotlin.math.roundToLong


class SyncDataWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    private val repository = Repository(context)
    private val notificationManager = ContextCompat.getSystemService(
        context,
        NotificationManager::class.java
    ) as NotificationManager

    @RequiresApi(Build.VERSION_CODES.M)
    override suspend fun doWork(): Result {
        return try {
            try {
                Log.d("StonksDebug", "Run work manager")
                //Do Your task here
                doYourTask(applicationContext)
                Result.success()
            } catch (e: Exception) {
                Log.d("StonksDebug", "exception in doWork ${e.message}")
                Result.failure()
            }
        } catch (e: Exception) {
            Log.d("StonksDebug", "exception in doWork ${e.message}")
            Result.failure()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private suspend fun doYourTask(context: Context) {
        val elos = repository.getElos(context)
//        val prices = repository.getPrices()
//        val coinList = repository.getCoinList(elos)
        val statsList = repository.getStats(elos)

        for (elo in elos) {
            var iteration = 0
            val eloTime = elo.endTime
            val eloRating = elo.elo
            val coin = elo.coin
            for (stats in statsList) {
                val statsTime = stats.time
                val average = stats.average
                val upperLimit = average.plus(3.times(stats.stdDev))
                val lowerLimit = average.minus(3.times(stats.stdDev))
                val now = System.currentTimeMillis()
                val millisInMin = 1000.times(60)
                val millisFromNow = eloTime.minus(now)
                val minsFromNow = millisFromNow.div(millisInMin)
                val channelId = "${coin}_channel_id"
                val channelName = "${coin}_channel"
                if (eloTime == statsTime && eloRating < lowerLimit && minsFromNow > -15) {
                    notificationManager.createChannel(channelId, channelName)
                    notificationManager.sendNotification(
                        "$coin: ${(eloRating * 100).roundToLong() / 100.0} < ${(lowerLimit * 100).roundToLong() / 100.0} ${minsFromNow.times(-1)} mins ago", applicationContext, channelId, iteration)
                } else if (eloTime == statsTime && eloRating > upperLimit && minsFromNow > -15) {
                    notificationManager.createChannel(channelId, channelName)
                    notificationManager.sendNotification(
                        "$coin: ${(eloRating * 100).roundToLong() / 100.0} > ${(upperLimit * 100).roundToLong() / 100.0} ${minsFromNow.times(-1)} mins ago", applicationContext, channelId, iteration)
                }
                iteration += 1
            }
        }
    }

}