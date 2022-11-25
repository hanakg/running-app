package com.example.runningapp.ui.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.style.LineHeightSpan
import android.util.Log
import android.view.*
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.OnLifecycleEvent
import androidx.navigation.fragment.findNavController
import com.example.runningapp.R
import com.example.runningapp.db.Run
import com.example.runningapp.other.Constants
import com.example.runningapp.other.Constants.ACTION_PAUSE_SERVICE
import com.example.runningapp.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runningapp.other.Constants.ACTION_STOP_SERVICE
import com.example.runningapp.other.Constants.MAP_ZOOM
import com.example.runningapp.other.Constants.POLYLINE_WIDTH
import com.example.runningapp.other.TrackingUtility
import com.example.runningapp.services.Polyline
import com.example.runningapp.services.TrackingService
import com.example.runningapp.ui.viewmodels.MainViewModel
import com.example.runningapp.ui.viewmodels.SharedViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tracking.*
import kotlinx.coroutines.Dispatchers.Main
import timber.log.Timber
import java.lang.Math.round
import java.util.*
import javax.inject.Inject

const val CANCEL_TRACKING_DIALOG_TAG="CancelDialog"

@AndroidEntryPoint
class TrackingFragment:Fragment(R.layout.fragment_tracking) {

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    lateinit var movement: String


    private val viewModel: MainViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private var isTracking=false
    private var pathPoints= mutableListOf<Polyline>()
    private var actualSpeed=0.0
    private var actualDistance=0

    private var maxSpeed=0.0
    private var minSpeed=0.0

    private var lineColor=Color.RED

    private var stopTimer=0L
    private var speedList= mutableListOf<Double>();

    private var map: GoogleMap? = null

    private var currentTimeMillisec=0L

    private var menu: Menu? = null

    var goalDistance: Int=0
    var goalTime: Long=0L

    var walkGetter: Int=Color.RED
        get() {
            if(actualSpeed<=2){
                return Color.RED
            }
            else if (actualSpeed>2 && actualSpeed<=6){
                return Color.YELLOW
            }
            else{
                return Color.GREEN
            }
        }

    var runGetter: Int=Color.RED
        get() {
            if(actualSpeed<=5){
                //Timber.d("Run getter piros meghívódott")
                return Color.RED
            }
            else if (actualSpeed>5 && actualSpeed<=15){
                //Timber.d("Run getter meghívódott")
                return  Color.YELLOW
            }
            else{
                //Timber.d("Run getter zöld meghívódott")
                return Color.GREEN
            }
        }

    var cycleGetter: Int=Color.RED
    get() {
        if(actualSpeed<=15){
            return Color.RED
        }
        else if (actualSpeed>15 && actualSpeed<=25){
            return Color.YELLOW
        }
        else{
            return Color.GREEN
        }
    }

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

        movement = sharedPreferences.getString(Constants.KEY_MOVEMENT, "Futás")!!

        subscribeToObservers()

        startMusicApp.setOnClickListener {
            var intent=Intent(getActivity(), Main::class.java)
            val launchIntent = packageManager?.getLaunchIntentForPackage("com.spotify.music")
            if (launchIntent != null) {
                startActivity(launchIntent);
            } else {

            }
        }

        setGoal.setOnClickListener {
            findNavController().navigate(R.id.action_trackingFragment_to_setGoalFragment,)
        }

        if(sharedViewModel.goalDistance.value!=null) {
            goalDistance = sharedViewModel.goalDistance.value!!
        }
        if(sharedViewModel.goalTime.value!=null){
            goalTime=sharedViewModel.goalTime.value!!
        }

