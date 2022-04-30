package com.example.runningapp.ui.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.runningapp.R
import com.example.runningapp.db.Run
import com.example.runningapp.other.Constants.ACTION_PAUSE_SERVICE
import com.example.runningapp.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runningapp.other.Constants.ACTION_STOP_SERVICE
import com.example.runningapp.other.Constants.MAP_ZOOM
import com.example.runningapp.other.Constants.POLYLINE_WIDTH
import com.example.runningapp.other.TrackingUtility
import com.example.runningapp.services.Polyline
import com.example.runningapp.services.TrackingService
import com.example.runningapp.ui.viewmodels.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tracking.*
import kotlinx.coroutines.Dispatchers.Main
import timber.log.Timber
import java.lang.Math.round
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

const val CANCEL_TRACKING_DIALOG_TAG="CancelDialog"

@AndroidEntryPoint
class TrackingFragment:Fragment(R.layout.fragment_tracking) {
    private val viewModel: MainViewModel by viewModels()

    private var isTracking=false
    private var pathPoints= mutableListOf<Polyline>()
    private var actualSpeed=0.0
    private var actualDistance=0

    private var maxSpeed=0.0
    private var minSpeed=0.0

    private var lineColor=Color.RED

    private var stopTimer=0L

    private var map: GoogleMap? = null

    private var currentTimeMillisec=0L

    private var menu: Menu? = null

    val Fragment.packageManager get() = activity?.packageManager


    @set:Inject
    var weight=80f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView.onCreate(savedInstanceState)
        btnToggleRun.setOnClickListener{
            toggleRun()
        }

        if (savedInstanceState!=null){
            val cancelTrackingDialog=parentFragmentManager.findFragmentByTag(
                CANCEL_TRACKING_DIALOG_TAG)as CancelTrackingDialog?
            cancelTrackingDialog?.setYesListener {
                stopRun()
            }
        }

        btnFinishRun.setOnClickListener {
            zoomToSeeWholeTrack()
            endRunAndSaveToDatabase()
        }

        mapView.getMapAsync{
            map=it
            addAllPolylines()
        }

        subscribeToObservers()

