package com.example.CryptoAppAndroid.worker

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.work.*
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.TimeUnit


object WorkManagerScheduler {

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshPeriodicDataWork(context: Context) {

        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance()

        // Set Execution around 07:00:00 AM
        dueDate.set(Calendar.HOUR_OF_DAY, 7)
        dueDate.set(Calendar.MINUTE, 0)
        dueDate.set(Calendar.SECOND, 0)
        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24)
        }

        //define constraints
        val myConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val refreshSyncDataWork = PeriodicWorkRequest.Builder(SyncDataWorker::class.java, 15, TimeUnit.MINUTES)
//            .setInitialDelay(minutes, TimeUnit.MINUTES)
            .setConstraints(myConstraints)
            .addTag("syncDataWorker")
            .build()

        WorkManager.getInstance(context).cancelAllWorkByTag("syncDataWorker")

        WorkManager.getInstance(context).enqueueUniquePeriodicWork("syncDataWorker",
            ExistingPeriodicWorkPolicy.REPLACE, refreshSyncDataWork)

        Toast.makeText(context, "Starting data worker", Toast.LENGTH_SHORT).show()

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshPeriodicTradesWork(context: Context, startDate: LocalDateTime) {

        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance()

        // Set Execution around 07:00:00 AM
        dueDate.set(Calendar.HOUR_OF_DAY, 7)
        dueDate.set(Calendar.MINUTE, 0)
        dueDate.set(Calendar.SECOND, 0)
        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24)
        }

        val tradesData = Data.Builder()
            .putInt("Year", startDate.year)
            .putInt("Month", startDate.monthValue)
            .putInt("Day", startDate.dayOfMonth)
            .putInt("Hour", startDate.hour)
            .putInt("Minute", startDate.minute)
            .build()

        //define constraints
        val myConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val refreshSyncTradesWork = PeriodicWorkRequest.Builder(SyncTradesWorker::class.java, 30, TimeUnit.MINUTES)
            .setInputData(tradesData)
            .setConstraints(myConstraints)
            .addTag("syncTradesWorker")
            .build()

        WorkManager.getInstance(context).cancelAllWorkByTag("syncTradesWorker")

        WorkManager.getInstance(context).enqueueUniquePeriodicWork("syncTradesWorker",
            ExistingPeriodicWorkPolicy.KEEP, refreshSyncTradesWork)

        Toast.makeText(context, "Starting trades worker", Toast.LENGTH_SHORT).show()

    }

    fun cancelWorkerByTag(context: Context, tag: String) {
        WorkManager.getInstance(context).cancelAllWorkByTag(tag)
    }
}