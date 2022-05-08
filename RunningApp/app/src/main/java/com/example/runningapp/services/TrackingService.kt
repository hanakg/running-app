package com.example.runningapp.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.content.Intent.getIntent
import android.location.Location
import android.location.LocationListener
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.runningapp.R
import com.example.runningapp.other.Constants.ACTION_PAUSE_SERVICE
import com.example.runningapp.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runningapp.other.Constants.ACTION_STOP_SERVICE
import com.example.runningapp.other.Constants.FASTEST_LOCATION_INTERVAL
import com.example.runningapp.other.Constants.LOCATION_UPDATE_INTERVAL
import com.example.runningapp.other.Constants.NOTIFICATION_CHANNEL_ID
import com.example.runningapp.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.runningapp.other.Constants.NOTIFICATION_ID
import com.example.runningapp.other.Constants.TIMER_UPDATE_INTERVAL
import com.example.runningapp.other.TrackingUtility
import com.example.runningapp.ui.MainActivity
import com.example.runningapp.ui.viewmodels.SharedViewModel
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlin.properties.Delegates

typealias Polyline=MutableList<LatLng>
typealias Polylines=MutableList<Polyline>

@AndroidEntryPoint
class TrackingService:LifecycleService() {
    var isFirstRun=true
    var serviceKilled=false

    //lateinit var mLocation: Location // location
    private val _locations = mutableListOf<LatLng>()

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val timeRunInSeconds=MutableLiveData<Long>()


    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder

    lateinit var curNotificationBuilder: NotificationCompat.Builder

    companion object{

        val timeRunInMillisec=MutableLiveData<Long>()
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<Polylines>()
        val liveDistance=MutableLiveData<Int>()
        val actualSpeed=MutableLiveData<Double>()
        val highSpeed=MutableLiveData<Double>()
        val minSpeed=MutableLiveData<Double>()
        val updater=MutableLiveData<Int>()
    }

    private fun postInitialValues(){
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        timeRunInSeconds.postValue(0L)
        timeRunInMillisec.postValue(0L)
        liveDistance.postValue(0)
        actualSpeed.postValue(0.0)
        highSpeed.postValue(0.0)
        minSpeed.postValue(0.0)
        updater.postValue(0)
    }

    override fun onCreate() {
        super.onCreate()
        curNotificationBuilder=baseNotificationBuilder
        postInitialValues()
        fusedLocationProviderClient=FusedLocationProviderClient(this)

        isTracking.observe(this, Observer {
            updateLocationTracking(it)
            updateNotificationTrackingState(it)
        })
        //distance=0
    }

    override fun onDestroy() {
        super.onDestroy()
        //System.exit(0)
        Timber.d("Destroooooy")
    }

    override fun getLifecycle(): Lifecycle {
        return super.getLifecycle()
        Timber.d("Lifecycle: ${this.lifecycle}")
    }

    // Rögzítés megállítása mentés nélkül
    private fun killService(){
        serviceKilled=true
        isFirstRun=true
        //distance=null
        pauseService()
        postInitialValues()
        //onDestroy()
        stopForeground(true)
        stopSelf()
    }

