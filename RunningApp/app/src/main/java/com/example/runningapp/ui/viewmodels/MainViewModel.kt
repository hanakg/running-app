package com.example.runningapp.ui.viewmodels


import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runningapp.db.Run
import com.example.runningapp.other.SortType
import com.example.runningapp.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val mainRepository: MainRepository
):ViewModel(){
    //A repo-ban lévő adatbázis műveletek meghívása
    private val runsSortedByDate=mainRepository.getAllRunsByDate()
    private val runsSortedByDistance=mainRepository.getAllRunsByDistance()
    private val runsSortedByBurnedCalories=mainRepository.getAllRunsByBurnedCalories()
    private val runsSortedByTimeInMillisec=mainRepository.getAllRunsByTimeMillisec()
    private val runsSortedByAvgSpeed=mainRepository.getAllRunsByAvgSpeed()

    val runs=MediatorLiveData<List<Run>>()

    var sortType=SortType.DATE

    init {
        runs.addSource(runsSortedByDate){ result->
            if (sortType==SortType.DATE){
                result?.let { runs.value=it }
            }
        }
        runs.addSource(runsSortedByAvgSpeed){ result->
            if (sortType==SortType.AVG_SPEED){
                result?.let { runs.value=it }
            }
        }
        runs.addSource(runsSortedByBurnedCalories){ result->
            if (sortType==SortType.BURNED_CALORIES){
                result?.let { runs.value=it }
            }
        }
        runs.addSource(runsSortedByDistance){ result->
            if (sortType==SortType.DISTANCE){
                result?.let { runs.value=it }
            }
        }
        runs.addSource(runsSortedByTimeInMillisec){ result->
            if (sortType==SortType.RUNNING_TIME){
                result?.let { runs.value=it }
            }
        }
    }

    fun sortRuns(sortType: SortType)=when(sortType){
        SortType.DATE->runsSortedByDate.value?.let { runs.value=it }
        SortType.RUNNING_TIME->runsSortedByTimeInMillisec.value?.let { runs.value=it }
        SortType.AVG_SPEED->runsSortedByAvgSpeed.value?.let { runs.value=it }
        SortType.DISTANCE->runsSortedByDistance.value?.let { runs.value=it }
        SortType.BURNED_CALORIES->runsSortedByBurnedCalories.value?.let { runs.value=it }
    }.also {
        this.sortType=sortType
    }

    fun insertRun(run: Run)=viewModelScope.launch {
        mainRepository.insertRun(run)
    }
}