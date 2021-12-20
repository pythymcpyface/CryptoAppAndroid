package com.example.CryptoAppAndroid

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.work.WorkInfo
import com.example.CryptoAppAndroid.ui.SectionsPagerAdapter
import com.example.CryptoAppAndroid.databinding.ActivityMainBinding
import com.example.CryptoAppAndroid.ui.PageViewModel
import com.example.CryptoAppAndroid.worker.WorkManagerScheduler
import io.ktor.util.reflect.*

class MainActivity : AppCompatActivity() {

    private lateinit var pageViewModel: PageViewModel
    private lateinit var binding: ActivityMainBinding

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pageViewModel = ViewModelProvider(this).get(PageViewModel::class.java)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)
        val fab: FloatingActionButton = binding.fab

        fab.setOnClickListener { view ->

            if (pageViewModel.syncDataWorkInfoItems.value != null
                && pageViewModel.syncDataWorkInfoItems.value!!.isNotEmpty()) {
                when (pageViewModel.syncDataWorkInfoItems.value?.get(0)?.state?.isFinished) {
                    true -> WorkManagerScheduler.refreshPeriodicDataWork(this)
                    false -> WorkManagerScheduler.cancelWorkerByTag(this, "syncDataWorker")
                }
            } else {
                WorkManagerScheduler.refreshPeriodicDataWork(this)
            }

            if (pageViewModel.syncTradesWorkInfoItems.value != null
                && pageViewModel.syncTradesWorkInfoItems.value!!.isNotEmpty()) {
                when (pageViewModel.syncTradesWorkInfoItems.value?.get(0)?.state?.isFinished) {
                    true -> WorkManagerScheduler.refreshPeriodicTradesWork(this, pageViewModel.pickedDateTime)
                    false -> WorkManagerScheduler.cancelWorkerByTag(this, "syncTradesWorker")
                }
            } else {
                WorkManagerScheduler.refreshPeriodicTradesWork(this, pageViewModel.pickedDateTime)
            }

            checkBattery(this@MainActivity)
        }

        pageViewModel.syncDataWorkInfoItems.observe(this, dataWorkInfosObserver())
        pageViewModel.syncTradesWorkInfoItems.observe(this, tradesWorkInfosObserver())
    }

    private fun isBatteryOptimized(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val name = context.packageName
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return !powerManager.isIgnoringBatteryOptimizations(name)
        }
        return false
    }

    private fun checkBattery(context: Context) {
        if (isBatteryOptimized(context) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            val name = context.resources.getString(R.string.app_name)
            Toast.makeText(context, "Battery optimization -> All apps -> $name -> Don't optimize", Toast.LENGTH_LONG).show()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                context.startActivity(intent)
            }

        }
    }

    private fun dataWorkInfosObserver(): Observer<List<WorkInfo>> {
        return Observer { listOfWorkInfo ->
            Log.d("Stonksdebug", "dataworkinfo observed $listOfWorkInfo")

            // Note that these next few lines grab a single WorkInfo if it exists
            // This code could be in a Transformation in the ViewModel; they are included here
            // so that the entire process of displaying a WorkInfo is in one location.

            // If there are no matching work info, do nothing
            if (listOfWorkInfo.isNullOrEmpty()) {
                return@Observer
            }

            // We only care about the one output status.
            // Every continuation has only one worker tagged TAG_OUTPUT
            val workInfo = listOfWorkInfo[0]

            if (workInfo.state.isFinished) {
                showDataWorkFinished()
            } else {
                showDataWorkInProgress()
            }
        }
    }

    private fun tradesWorkInfosObserver(): Observer<List<WorkInfo>> {
        return Observer { listOfWorkInfo ->
            Log.d("Stonksdebug", "tradesworkinfo observed $listOfWorkInfo")

            // Note that these next few lines grab a single WorkInfo if it exists
            // This code could be in a Transformation in the ViewModel; they are included here
            // so that the entire process of displaying a WorkInfo is in one location.

            // If there are no matching work info, do nothing
            if (listOfWorkInfo.isNullOrEmpty()) {
                Log.d("Stonksdebug", "tradesworkinfo is empty $listOfWorkInfo")
                return@Observer
            }

            // We only care about the one output status.
            // Every continuation has only one worker tagged TAG_OUTPUT
            val workInfo = listOfWorkInfo[0]

            if (workInfo.state.isFinished) {
                showTradesWorkFinished()
            } else {
                showTradesWorkInProgress()
            }
        }
    }

    private fun showDataWorkInProgress() {
        with(binding) {
            fab.imageTintList = ColorStateList.valueOf(Color.RED)
        }
    }

    private fun showDataWorkFinished() {
        with(binding) {
            fab.imageTintList = ColorStateList.valueOf(Color.LTGRAY)
        }
    }

    private fun showTradesWorkInProgress() {
        with(binding) {
            fab.imageTintList = ColorStateList.valueOf(Color.RED)
        }
    }

    private fun showTradesWorkFinished() {
        with(binding) {
            fab.imageTintList = ColorStateList.valueOf(Color.DKGRAY)
        }
    }
}