        spotify.setOnClickListener {
            var intent=Intent(getActivity(), Main::class.java)
            val launchIntent = packageManager?.getLaunchIntentForPackage("com.spotify.music")
            if (launchIntent != null) {
                startActivity(launchIntent);
            } else {

            }
        }

    }

    private fun subscribeToObservers(){
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })

        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints=it
            addLatestPolyline()
            moveCameraToUser()
        })

        TrackingService.timeRunInMillisec.observe(viewLifecycleOwner, Observer {
            currentTimeMillisec=it
            val formattedTime=TrackingUtility.getFormattedStopWatchTime(currentTimeMillisec, true)
            tvTimer.text=formattedTime
        })

        TrackingService.liveDistance.observe(viewLifecycleOwner, Observer {
            actualDistance=it
            if(actualDistance<1000) {
                tvDistance.text = "${actualDistance} m"
            }
            else
            {
                val distanceDouble=actualDistance/1000.0
                tvDistance.text = "${distanceDouble} Km"
            }
        })

        TrackingService.actualSpeed.observe(viewLifecycleOwner, Observer {
            actualSpeed=it
            if(isTracking==true) {
                tvActualSpeed.text = "${actualSpeed} Km/h"
            }
            else{
                tvActualSpeed.text = "0 Km/h"
            }
            stopTrackingIfTheUserStop()
        })

        TrackingService.highSpeed.observe(viewLifecycleOwner, Observer {
            maxSpeed=it
            Log.d("Teszt", "Max sebesség: ${maxSpeed}")
        })

        TrackingService.minSpeed.observe(viewLifecycleOwner, Observer {
            minSpeed=it
        })
    }

    // Megfelelő action meghívása
    private fun toggleRun(){
        if (isTracking){
            menu?.getItem(0)?.isVisible=true
            sendCommandToService(ACTION_PAUSE_SERVICE)
        }
        else{
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    // Menü létrehozása, beállítása
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.toolbar_tracking_menu, menu)
        this.menu=menu
    }

    // A menü elem láthatóságának beállítása
    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        if (currentTimeMillisec>0L){
            this.menu?.getItem(0)?.isVisible=true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.miCancelTracking->{
                showCancelTrackingDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // Kilépés a rögzítésből üzenet megjelenítése
    private fun showCancelTrackingDialog(){
        CancelTrackingDialog().apply {
            setYesListener {
                stopRun()
            }
        }.show(parentFragmentManager, CANCEL_TRACKING_DIALOG_TAG)
    }

    // Rögzítés megállítása
    private fun stopRun(){
        tvTimer.text="00:00:00:00"
        stopTimer=0
        tvActualSpeed.text="Okm/h"
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }

    // A gomb szövegének beállítása
    private fun updateTracking(isTracking:Boolean){
        this.isTracking=isTracking
        if (!isTracking && currentTimeMillisec>0L){
            btnToggleRun.text="Start"
            btnFinishRun.visibility=View.VISIBLE
            stopTimer=0
        }
        else if(isTracking){
            btnToggleRun.text="Stop"
            menu?.getItem(0)?.isVisible=true
            btnFinishRun.visibility=View.GONE
            stopTimer=0
        }
    }

    // Oda mozgatja a kamerát a térképen, ahol éppen a felhasználó van
    private fun moveCameraToUser(){
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()){
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }

    // Annyira nagyít rá a térképre, hogy látszódjon az egész útvonalunk
    private fun zoomToSeeWholeTrack(){
        val bounds=LatLngBounds.Builder()
        for (polyline in pathPoints){
            for (pos in polyline){
                bounds.include(pos)
            }
        }

        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                mapView.width,
                mapView.height,
                (mapView.height*0.05f).toInt()
            )
        )
    }

    // A futás rögzítésének befejezése, majd a futása adatainak mentése az adatbázisba, hogy később vissza tudjuk nézni
    private fun endRunAndSaveToDatabase(){
        map?.snapshot { bmp->
            var distanceInMeters=0
            for (polyline in pathPoints){
                distanceInMeters+=TrackingUtility.calculatePolylineLength(polyline).toInt()
            }

            val avgSpeed=round((distanceInMeters/1000f)/(currentTimeMillisec/1000f/60/60)*10)/ 10f
            val dateTimestamp=Calendar.getInstance().timeInMillis
            //val dateTimestamp=java.sql.Timestamp(System.currentTimeMillis())
            val caloriesBurned=((distanceInMeters/1000f)*weight).toInt()


            val run=Run(bmp, dateTimestamp, avgSpeed, distanceInMeters, currentTimeMillisec, caloriesBurned, maxSpeed, minSpeed)

            viewModel.insertRun(run)
            Snackbar.make(
                requireActivity().findViewById(R.id.rootView),
                "A futás mentése sikeres",
                Snackbar.LENGTH_LONG
            ).show()
            stopRun()
        }
    }

    // Ez rajzolja ki az összes helyzetet, az egész útvonalunkat
    private fun addAllPolylines(){
        for (polyline in pathPoints){
            if(actualSpeed<=5){
                lineColor = Color.RED
            }
            else if (actualSpeed>5 && actualSpeed<=15){
                lineColor=Color.YELLOW
            }
            else{
                lineColor=Color.GREEN
            }
            val polylineOptions=PolylineOptions()
                .color(lineColor)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            map?.addPolyline(polylineOptions)
        }
    }

    // Az utolsó helyzet(koordináta) kirajzolása, itt állítjuk be a vonal színét, szélességét...
    // Ez csak az utolsó két helyzetet köti össze, nem rajzolja ki az összes koordinátát
    private fun addLatestPolyline(){
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1){
            val preLastLatLng=pathPoints.last()[pathPoints.last().size-2]
            val lastLatLng=pathPoints.last().last()
            if(actualSpeed<=5){
                lineColor = Color.RED
            }
            else if (actualSpeed>5 && actualSpeed<=15){
                lineColor=Color.YELLOW
            }
            else{
                lineColor=Color.GREEN
            }

            val polylineOptions=PolylineOptions()
                .color(lineColor)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)
            map?.addPolyline(polylineOptions)
        }
    }

    // Parancs küldése a servicenek
    private fun sendCommandToService(action: String)=
        Intent(requireContext(), TrackingService::class.java).also {
            it.action=action
            requireContext().startService(it)
        }

    private fun stopTrackingIfTheUserStop(){
        if(actualSpeed<1.0){
            stopTimer=stopTimer+1
        }


        if (stopTimer==10L){
            sendCommandToService(ACTION_PAUSE_SERVICE)
        }
        Timber.d("Ido: ${stopTimer}")
    }

    //Térkép nézet folytatás, elindítás, leállítás, megállítás, mi történjen, ha kevés a memória
    //Térkép életciklus
    override fun onResume() {
        super.onResume()
        stopTimer=0
        mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        stopTimer=0
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        stopTimer=0
        tvActualSpeed.text="O Km/h"
        mapView?.onStop()
    }

    override fun onPause() {
        super.onPause()
        stopTimer=0
        tvActualSpeed.text="O Km/h"
        mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()

        mapView?.onLowMemory()
    }

    //A térkép státuszának mentése
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        mapView?.onSaveInstanceState(outState)
    }
}