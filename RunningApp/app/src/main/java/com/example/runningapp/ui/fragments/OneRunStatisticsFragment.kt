package com.example.runningapp.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_onerunstatistics.*
import kotlinx.android.synthetic.main.fragment_run.*
import kotlinx.android.synthetic.main.item_run.view.*
import pub.devrel.easypermissions.EasyPermissions
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
internal class OneRunStatisticsFragment: Fragment(R.layout.fragment_onerunstatistics){
    private val viewModel:MainViewModel by viewModels()
    private val sharedViewModel:SharedViewModel by activityViewModels()



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //val run=sharedViewModel.selectedRun.value!!
        val pos=sharedViewModel.position
       viewModel.runs.observe(viewLifecycleOwner, Observer {
            val run= it[pos.value!!]

           Glide.with(this).load(run.img).into(ivRunImage)

           val calendar= Calendar.getInstance().apply {
               timeInMillis=run.timestamp
           }
           val dateFormat= SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
           tvDate.text="Dátum: "+dateFormat.format(calendar.time)

           val avgSpeed="${run.avgSpeed}km/h"
           tvAvgSpeed.text="Átlag sebesség: "+avgSpeed

           val distanceInKm="${run.distance/1000f}km"
           tvDistance.text="Távolság: "+distanceInKm

           tvTime.text= "Idő: "+TrackingUtility.getFormattedStopWatchTime(run.timeMillisec)

           val caloriesBurned="${run.burnedCalories}kcal"
           tvCalories.text="Elégetett kalóriák: "+caloriesBurned
        })
    }

}

