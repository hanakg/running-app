package com.example.runningapp.ui.fragments

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.core.content.ContextCompat
import androidx.core.view.size
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.Glide
import com.example.runningapp.R
import com.example.runningapp.adapters.RunAdapter
import com.example.runningapp.db.Run
import com.example.runningapp.other.Constants
import com.example.runningapp.other.TrackingUtility
import com.example.runningapp.ui.MainActivity
import com.example.runningapp.ui.MainActivity_GeneratedInjector
import com.example.runningapp.ui.viewmodels.MainViewModel
import com.example.runningapp.ui.viewmodels.SharedViewModel
import com.example.runningapp.ui.viewmodels.StatisticsViewModel
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_onerunstatistics.*
import kotlinx.android.synthetic.main.fragment_run.*
import kotlinx.android.synthetic.main.item_run.view.*
import pub.devrel.easypermissions.EasyPermissions
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
internal class OneRunStatisticsFragment : Fragment(R.layout.fragment_onerunstatistics) {
    private val viewModel: MainViewModel by viewModels()
    private val statisticsViewModel: StatisticsViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //val run=sharedViewModel.selectedRun.value!!
        val pos = sharedViewModel.position

        val run = sharedViewModel.selectedRun.value!!
        Glide.with(this).load(run.img).into(ivRunImage)

        val calendar = Calendar.getInstance().apply {
            timeInMillis = run.timestamp
        }
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        tvDate.text = "Dátum: " + dateFormat.format(calendar.time)

        val avgSpeed = "${run.avgSpeed}km/h"
        tvAvgSpeed.text = "Átlag sebesség: " + avgSpeed

        val distanceInKm = "${run.distance / 1000f}km"
        tvDistance.text = "Távolság: " + distanceInKm

        tvTime.text = "Idő: " + TrackingUtility.getFormattedStopWatchTime(run.timeMillisec)

        val caloriesBurned = "${run.burnedCalories}kcal"
        tvCalories.text = "Elégetett kalóriák: " + caloriesBurned

        val maxSpeed = "${run.maxSpeed}Km/h"
        tvMaxSpeed.text = "Maximum sebesség: " + maxSpeed

        val minSpeed = "${run.minSpeed}Km/h"
        tvMinSpeed.text = "Minumum sebesség: " + minSpeed


        statisticsViewModel.avgDistance.observe(viewLifecycleOwner, Observer {
            it?.let {
                var avgDistance = it


                var pieEntry2 =
                    PieEntry(run.distance / 1000F, "Adott futás alkalmával megtett távolság")
                var pieEntry1 = PieEntry(avgDistance / 1000F, "Átlagosan megtett távolság")

                var pieChartData: MutableList<PieEntry> = arrayListOf(pieEntry1, pieEntry2)

                val DataSetPieChart =
                    PieDataSet(pieChartData, "").apply {
                        valueTextColor = Color.WHITE
                        valueTextSize = 15F
                        //color = ContextCompat.getColor(requireContext(), R.color.purple_700)
                        colors = arrayListOf(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.purple_700
                            ), ContextCompat.getColor(requireContext(), R.color.purple_200)
                        )
                    }

                pieChart.data = PieData(DataSetPieChart)

                pieChart.apply {
                    setDrawEntryLabels(false)
                    setDrawRoundedSlices(true)
                    setHoleColor(Color.WHITE)
                    description.text = ""
                    extraBottomOffset=20F
                }

                pieChart.legend.apply {
                    textSize = 15F
                    verticalAlignment=Legend.LegendVerticalAlignment.BOTTOM
                    setDrawInside(false)
                    formSize=15F
                    orientation=Legend.LegendOrientation.VERTICAL
                    mNeededHeight=150F
                    yOffset=-50F
                }
                pieChart.legend.isWordWrapEnabled=true

                pieChart.invalidate()
            }
        })

        challengestarttrack.setOnClickListener {
            sharedViewModel.setChallenge(true)
            findNavController().navigate(R.id.action_oneRunStatisticsFragment_to_trackingFragment)
        }
    }

}

