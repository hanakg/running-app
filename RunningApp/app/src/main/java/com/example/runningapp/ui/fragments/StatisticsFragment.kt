package com.example.runningapp.ui.fragments

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.runningapp.R
import com.example.runningapp.other.TrackingUtility
import com.example.runningapp.ui.viewmodels.StatisticsViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.fragment_statistics.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import kotlin.math.round


data class SumDistanceMonth(
    val month: String,
    val distance: Int
)
data class SumDistanceMonthInt(
    val month: Int,
    val distance: Int
)


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
                tvRunTime.text=totalTimeRun
            }
        })

        viewModel.totalDistance.observe(viewLifecycleOwner, Observer {
            it?.let {
                val km=it/1000f
                val totalDistance= round(km*10f)/10f
                val totalDistanceString="${totalDistance}km"
                tvRunDistance.text=totalDistanceString
            }
        })

        viewModel.totalAvgSpeed.observe(viewLifecycleOwner, Observer {
            it?.let {
                val avgSpeed= round(it*10f)/10f
                val avgSpeedString="${avgSpeed}km/h"
                tvRunAvgSpeed.text=avgSpeedString
            }
        })

        viewModel.totalBurnedCalories.observe(viewLifecycleOwner, Observer {
            it?.let {
                val totalBurnedCalories="${it}kcal"
                tvBurnedCalories.text=totalBurnedCalories
            }
        })

        viewModel.totalMaxSpeed.observe(viewLifecycleOwner, Observer {
            it?.let {
                val maxSpeed="${it}km/h"
                tvRunMaxSpeed.text=maxSpeed
            }
        })

        viewModel.totalMinSpeed.observe(viewLifecycleOwner, Observer {
            it?.let {
                val minSpeed="${it}km/h"
                tvRunMinSpeed.text=minSpeed
            }
        })

        viewModel.lastRunTimeStamp.observe(viewLifecycleOwner, Observer {
            it?.let {
                val lastTimeStamp=it
                if(lastTimeStamp!=null) {
                    tvLastRun.append(lastTimeStamp.toString())
                }
                else
                {
                    tvLastRun.text="Még nem történt rögzítés."
                }
            }
        })

        viewModel.distanceSumByMonth.observe(viewLifecycleOwner, Observer {
            it?.let {
                val actaulMonth=it
                Timber.d("distancehonap: ${actaulMonth}")

                if (actaulMonth.month!=0 && actaulMonth!=null) {
                    var distanceStrint=(actaulMonth.distance).toString() +" m";
                    if (actaulMonth.distance>1000) {
                        distanceStrint = (actaulMonth.distance / 1000.0).toString() + " km"
                    }
                    tvDistanceSumByMonth.append(
                        "(${
                            context?.getResources()!!
                                .getStringArray(R.array.month_names)[actaulMonth.month - 1]
                        }):\n\t ${distanceStrint}"
                    )
                }
                else
                {
                    tvDistanceSumByMonth.text=("Az aktuális hónapban még nem történt rögzítés")
                }
            }
        })

        viewModel.runsCountByYear.observe(viewLifecycleOwner, Observer {
            it?.let {
                val countList=it

                /*
                Timber.d("${countList[0].year}")
                for (i in 0 until countList.size){
                    listRunsCountByYear.append("\n\t${countList[i].year} : ${countList[i].count}")
                }
                */

                if(countList.isEmpty()===false) {
                    val allYearCount = countList.indices.map { i ->
                        BarEntry(
                            countList[i].year.toFloat(),
                            (countList[i].count).toFloat()
                        )
                    }
                    val DataSetBarChart =
                        BarDataSet(allYearCount, "Futások száma az adott évben").apply {
                            valueTextColor = Color.WHITE
                            valueTextSize = 10F
                            valueFormatter = DefaultValueFormatter(0)

                            color = ContextCompat.getColor(requireContext(), R.color.purple_700)
                        }
                    barChartRunsCountByYear.data = BarData(DataSetBarChart)
                    //barChartRunsSumByYear.marker = CustomMarkerView(sumList.reversed(), requireContext(), R.layout.marker_view)
                    barChartRunsCountByYear.invalidate()

                    barChartRunsCountByYear.setDrawMarkers(false)

                    barChartRunsCountByYear.description.text = "Év"
                    barChartRunsCountByYear.description.textSize = 11F
                    barChartRunsCountByYear.description.textColor = Color.WHITE


                    //barChartRunsCountByYear.xAxis.valueFormatter=IndexAxisValueFormatter(yearLit)

                    barChartRunsCountByYear.xAxis.apply {
                        //valueFormatter=IndexAxisValueFormatter(yearLit)
                        position = XAxis.XAxisPosition.BOTTOM
                        setDrawAxisLine(true)
                        setDrawGridLines(false)
                        setDrawLabels(true)
                        labelCount = countList.size
                        valueFormatter = DefaultValueFormatter(0)
                        spaceMax = 0.5f
                        spaceMin = 0.5f
                        axisLineColor = Color.WHITE
                        textColor = Color.WHITE

                        /*valueFormatter=object :ValueFormatter(){
                        override fun getFormattedValue(value: Float): String {
                            return countList[value.toInt()].year.toString()
                        }
                    }*/
                    }

                    barChartRunsCountByYear.axisLeft.apply {
                        axisLineColor = Color.WHITE
                        zeroLineColor = Color.WHITE
                        textColor = Color.WHITE
                        axisMinimum = 0.0f
                        labelCount = countList.maxOf { x -> x.count }

                        setDrawAxisLine(true)
                        setDrawGridLines(true)


                    }



                    barChartRunsCountByYear.axisRight.apply {
                        setDrawZeroLine(false)
                        setDrawAxisLine(false)
                        setDrawGridLines(false)
                        setDrawLabels(false)
                    }

                    barChartRunsCountByYear.description.textColor = Color.WHITE

                    barChartRunsCountByYear.isClickable = false
                    barChartRunsCountByYear.legend.apply {
                        textColor = Color.WHITE
                        textSize = 13F
                        formSize = 13F
                    }
                }

//barChartRunsCountByYear.isHighlightPerTapEnabled=false
            }
        })

        viewModel.distanceSumByYear.observe(viewLifecycleOwner, Observer {
            it?.let {
                val sumList=it
                /*
                Timber.d("${sumList[0].year}")
                for (i in 0 until sumList.size){
                    listDistanceSumByYear.append("\n\t${sumList[i].year}")
                    if (sumList[i].distance>1000)
                    {
                        listDistanceSumByYear.append(": ${sumList[i].distance/1000.0} km")
                    }
                    else
                    {
                        listDistanceSumByYear.append(": ${sumList[i].distance} m")
                    }
                }*/

                if(sumList.isEmpty()===false) {
                    val allYearSum = sumList.indices.map { i ->
                        BarEntry(
                            sumList[i].year.toFloat(),
                            (sumList[i].distance / 1000.0).toFloat()
                        )
                    }
                    val DataSetBarChart =
                        BarDataSet(allYearSum, "Adott évben megtett távolság").apply {
                            valueTextColor = Color.WHITE
                            valueTextSize = 10F
                            color = ContextCompat.getColor(requireContext(), R.color.purple_700)
                        }
                    barChartRunsSumByYear.data = BarData(DataSetBarChart)
                    //barChartRunsSumByYear.marker = CustomMarkerView(sumList.reversed(), requireContext(), R.layout.marker_view)
                    barChartRunsSumByYear.invalidate()

                    barChartRunsSumByYear.setDrawMarkers(false)

                    barChartRunsSumByYear.description.text = "Év"
                    barChartRunsSumByYear.description.textSize = 11F
                    barChartRunsSumByYear.description.textColor = Color.WHITE

                    barChartRunsSumByYear.xAxis.apply {
                        position = XAxis.XAxisPosition.BOTTOM
                        setDrawGridLines(false)
                        setDrawAxisLine(true)
                        setDrawLabels(true)
                        labelCount = sumList.size
                        valueFormatter = DefaultValueFormatter(0)
                        spaceMax = 0.5f
                        spaceMin = 0.5f
                        axisLineColor = Color.WHITE
                        textColor = Color.WHITE

                        /*valueFormatter=object :ValueFormatter(){
                        override fun getFormattedValue(value: Float): String {
                            return countList[value.toInt()].year.toString()
                        }
                    }*/
                    }

                    barChartRunsSumByYear.axisLeft.apply {
                        axisLineColor = Color.WHITE
                        textColor = Color.WHITE
                        axisMinimum = 0.0f
                        //labelCount=sumList.maxOf { x->x.distance }
                        setDrawAxisLine(true)
                        setDrawGridLines(true)
                    }



                    barChartRunsSumByYear.axisRight.apply {
                        setDrawZeroLine(false)
                        setDrawAxisLine(false)
                        setDrawGridLines(false)
                        setDrawLabels(false)
                    }

                    barChartRunsSumByYear.description.textColor = Color.WHITE

                    barChartRunsSumByYear.isClickable = false
                    barChartRunsSumByYear.legend.apply {
                        textColor = Color.WHITE
                        textSize = 13F
                        formSize = 13F
                    }
                }
            }
        })

        viewModel.distanceSumLastThreeMonths.observe(viewLifecycleOwner, Observer {
            it?.let {
                val monthsDistList=it

                Timber.d("Teszt:...${monthsDistList}")

                val time = Calendar.getInstance().time
                val formatter=SimpleDateFormat("yyyy-MM-dd")
                val current=formatter.format(time)

                val referenceDate = Date()
                val c = Calendar.getInstance()
                c.time = referenceDate
                c.add(Calendar.MONTH, -3)
                val oldDate=formatter.format(c.time)

                var monthsDistanceList:MutableList<SumDistanceMonth> = arrayListOf()
                var monthsDistanceListInt:MutableList<SumDistanceMonthInt> = arrayListOf()

                Timber.d("OldDate: ${oldDate}")

                Timber.d("ActalMonth: ${current}")

                if(monthsDistList.isEmpty()===true)
                {
                    Timber.d("If ag fut")
                    val old = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        LocalDate.parse(oldDate)
                    } else {
                        TODO("VERSION.SDK_INT < O")
                    }

                    val new=LocalDate.parse(current)

                    var monthVar=old.monthValue
                    var monthsListLastThree:MutableList<Int> = arrayListOf()
                    var whileIteration=0

                    while(monthVar!=new.monthValue)
                    {
                        monthsListLastThree.add(whileIteration, monthVar)
                        whileIteration+=1

                        monthVar+=1

                        if(monthVar > 12){
                            monthVar=1
                        }
                    }
                    monthsListLastThree.add(whileIteration, monthVar) // A new.monthValue-nak is be kell kerülnie, ez azért kell

                    monthsListLastThree


                    var j=0

                    for (i in 0 until monthsListLastThree.size) {
                        monthsDistanceList.add(j, SumDistanceMonth(context?.getResources()!!.getStringArray(R.array.month_names)[(monthsListLastThree[i]-1)], 0))
                        monthsDistanceListInt.add(j, SumDistanceMonthInt(monthsListLastThree[i], 0))
                        j++
                    }
                   Timber.d("Regi honap${old.monthValue}")
                }
                else
                {
                    Timber.d("Else ag fut")
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    {
                        val old=LocalDate.parse(monthsDistList[0].olddate)
                        val new=LocalDate.parse(monthsDistList[0].newdate)

                        var monthVar=old.monthValue
                        var monthsListLastThree:MutableList<Int> = arrayListOf()
                        var whileIteration=0

                        while(monthVar!=new.monthValue)
                        {
                            monthsListLastThree.add(whileIteration, monthVar)
                            whileIteration+=1

                            monthVar+=1

                            if(monthVar > 12){
                                monthVar=1
                            }
                        }
                        monthsListLastThree.add(whileIteration, monthVar) // A new.monthValue-nak is be kell kerülnie, ez azért kell

                        monthsListLastThree

                        Timber.d("${monthsListLastThree.size}")


                        var j=0
                        var listIndex=0
                        for (i in 0 until monthsListLastThree.size) {
                            if(j<monthsDistList.size && monthsDistList[j].month===monthsListLastThree[i])
                            {
                                monthsDistanceList.add(listIndex, SumDistanceMonth(context?.getResources()!!.getStringArray(R.array.month_names)[(monthsListLastThree[i]-1)], monthsDistList[j].distance))
                                monthsDistanceListInt.add(listIndex, SumDistanceMonthInt(monthsListLastThree[i], monthsDistList[j].distance))

                                listIndex++
                                j++
                            }
                            else
                            {
                                monthsDistanceList.add(listIndex, SumDistanceMonth(context?.getResources()!!.getStringArray(R.array.month_names)[(monthsListLastThree[i]-1)], 0))
                                monthsDistanceListInt.add(listIndex, SumDistanceMonthInt(monthsListLastThree[i], 0))
                                listIndex++
                            }
                        }

                        //val allAvgSpeeds = it.indices.map { i -> LineEn(i.toFloat(), it[i].avgSpeedInKMH) }


                        Timber.d("${old}-tól ${new}-ig")
                    }
                }



                val threeMonthsDistance = monthsDistanceListInt.indices.map { i ->
                    BarEntry(
                        monthsDistanceListInt[i].month.toFloat(),
                        (monthsDistanceListInt[i].distance / 1000.0).toFloat()
                    )
                }

                val DataSetLineChart=LineDataSet(threeMonthsDistance, "Adott hónapban megtett távolság").apply {
                    valueTextColor = Color.WHITE
                    valueTextSize = 10F
                    color = ContextCompat.getColor(requireContext(), R.color.purple_700)
                }

                lineChartLastThreeMonths.data = LineData(DataSetLineChart)
                //barChartRunsSumByYear.marker = CustomMarkerView(sumList.reversed(), requireContext(), R.layout.marker_view)
                lineChartLastThreeMonths.invalidate()
                lineChartLastThreeMonths.setDrawMarkers(false)

                lineChartLastThreeMonths.description.text="Hónap"
                lineChartLastThreeMonths.description.textSize=11F
                lineChartLastThreeMonths.description.textColor = Color.WHITE

                lineChartLastThreeMonths.xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    setDrawAxisLine(true)
                    setDrawLabels(true)
                    labelCount = monthsDistanceList.size
                    valueFormatter = DefaultValueFormatter(0)
                    spaceMax = 0.5f
                    spaceMin = 0.5f
                    axisLineColor = Color.WHITE
                    textColor = Color.WHITE

                    lineChartLastThreeMonths.axisLeft.apply {
                        axisLineColor = Color.WHITE
                        textColor = Color.WHITE
                        axisMinimum = 0.0f
                        setDrawAxisLine(true)
                        setDrawGridLines(true)
                    }



                    lineChartLastThreeMonths.axisRight.apply {
                        setDrawZeroLine(false)
                        setDrawAxisLine(false)
                        setDrawGridLines(false)
                        setDrawLabels(false)
                    }

                    lineChartLastThreeMonths.isClickable = false
                    lineChartLastThreeMonths.legend.apply {
                        textColor = Color.WHITE
                        textSize = 13F
                        formSize = 13F
                    }
                }

                /*
                *
                    }



                    barChartRunsSumByYear.description.textColor = Color.WHITE

                    barChartRunsSumByYear.isClickable = false
                    barChartRunsSumByYear.legend.apply {
                        textColor = Color.WHITE
                        textSize = 13F
                        formSize = 13F
                * */



                //Timber.d("${monthsDistanceList}")


                /*var monthsDistanceList:MutableList<SumDistanceMonth> = arrayListOf()

                var j=0
                for (i in 0 until 3){
                    if (monthsDistList.isEmpty()==false && monthsDistList.size<=j && monthsDistList[j].month==actualMonth-(2-i))
                    {
                        monthsDistanceList. add(i,SumDistanceMonth(context?.getResources()!!
                            .getStringArray(R.array.month_names)[monthsDistList[j].month - 1],monthsDistList[j].distance))
                        j++
                    }
                    else {
                        monthsDistanceList.add(i, SumDistanceMonth(context?.getResources()!!.getStringArray(R.array.month_names)[(actualMonth-(2-i) - 1)], 0))
                    }
                }

                Timber.d("${monthsDistanceList}")*/
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