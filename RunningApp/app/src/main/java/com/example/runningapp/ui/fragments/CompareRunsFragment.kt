package com.example.runningapp.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.runningapp.R
import com.example.runningapp.other.TrackingUtility
import com.example.runningapp.ui.viewmodels.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_compareruns.*
import kotlinx.android.synthetic.main.fragment_onerunstatistics.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class CompareRunsFragment: Fragment(R.layout.fragment_compareruns) {
    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//-------------------------------------------------------------------------

        val oldRun=sharedViewModel.selectedRun.value!!
        val newRun=sharedViewModel.newRun.value!!

        val calendarOld= Calendar.getInstance().apply {
            timeInMillis=oldRun.timestamp
        }
        val calendarNew= Calendar.getInstance().apply {
            timeInMillis=newRun.timestamp
        }

        val dateFormat= SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        tvOldRunDate.text=dateFormat.format(calendarOld.time)
        tvNewRunDate.text=dateFormat.format(calendarNew.time)

//--------------------------------------------------------------------

        val oldDistance=oldRun.distance
        val newDistance=newRun.distance

        tvOldDistance.text = oldDistance.toString() + "m"
        tvNewDistance.text = newDistance.toString() + "m"
        Timber.d("${oldDistance.toString()}")
        if(oldDistance>1000) {
            tvOldDistance.text = (oldDistance/1000f).toString() + "km"
        }
        if(newDistance>1000) {
            tvNewDistance.text = (newDistance/1000f).toString() + "km"
        }

        if(oldDistance>newDistance) {
            tvOldDistance.setTextColor(Color.GREEN)
            tvNewDistance.setTextColor(Color.RED)

            tvOldDistanceInfo.setTextColor(Color.RED)
            tvNewDistanceInfo.setTextColor(Color.RED)

            tvOldDistanceInfo.text="Rontottál"
            tvNewDistanceInfo.text="${if(oldDistance-newDistance>1000) ((oldDistance-newDistance)/1000f).toString()+"km-el"  else (oldDistance-newDistance).toString()+"m-el"} kevesebb, mint a régi"
        }
        else if(oldDistance<newDistance){
            tvOldDistance.setTextColor(Color.RED)
            tvNewDistance.setTextColor(Color.GREEN)

            tvOldDistanceInfo.setTextColor(Color.GREEN)
            tvNewDistanceInfo.setTextColor(Color.GREEN)

            tvOldDistanceInfo.text="Javítottál"
            tvNewDistanceInfo.text="${if(newDistance-oldDistance>1000) ((newDistance-oldDistance)/1000f).toString()+"km-el"  else (newDistance-oldDistance).toString()+"m-el"} több mint a régi"
        }
        else{
            tvOldDistance.setTextColor(Color.YELLOW)
            tvNewDistance.setTextColor(Color.YELLOW)

            tvOldDistanceInfo.setTextColor(Color.YELLOW)
            tvNewDistanceInfo.setTextColor(Color.YELLOW)

            tvOldDistanceInfo.text="Megegyezik az új távolsággal"
            tvNewDistanceInfo.text="Megegyezik a régi távolsággal"
        }

//--------------------------------------------------------------------------

        val oldTime=oldRun.timeMillisec
        val newTime=newRun.timeMillisec

        tvOldTime.text= TrackingUtility.getFormattedStopWatchTime(oldTime)
        tvNewTime.text= TrackingUtility.getFormattedStopWatchTime(newTime)

        if(oldTime>newTime){
            tvOldTime.setTextColor(Color.GREEN)
            tvNewTime.setTextColor(Color.RED)

            tvOldTimeInfo.setTextColor(Color.RED)
            tvNewTimeInfo.setTextColor(Color.RED)

            tvOldTimeInfo.text="Kevesebb"
            tvNewTimeInfo.text=TrackingUtility.getFormattedStopWatchTime(oldTime-newTime)+"-el kevesebb"
        }
        else if(oldTime<newTime){
            tvOldTime.setTextColor(Color.RED)
            tvNewTime.setTextColor(Color.GREEN)

            tvOldTimeInfo.setTextColor(Color.GREEN)
            tvNewTimeInfo.setTextColor(Color.GREEN)

            tvOldTimeInfo.text="Több"
            tvNewTimeInfo.text="Ennyivel több: "+TrackingUtility.getFormattedStopWatchTime(newTime-oldTime)
        }
        else{
            tvOldTime.setTextColor(Color.YELLOW)
            tvNewTime.setTextColor(Color.YELLOW)

            tvOldTimeInfo.setTextColor(Color.YELLOW)
            tvNewTimeInfo.setTextColor(Color.YELLOW)

            tvOldTimeInfo.text="Megegyezik"
            tvNewTimeInfo.text="Megegyezik"
        }

//---------------------------------------------------------------------------

        val oldAvgSpeed=oldRun.avgSpeed
        val newAvgSpeed=newRun.avgSpeed

        tvOldAvgSpeed.text=oldAvgSpeed.toString()+"km/h"
        tvNewAvgSpeed.text=newAvgSpeed.toString()+"km/h"

        if(oldAvgSpeed>newAvgSpeed) {
            tvOldAvgSpeed.setTextColor(Color.GREEN)
            tvNewAvgSpeed.setTextColor(Color.RED)

            tvOldAvgSpeedInfo.setTextColor(Color.RED)
            tvNewAvgSpeedInfo.setTextColor(Color.RED)

            tvOldAvgSpeedInfo.text="Rontottál"
            tvNewAvgSpeedInfo.text="${(oldAvgSpeed-newAvgSpeed).toString()+"km/h-val"} lassabb"
        }
        else if(oldAvgSpeed<newAvgSpeed){
            tvOldAvgSpeed.setTextColor(Color.RED)
            tvNewAvgSpeed.setTextColor(Color.GREEN)

            tvOldAvgSpeedInfo.setTextColor(Color.GREEN)
            tvNewAvgSpeedInfo.setTextColor(Color.GREEN)

            tvOldAvgSpeedInfo.text="Javítottál"
            tvNewAvgSpeedInfo.text="${(newAvgSpeed-oldAvgSpeed).toString()+"km/h-val"} gyorsabb"
        }
        else{
            tvOldAvgSpeed.setTextColor(Color.YELLOW)
            tvNewAvgSpeed.setTextColor(Color.YELLOW)

            tvOldAvgSpeedInfo.setTextColor(Color.YELLOW)
            tvNewAvgSpeedInfo.setTextColor(Color.YELLOW)

            tvOldAvgSpeedInfo.text="Megegyezik"
            tvNewAvgSpeedInfo.text="Megegyezik"
        }

//--------------------------------------------------------------------------------------

        val oldMaxSpeed=oldRun.maxSpeed
        val newMaxSpeed=newRun.maxSpeed

        tvOldMaxSpeed.text=oldMaxSpeed.toString()+"km/h"
        tvNewMaxSpeed.text=newMaxSpeed.toString()+"km/h"

        if(oldMaxSpeed>newAvgSpeed) {
            tvOldMaxSpeed.setTextColor(Color.GREEN)
            tvNewMaxSpeed.setTextColor(Color.RED)

            tvOldMaxSpeedInfo.setTextColor(Color.RED)
            tvNewMaxSpeedInfo.setTextColor(Color.RED)

            tvOldMaxSpeedInfo.text="Rontottál"
            tvNewMaxSpeedInfo.text="${(oldMaxSpeed-newMaxSpeed).toString()+"km/h-val"} lassabb"
        }
        else if(oldMaxSpeed<newAvgSpeed){
            tvOldMaxSpeed.setTextColor(Color.RED)
            tvNewMaxSpeed.setTextColor(Color.GREEN)

            tvOldMaxSpeedInfo.setTextColor(Color.GREEN)
            tvNewMaxSpeedInfo.setTextColor(Color.GREEN)

            tvOldMaxSpeedInfo.text="Javítottál"
            tvNewMaxSpeedInfo.text="${(newMaxSpeed-oldMaxSpeed).toString()+"km/h-val"} gyorsabb"
        }
        else{
            tvOldAvgSpeed.setTextColor(Color.YELLOW)
            tvNewMaxSpeed.setTextColor(Color.YELLOW)

            tvOldMaxSpeedInfo.setTextColor(Color.YELLOW)
            tvNewMaxSpeedInfo.setTextColor(Color.YELLOW)

            tvOldMaxSpeedInfo.text="Megegyezik"
            tvNewMaxSpeedInfo.text="Megegyezik"
        }

        val oldBurnedCalories=oldRun.burnedCalories
        val newBurnedCalories=newRun.burnedCalories

        tvOldBurnedCalories.text=oldBurnedCalories.toString()+"kcal"
        tvNewBurnedCalories.text=newBurnedCalories.toString()+"kcal"

        if(oldBurnedCalories>newBurnedCalories) {
            tvOldBurnedCalories.setTextColor(Color.GREEN)
            tvNewBurnedCalories.setTextColor(Color.RED)

            tvOldBurnedCaloriesInfo.setTextColor(Color.RED)
            tvNewBurnedCaloriesInfo.setTextColor(Color.RED)

            tvOldBurnedCaloriesInfo.text="Kevesebb kalóriát égettél"
            tvNewBurnedCaloriesInfo.text="${(oldBurnedCalories-newBurnedCalories).toString()} kalóriával kevesebb égettél"
        }
        else if(oldBurnedCalories<newBurnedCalories){
            tvOldBurnedCalories.setTextColor(Color.RED)
            tvNewBurnedCalories.setTextColor(Color.GREEN)

            tvOldBurnedCaloriesInfo.setTextColor(Color.GREEN)
            tvNewBurnedCaloriesInfo.setTextColor(Color.GREEN)

            tvOldBurnedCaloriesInfo.text="Több kalóriát égettél"
            tvNewBurnedCaloriesInfo.text="${(newBurnedCalories-oldBurnedCalories).toString()} kalóriával többet égettél"
        }
        else{
            tvOldBurnedCalories.setTextColor(Color.YELLOW)
            tvNewBurnedCalories.setTextColor(Color.YELLOW)

            tvOldBurnedCaloriesInfo.setTextColor(Color.YELLOW)
            tvNewBurnedCaloriesInfo.setTextColor(Color.YELLOW)

            tvOldBurnedCaloriesInfo.text="Megegyezik"
            tvNewBurnedCaloriesInfo.text="Megegyezik"
        }

//-------------------------------------------------------------------------------------------

        if((newDistance-oldDistance)>0 && (oldTime-newTime)>0){
            if (newMaxSpeed>oldMaxSpeed){
                tvSummary.text=
                    "Az új futásod alatt nagyobb távot tettél meg kevesebb idő alatt, és még a maximum sebességed is több volt.\n Gratulálok, csak így tovább!"
            }
            else{
                tvSummary.text=
                    "Az új futásod alatt nagyobb távot tettél meg kevesebb idő alatt, de a maximum sebességen van még mit javítani.\n Gratulálok, csak így tovább!"
            }
        }
        else if((newDistance-oldDistance)>0 && (newTime-oldTime)>0){
            if (newAvgSpeed>oldAvgSpeed){
                tvSummary.text=
                    "Gratulálok, javítottál a teljesítményeden, csak így tovább!"
            }
            else {
                tvSummary.text =
                    "Gratulálok, nagyobb távot tettél meg, de a sebességen, időn még lenne mit javítani!"
            }
        }
        else if ((oldDistance-newDistance)>0){
            if(newAvgSpeed>oldAvgSpeed){
                tvSummary.text=
                    "A teljesítményed javult, de sajnos a táv most kevesebb lett."
            }
            else{
                tvSummary.text=
                    "Sajnos romlott a teljesítményed."
            }
        }
        else{
            if(newAvgSpeed>oldAvgSpeed){
                tvSummary.text=
                    "A táv nem változott, de a teljesítményed javult."
            }
            else{
                tvSummary.text=
                    "A táv nem változott, és sajnos romlott egy kicsit a teljesítményed is."
            }
        }


    }

}