        when (movement){
            "Futás"->{
                Timber.d("Beállítva")
                lineColor=runGetter
            }
            "Gyaloglás"->{
                lineColor=walkGetter
            }
            "Kerékpározás"->{
                lineColor=cycleGetter
            }
        }

    }

    private fun subscribeToObservers(){
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })

        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints=it
            Timber.d("PathPoint fut")
            addLatestPolyline()
            moveCameraToUser()

        })

        TrackingService.timeRunInMillisec.observe(viewLifecycleOwner, Observer {
            currentTimeMillisec=it

            val formattedTime =
                TrackingUtility.getFormattedStopWatchTime(currentTimeMillisec, true)
            if(goalTime!=0L) {

                val timeBack=TrackingUtility.getFormattedStopWatchTime((goalTime-currentTimeMillisec), true)
                tvTimer.text = timeBack

            }
            else{
                tvTimer.text = formattedTime
            }
        })

        TrackingService.updater.observe(viewLifecycleOwner, Observer {
            if(goalTime!=0L &&(goalTime-currentTimeMillisec)<=0)
            {
                this.endRunAndSaveToDatabase()
            }
        })

        TrackingService.liveDistance.observe(viewLifecycleOwner, Observer {

            actualDistance=it
            if(goalDistance!=0) {
                if ((goalDistance-actualDistance) < 1000) {
                    tvDistance.text = "Vissza: ${(goalDistance-actualDistance)} m"
                } else {
                    val distanceDouble = actualDistance / 1000.0
                    tvDistance.text = "Vissza: ${(goalDistance-distanceDouble)} Km"
                }

                if((actualDistance-goalDistance)>=0)
                {
                    endRunAndSaveToDatabase()
                }
            }
            else {
                if (actualDistance < 1000) {
                    tvDistance.text = "${actualDistance} m"
                } else {
                    val distanceDouble = actualDistance / 1000.0
                    tvDistance.text = "${distanceDouble} Km"
                }
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

        //Timber.d("MillisecEndRunhoz: ${currentTimeMillisec} ---- ${goalTime}")
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
        speedList.clear()
        tvActualSpeed.text="Okm/h"
        sendCommandToService(ACTION_STOP_SERVICE)

        if(goalDistance!=0)
        {
            sharedViewModel.setGoalDistance(0)
            Timber.d("Táv vissza")
        }

        if(goalTime!=0L)
        {
            sharedViewModel.setGoalTime(0L)
            Timber.d("Idő vissza")
        }

        Timber.d("Ms: ${currentTimeMillisec}")

        if(sharedViewModel.challenge.value==true) {
            findNavController().navigate(R.id.action_trackingFragment_to_compareRunsFragment)
            sharedViewModel.setChallenge(false)
        }
        else{
            Timber.d("Ms: ${R.id.action_trackingFragment_to_runFragment}")
            //findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
            //activity?.recreate()
            findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
        }

    }

    // A gomb szövegének beállítása
    private fun updateTracking(isTracking:Boolean){
        this.isTracking=isTracking
        if (!isTracking && currentTimeMillisec>0L){
            btnToggleRun.text=""
            btnToggleRun.setIconResource(R.drawable.ic_play)
            btnToggleRun.iconTint= ColorStateList.valueOf(Color.BLACK)
            val scale = resources.displayMetrics.density
            btnToggleRun.iconGravity=MaterialButton.ICON_GRAVITY_TEXT_START
            btnToggleRun.iconPadding=0
            btnFinishRun.visibility=View.VISIBLE
            stopTimer=0
            speedList.clear()
        }
        else if(isTracking){
            btnToggleRun.text=""
            btnToggleRun.setIconResource(R.drawable.ic_pause)
            btnToggleRun.iconTint= ColorStateList.valueOf(Color.BLACK)
            btnToggleRun.iconGravity=MaterialButton.ICON_GRAVITY_TEXT_START
            btnToggleRun.iconPadding=0
            menu?.getItem(0)?.isVisible=true
            btnFinishRun.visibility=View.GONE
            setGoal.visibility=View.GONE
            stopTimer=0
            speedList.clear()
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


            //MET: https://www.topendsports.com/weight-loss/energy-met.htm
            //val caloriesBurned=((distanceInMeters/1000f)*weight).toInt()
            var MET=1.3f

            when (movement){
                "Futás"->{
                    if(avgSpeed<=6)
                    {
                        MET=5.0f
                    }
                    else if(avgSpeed>6 && avgSpeed<=10){
                        MET=10.0f
                    }
                    else if (avgSpeed>10 && avgSpeed<=13){
                        MET=13.5f
                    }
                    else{
                        MET=16.0f
                    }
                }
                "Gyaloglás"->{
                    if(avgSpeed<=2){
                        MET=2.0f
                    }
                    else if (avgSpeed>2 && avgSpeed<=4){
                        MET=3.0f
                    }
                    else if (avgSpeed>4 && avgSpeed<=6){
                        MET=4.0f
                    }
                    else{
                        MET=5.0f
                    }
                }
                "Kerékpározás"->{
                    if(avgSpeed<=16){
                        MET=4.0f
                    }
                    else if (avgSpeed>16 && avgSpeed<=25){
                        MET=7.0f
                    }
                    else{
                        MET=10.0f
                    }
                }
            }

            //https://www.verywellfit.com/how-many-calories-you-burn-during-exercise-4111064
            Timber.d("${MET}")
            val caloriesBurnedFloat= (currentTimeMillisec/60000.0f)*(MET*3.5f*weight)/200.0f

            val caloriesBurned=caloriesBurnedFloat.toInt()

            val run=Run(bmp, dateTimestamp, avgSpeed, distanceInMeters, currentTimeMillisec, caloriesBurned, maxSpeed, minSpeed)

            Timber.d("${avgSpeed} + ${currentTimeMillisec} + ${caloriesBurned}")
            viewModel.insertRun(run)
            Snackbar.make(
                requireActivity().findViewById(R.id.rootView),
                "A futás mentése sikeres",
                Snackbar.LENGTH_LONG
            ).show()

            if(sharedViewModel.challenge.value==true) {
                sharedViewModel.setNewRun(run)
            }

            stopRun()
        }
    }

    // Ez rajzolja ki az összes helyzetet, az egész útvonalunkat
    private fun addAllPolylines(){
        for (polyline in pathPoints){
            /*when (movement){
                "Futás"->{
                    if(actualSpeed<=5){
                        lineColor = Color.RED
                    }
                    else if (actualSpeed>5 && actualSpeed<=15){
                        lineColor=Color.YELLOW
                    }
                    else{
                        lineColor=Color.GREEN
                    }
                }
                "Gyaloglás"->{
                    if(actualSpeed<=2){
                        lineColor = Color.RED
                    }
                    else if (actualSpeed>2 && actualSpeed<=6){
                        lineColor=Color.YELLOW
                    }
                    else{
                        lineColor=Color.GREEN
                    }
                }
                "Kerékpározás"->{
                    if(actualSpeed<=15){
                        lineColor = Color.RED
                    }
                    else if (actualSpeed>15 && actualSpeed<=25){
                        lineColor=Color.YELLOW
                    }
                    else{
                        lineColor=Color.GREEN
                    }
                }
            }*/

            when (movement){
                "Futás"->{
                    //Timber.d("Beállítva")
                    lineColor=runGetter
                }
                "Gyaloglás"->{
                    lineColor=walkGetter
                }
                "Kerékpározás"->{
                    lineColor=cycleGetter
                }
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

            if (pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
                val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2]
                val lastLatLng = pathPoints.last().last()
                /*when (movement){
                "Futás"->{
                    if(actualSpeed<=5){
                        lineColor = Color.RED
                    }
                    else if (actualSpeed>5 && actualSpeed<=15){
                        lineColor=Color.YELLOW
                    }
                    else{
                        lineColor=Color.GREEN
                    }
                }
                "Gyaloglás"->{
                    if(actualSpeed<=2){
                        lineColor = Color.RED
                    }
                    else if (actualSpeed>2 && actualSpeed<=6){
                        lineColor=Color.YELLOW
                    }
                    else{
                        lineColor=Color.GREEN
                    }
                }
                "Kerékpározás"->{
                    if(actualSpeed<=15){
                        lineColor = Color.RED
                    }
                    else if (actualSpeed>15 && actualSpeed<=25){
                        lineColor=Color.YELLOW
                    }
                    else{
                        lineColor=Color.GREEN
                    }
                }
            }*/

                when (movement) {
                    "Futás" -> {
                        //Timber.d("Beállítva")
                        lineColor = runGetter
                    }
                    "Gyaloglás" -> {
                        lineColor = walkGetter
                    }
                    "Kerékpározás" -> {
                        lineColor = cycleGetter
                    }
                }

                val polylineOptions = PolylineOptions()
                    .color(lineColor)
                    .width(POLYLINE_WIDTH)
                    .add(preLastLatLng)
                    .add(lastLatLng)
                map?.addPolyline(polylineOptions)
                Timber.d("Map kirajzolas")
            }
    }

    // Parancs küldése a servicenek
    private fun sendCommandToService(action: String)=
        Intent(requireContext(), TrackingService::class.java).also {
            it.action=action
            requireContext().startService(it)
        }

    private fun stopTrackingIfTheUserStop(){
        /*if(actualSpeed<1.0){
            stopTimer=stopTimer+1
        }


        if (stopTimer==10L){
            sendCommandToService(ACTION_PAUSE_SERVICE)
        }
        Timber.d("Ido: ${stopTimer}")*/

        if (stopTimer<10)
        {
            speedList.add(actualSpeed)
            stopTimer=stopTimer+1

            Timber.d("Ido: ${stopTimer}")
            Timber.d("Sebesseg: ${actualSpeed}")
            Timber.d("Atlag: ${speedList.sum()/speedList.size}")
            Timber.d("Size: ${speedList.size}")
        }
        else {
            if (speedList.sum() / speedList.size <= 1.5) {
                sendCommandToService(ACTION_PAUSE_SERVICE)
            }
            else
            {
                speedList.removeAt(0)
                speedList.add(actualSpeed)

                stopTimer=stopTimer+1

                Timber.d("Ido: ${stopTimer}")

                Timber.d("Sebesseg: ${actualSpeed}")
                Timber.d("Atlag: ${speedList.sum()/speedList.size}")
            }
        }

    }

    //Térkép nézet folytatás, elindítás, leállítás, megállítás, mi történjen, ha kevés a memória
    //Térkép életciklus
    override fun onResume() {
        super.onResume()
        //stopTimer=0
        //speedList.clear()
        mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        //stopTimer=0
        //speedList.clear()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        //stopTimer=0
        //speedList.clear()
        tvActualSpeed.text="O Km/h"
        mapView?.onStop()
    }

    override fun onPause() {
        super.onPause()
        //stopTimer=0
        //speedList.clear()
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