package com.example.runningapp.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.runningapp.R
import com.example.runningapp.other.Constants
import com.example.runningapp.ui.viewmodels.SharedViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_setgoal.*
import kotlinx.android.synthetic.main.fragment_setup.*
import timber.log.Timber

@AndroidEntryPoint
class SetGoalFragment: Fragment(R.layout.fragment_setgoal){

    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnSaveDistance.setOnClickListener {
            val success=writeDistanceGoalToSharedViewModel()
            if (success){
                findNavController().navigate(R.id.action_setGoalFragment_to_trackingFragment)
            }
            else{
                Snackbar.make(requireView(), "Hiba a mentéskor, nézd meg, hogy minden mezőt helyesen töltöttél-e ki!", Snackbar.LENGTH_SHORT).show()
            }
        }

        btnSaveTime.setOnClickListener {
            val success=writeTimeGoalToSharedViewModel()
            if (success){
                findNavController().navigate(R.id.action_setGoalFragment_to_trackingFragment)
            }
            else{
                Snackbar.make(requireView(), "Hiba a mentéskor, nézd meg, hogy minden mezőt helyesen töltöttél-e ki!", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun writeTimeGoalToSharedViewModel():Boolean{
        val hours=etHour.text.toString()
        val minutes=etMinute.text.toString()

        if (hours.isEmpty() || minutes.isEmpty() || minutes.toInt()>=60 || hours.toInt()>24 || minutes.toInt()<0 || hours.toInt()<0 || (hours.toInt()==0 && minutes.toInt()==0)){
            return false
        }

        //Milliszekundumá konvertálás

        val hourMillisec=hours.toLong()*3600000L
        val minutesMillisec=minutes.toLong()*60000L

        val millisec=hourMillisec+minutesMillisec

        sharedViewModel.setGoalTime(millisec)

        return true
    }

    private fun writeDistanceGoalToSharedViewModel():Boolean{
        val distance=etDistance.text.toString()

        if (distance.isEmpty() || distance.toInt()>100000 || distance.toInt()<=0){
            return false
        }

        sharedViewModel.setGoalDistance(distance.toInt())

        return true
    }

}