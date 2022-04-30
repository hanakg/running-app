package com.example.runningapp.ui.fragments

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.runningapp.R
import com.example.runningapp.adapters.RunAdapter
import com.example.runningapp.db.Run
import com.example.runningapp.other.Constants.REQUEST_CODE_LOCATION_PERMISSION
import com.example.runningapp.other.SortType
import com.example.runningapp.other.TrackingUtility
import com.example.runningapp.ui.viewmodels.MainViewModel
import com.example.runningapp.ui.viewmodels.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_run.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.text.FieldPosition

const val DELETE_RUN_DIALOG="DeleteDialog"

@AndroidEntryPoint
class RunFragment:Fragment(R.layout.fragment_run), EasyPermissions.PermissionCallbacks {
    private val viewModel:MainViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private lateinit var runAdapter:RunAdapter
    private lateinit var listRun:List<Run>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestPermission() //Engedélykérés meghívása
        setupRecyclerView()

        when(viewModel.sortType){
            SortType.DATE->spFilter.setSelection(0)
            SortType.RUNNING_TIME->spFilter.setSelection(1)
            SortType.DISTANCE->spFilter.setSelection(2)
            SortType.AVG_SPEED->spFilter.setSelection(3)
            SortType.BURNED_CALORIES->spFilter.setSelection(4)
        }

        spFilter.onItemSelectedListener=object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when(position){
                    0->viewModel.sortRuns(SortType.DATE)
                    1->viewModel.sortRuns(SortType.RUNNING_TIME)
                    2->viewModel.sortRuns(SortType.DISTANCE)
                    3->viewModel.sortRuns(SortType.AVG_SPEED)
                    4->viewModel.sortRuns(SortType.BURNED_CALORIES)
                }
            }
        }

        viewModel.runs.observe(viewLifecycleOwner, Observer {
            runAdapter.submitList(it)
            listRun=it
        })

        //Ha a Start gombra kattintunk, akkor átugrik a tracking fragment-re
        starttrack.setOnClickListener {
            findNavController().navigate(R.id.action_runFragment_to_trackingFragment,)
        }
    }

    private fun setupRecyclerView()=rvRuns.apply {
        runAdapter= RunAdapter()
        adapter=runAdapter

        layoutManager=LinearLayoutManager(requireContext())

        runAdapter.setOnClickListener(object : RunAdapter.onItemClickListener{
            override fun onItemClick(position: Int) {
                sharedViewModel.setPosition(position)
                sharedViewModel.setSelectedRun(listRun[position])
                findNavController().navigate(R.id.action_runFragment_to_oneRunStatisticsFragment)
            }
        })
        runAdapter.setOnLongClickListener(object : RunAdapter.onItemLongClickListener{
            override fun onItemLongClick(position: Int) {
                val deleteRunDialog=parentFragmentManager.findFragmentByTag(
                    DELETE_RUN_DIALOG)as DeleteRunDialog?
                deleteRunDialog?.setYesListener {
                    viewModel.deleteRun(listRun[position])
                }

                DeleteRunDialog().apply {
                    setYesListener {
                        viewModel.deleteRun(listRun[position])
                    }
                }.show(parentFragmentManager, DELETE_RUN_DIALOG)
            }
        })
    }

    //Engedély kérése a helymeghatározásra. Ha már adott engedélyt rá a felhasználó, akkor egyszerűen tovább lép
    private fun requestPermission(){
        if (TrackingUtility.hasLocationPermission(requireContext())){
            return
        }
        if (Build.VERSION.SDK_INT<Build.VERSION_CODES.Q){
            EasyPermissions.requestPermissions(
                this,
                "Engedélyezned kell a tartózkodási hely meghatározását az alkalmazás további használatához.",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }else{
            EasyPermissions.requestPermissions(
                this,
                "Engedélyezned kell a tartózkodási hely meghatározását az alkalmazás további használatához.",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }

    //Mi történjen, ha a felhasználó elutasítja az engedélykérést. Mutatja majd, hogy az alkalmazás nem fog rendesen működni az engedély megadása nélkül
    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)){
            AppSettingsDialog.Builder(this).build().show()
        }else{
            requestPermission()
        }
    }

    //Ha engedélyezte
    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
    }

    //Engedélyek kezelése->EasyPermission meghívása
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
}