package org.app.glimpse.data

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import io.ktor.websocket.Frame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.app.glimpse.MyApplication
import org.app.glimpse.R
import org.app.glimpse.data.network.UpdateUser
import org.app.glimpse.data.repository.ApiRepository
import org.app.glimpse.data.repository.UserDataRepository
import org.app.glimpse.data.repository.UserPreferencesRepository

class LocationTrackingService: Service() {

    private var isTracking = false
    val scope = CoroutineScope(Dispatchers.IO+ SupervisorJob())
    private lateinit var apiRepository: ApiRepository
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var userDataRepository: UserDataRepository
    private lateinit var fusedClient: FusedLocationProviderClient
    private lateinit var applicationContext: MyApplication
    private val callback = object:  LocationCallback() {
        var latitude = 0.0
        var longitude = 0.0
        override fun onLocationResult(p0: LocationResult) {
            val user = runBlocking {
                userDataRepository.userData.first()
            }
            val location = p0.lastLocation
            if(location != null){
                val res = FloatArray(1)
                Location.distanceBetween(user.latitude,user.longitude,location.latitude,location.longitude,res)
                if(res[0] <= 5.0f){
                    return
                } else {
                    latitude = location.latitude
                    longitude = location.longitude
                    scope.launch {
                        try {
                            userDataRepository.setUserData(
                                userDataRepository.userData.first().toBuilder()
                                    .setLatitude(latitude)
                                    .setLongitude(longitude)
                                    .build()
                            )
                            apiRepository.updateUserData(
                                userPreferencesRepository.token.first(),
                                UpdateUser(
                                    latitude = latitude,
                                    longitude = longitude
                                )
                            )
                            applicationContext.webSocketCnn!!.send(Frame.Text(""))
                        } catch(e: Exception) {
                            Log.e("Tracking",e.localizedMessage ?: "")
                        }
                    }
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        applicationContext = (application as MyApplication)
        apiRepository = (application as MyApplication).apiRepository
        userPreferencesRepository = (application as MyApplication).userPreferencesRepository
        userDataRepository = (application as MyApplication).userDataRepository
        fusedClient = LocationServices.getFusedLocationProviderClient(this)
    }

    enum class Actions {
        START_TRACKING,
        STOP_TRACKING
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action){
            Actions.STOP_TRACKING.name -> {
                if(isTracking){
                    isTracking = false
                    fusedClient.removeLocationUpdates(callback)
                    scope.cancel()
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
            Actions.START_TRACKING.name -> {
                if(!isTracking) {
                    startLocationUpdates()
                    isTracking = true
                }
            }
        }
        return START_STICKY
    }

    private fun buildNotification(): Notification {
        val chanelId = "location_tracking"
        val notification = NotificationCompat.Builder(this,chanelId)
            .setSmallIcon(R.drawable.my_location)
            .setContentTitle("Tracking active")
            .addAction(
                0,
                "Stop tracking",
                PendingIntent.getService(
                    this,
                    0,
                    Intent(
                        this,
                        LocationTrackingService::class.java,
                    ).apply {
                        action = Actions.STOP_TRACKING.name
                    },
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            .setAutoCancel(false)
            .setOngoing(true)
            .build()
        return notification
    }

    private fun startLocationUpdates(){
        startForeground(1,buildNotification())
        scope.launch {
            val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()
            fusedClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        fusedClient.removeLocationUpdates(callback)
        scope.cancel()
    }
}