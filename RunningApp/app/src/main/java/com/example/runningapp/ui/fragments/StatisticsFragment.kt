package com.example.runningapp.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.runningapp.R
import com.example.runningapp.other.CustomMarkerView
import com.example.runningapp.other.TrackingUtility
import com.example.runningapp.ui.viewmodels.MainViewModel
import com.example.runningapp.ui.viewmodels.StatisticsViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_onerunstatistics.*
import kotlinx.android.synthetic.main.fragment_statistics.*
import timber.log.Timber
import kotlin.math.round

@AndroidEntryPoint
class StatisticsFragment:Fragment(R.layout.fragment_statistics) {
    private val viewModel: StatisticsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeToObservers()
    }



    private fun subscribeToObservers(){
        viewModel.totalTimeRun.observe(viewLifecycleOwner, Observer {
            it?.let {
                val totalTimeRun=TrackingUtility.getFormattedStopWatchTime(it)
                tvTotalTime.text=totalTimeRun
            }
        })

        viewModel.totalDistance.observe(viewLifecycleOwner, Observer {
            it?.let {
                val km=it/1000f
                val totalDistance= round(km*10f)/10f
                val totalDistanceString="${totalDistance}km"
                tvTotalDistance.text=totalDistanceString
            }
        })

        viewModel.totalAvgSpeed.observe(viewLifecycleOwner, Observer {
            it?.let {
                val avgSpeed= round(it*10f)/10f
                val avgSpeedString="${avgSpeed}km/h"
                tvAverageSpeed.text=avgSpeedString
            }
        })

        viewModel.totalBurnedCalories.observe(viewLifecycleOwner, Observer {
            it?.let {
                val totalBurnedCalories="${it}kcal"
                tvTotalCalories.text=totalBurnedCalories
            }
        })

        viewModel.totalMaxSpeed.observe(viewLifecycleOwner, Observer {
            it?.let {
                val maxSpeed="${it}km/h"
                tvTotalMaxSpeed.text=maxSpeed
            }
        })

        viewModel.totalMinSpeed.observe(viewLifecycleOwner, Observer {
            it?.let {
                val minSpeed="${it}km/h"
                tvTotalMinSpeed.text=minSpeed
            }
        })

        viewModel.runsCountByYear.observe(viewLifecycleOwner, Observer {
            it?.let {
                val count=it
                Timber.d("${count}")
                //tvTotalMinSpeed.text=minSpeed
            }
        })

        viewModel.getTimeStampt.observe(viewLifecycleOwner, Observer {
            it?.let {
                val count=it
                Timber.d("${count}")
                //tvTotalMinSpeed.text=minSpeed
            }
        })

    }
}