    // Amikor küldünk egy parancsot a servicenknek akkor ez a metódus hívódik meg és meghívja a megfelelő műveletet
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action){
                ACTION_START_OR_RESUME_SERVICE->{
                    if (isFirstRun){
                        startForegroundService()
                        isFirstRun=false
                        distance=0
                    }
                    else{
                        Timber.d("Resuming szolgáltatás...")
                        startTimer()
                    }
                }
                ACTION_PAUSE_SERVICE->{
                    Timber.d("Szüneteltetés szolgáltatás")
                    pauseService()
                }
                ACTION_STOP_SERVICE->{
                    Timber.d("Leállítás szolgáltatás")
                    killService()
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private var isTimerEnabled=false
    private var lapTime=0L
    private var totalTimeRun=0L
    private var timeStarted=0L
    private var lastSecondTimestamp=0L

    // Idő számlálásának elindítása
    private fun startTimer(){
        addEmptyPolyline()
        isTracking.postValue(true)
        timeStarted=System.currentTimeMillis()
        isTimerEnabled=true
        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!){
                //Now-start time
                lapTime=System.currentTimeMillis()-timeStarted
                // Az új laptime postolása
                timeRunInMillisec.postValue(totalTimeRun+lapTime)

                if (timeRunInMillisec.value!!>=lastSecondTimestamp+1000L){
                    timeRunInSeconds.postValue(timeRunInSeconds.value!!+1)
                    lastSecondTimestamp+=1000L
                }
                delay(TIMER_UPDATE_INTERVAL)
            }
            totalTimeRun+=lapTime
        }
    }

    private fun pauseService(){
        isTracking.postValue(false)
        isTimerEnabled=false
    }

    // Értesítések frissítése
    private fun updateNotificationTrackingState(isTracking: Boolean){
        val notificationActionText=if(isTracking) "Megállítás" else "Folytatás"
        val pendingIntent=if(isTracking){
            val pauseIntent=Intent(this, TrackingService::class.java).apply {
                action= ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this, 1, pauseIntent, FLAG_UPDATE_CURRENT)
        }
        else{
            val resumeIntent=Intent(this, TrackingService::class.java).apply {
                action= ACTION_START_OR_RESUME_SERVICE
            }
            PendingIntent.getService(this, 2, resumeIntent, FLAG_UPDATE_CURRENT)
        }

        val notificationManager=getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        curNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible=true
            set(curNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }
        if (!serviceKilled){
            curNotificationBuilder=baseNotificationBuilder
                .addAction(R.drawable.ic_pause, notificationActionText, pendingIntent)
            notificationManager.notify(NOTIFICATION_ID, curNotificationBuilder.build())
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean){
        // Itt állítjuk be, hogy milyen gyakran frissítse a helyzetünket, mi lehet a leggyorsabb frissítés
        if (isTracking){
            if (TrackingUtility.hasLocationPermission(this)){
                val request=LocationRequest().apply {
                    interval= LOCATION_UPDATE_INTERVAL
                    fastestInterval= FASTEST_LOCATION_INTERVAL
                    priority=PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
            else{
                fusedLocationProviderClient= FusedLocationProviderClient(this)
            }
        }
    }
    private var _highSpeed=0.0
    private var _minSpeed=Double.MAX_VALUE

    var distance= 0
    // Amíg fut a rögzítés addig újra és újra új koordinátákat ad hozzá a koordináta listához
    val locationCallback=object :LocationCallback(){
        override fun onLocationResult(result: LocationResult?) {
            super.onLocationResult(result)
            if (lifecycle.currentState == Lifecycle.State.STARTED) {
                if (isTracking.value!!) {
                    val currentLocation = result!!.lastLocation
                    val latLng = LatLng(currentLocation.latitude, currentLocation.longitude)

                    val lastLocation = _locations.lastOrNull()

                    if (lastLocation != null) {
                        //liveDistance.value = liveDistance.value!! + SphericalUtil.computeDistanceBetween(lastLocation, latLng).roundToInt()
                        if (distance != null) {
                            distance = distance!! + SphericalUtil.computeDistanceBetween(
                                lastLocation,
                                latLng
                            )
                                .roundToInt()
                            liveDistance.value = distance
                            lifecycle.currentState
                            Timber.d("Új distance: ${distance}")
                            Timber.d("asdLifecycle: ${lifecycle}---------${lifecycle.currentState}")
                        }
                    }

                    updater.postValue(1)

                    result?.locations?.let { locations ->
                        for (location in locations) {

                            addPathPoint(location)


                            Timber.d("Új time: ${timeRunInMillisec.value!!}")
                            //liveDistance.value=distance
                            //Teszt
                            //Timber.d("Új helyzet: ${location.latitude}, ${location.latitude}")
                            //onLocationChanged(location)
                            val speed: Double =
                                ((location.speed * 3.6) * 100.0).roundToInt() / 100.0

                            if (speed > _highSpeed) {
                                _highSpeed = speed
                                highSpeed.postValue(_highSpeed)
                            }

                            if (speed < _minSpeed) {
                                _minSpeed = speed
                                minSpeed.postValue(_minSpeed)
                            }

                            //Timber.d("Új high speed: ${highSpeed}")

                            actualSpeed.postValue(speed)
                            //Timber.d("Sebesség: ${speed}")
                            //setSpeed(location)
                        }
                    }
                }
            }
        }
    }

    // Új koordináta hozzáadása a koordináta listához
    private fun addPathPoint(location: Location?){
        location?.let {
            val pos=LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                last().add(pos)
                pathPoints.postValue(this)
            }

            _locations.add(pos)
        }
    }

    private fun addEmptyPolyline()= pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))

    private fun startForegroundService(){
        startTimer()
        isTracking.postValue(true)

        val notificationManager=getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Csak akkor kell, ha az android rendszer Oreo vagy későbbi verziójú
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            createNotificationChannel(notificationManager)
        }

        startForeground(NOTIFICATION_ID, baseNotificationBuilder.build())

        timeRunInSeconds.observe(this, Observer {
            if(isTracking.value!!) {
                if (!serviceKilled) {
                    val notification = curNotificationBuilder
                        .setContentText(TrackingUtility.getFormattedStopWatchTime(it * 1000L))
                    notificationManager.notify(NOTIFICATION_ID, notification.build())
                }
            }
        })
    }

    // Értesítése csatorna elkészítése
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager){
        val channel=NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, IMPORTANCE_LOW)

        notificationManager.createNotificationChannel(channel)
    }
}