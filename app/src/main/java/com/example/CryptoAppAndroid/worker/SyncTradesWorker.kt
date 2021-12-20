package com.example.CryptoAppAndroid.worker

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.CryptoAppAndroid.R
import com.example.CryptoAppAndroid.ui.createChannel
import com.example.CryptoAppAndroid.repository.Repository
import kotlinx.coroutines.*
import java.time.LocalDateTime
import java.time.ZoneOffset


class SyncTradesWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    private val repository = Repository(context)
    private val notificationManager = ContextCompat.getSystemService(
        context,
        NotificationManager::class.java
    ) as NotificationManager

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result {
        return try {
            try {
                Log.d("StonksDebug", "Run work manager")
                //Do Your task here
                withContext(Dispatchers.IO) {
                    val year = inputData.getInt("Year", 2021)
                    val month = inputData.getInt("Month", 13)
                    val day = inputData.getInt("Day", 11)
                    val hour = inputData.getInt("Hour", 16)
                    val minute = inputData.getInt("Minute", 0)
                    val startTime = LocalDateTime.of(year, month, day, hour, minute)

                    setForeground(createForegroundInfo("trades_channel_id", "trades_channel", "Downloading Trades", "Downloading Trades"))
                    doYourTask(this, applicationContext, startTime)
                    Result.success()
                }
            } catch (e: Exception) {
                Log.d("StonksDebug", "exception in doWork ${e.message}")
                Result.failure()
            }
        } catch (e: Exception) {
            Log.d("StonksDebug", "exception in doWork ${e.message}")
            Result.failure()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun doYourTask(scope: CoroutineScope, context: Context, startTime: LocalDateTime) {
        repository.getAllPricesAtTime(
            startTime = startTime.atZone(ZoneOffset.UTC).toEpochSecond().times(1000L),
            limit = null,
            orderId = null,
            endTime = null,
            scope = scope,
            context = context
        )
    }

    // Creates an instance of ForegroundInfo which can be used to update the
    // ongoing notification.
    private fun createForegroundInfo(
        channelId: String,
        channelName: String,
        progress: String,
        title: String
    ): ForegroundInfo {
        // This PendingIntent can be used to cancel the worker
        val intent = WorkManager.getInstance(applicationContext)
            .createCancelPendingIntent(id)

        // Create a Notification channel if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createChannel(channelId, channelName)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(title)
            .setTicker(title)
            .setContentText(progress)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setOngoing(true)
            // Add the cancel action to the notification which can
            // be used to cancel the worker
            .addAction(android.R.drawable.ic_delete, "Cancel", intent)
            .build()

        return ForegroundInfo(1, notification)
    }
}