package org.app.glimpse.data

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.app.glimpse.MyApplication
import org.app.glimpse.R
import org.app.glimpse.data.network.UpdateUser
import org.app.glimpse.data.repository.ApiRepository
import org.app.glimpse.data.repository.UserDataRepository
import org.app.glimpse.data.repository.UserPreferencesRepository

class LocationTrackingService: Service() {

    val scope = CoroutineScope(Dispatchers.IO+ SupervisorJob())
    private lateinit var apiRepository: ApiRepository
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var userDataRepository: UserDataRepository
    private lateinit var fusedClient: FusedLocationProviderClient
    private val callback = object:  LocationCallback() {
        private var latitude = 0.0
        private var longitude = 0.0
        override fun onLocationResult(p0: LocationResult) {
            latitude = p0.lastLocation?.latitude ?: latitude
            longitude = p0.lastLocation?.longitude ?: longitude
            scope.launch {
                apiRepository.updateUserData(
                    userPreferencesRepository.token.first(),
                    UpdateUser(
                        userDataRepository.userData.first().name,
                        latitude = latitude,
                        longitude = longitude
                    )
                )
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
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
                scope.launch {
                    userPreferencesRepository.toggleServiceRun(false)
                }
                fusedClient.removeLocationUpdates(callback)
                scope.cancel()
                stopSelf()
            }
            Actions.START_TRACKING.name -> startLocationUpdates()
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
            .build()
        return notification
    }

    private fun startLocationUpdates(){
        val context = this
        val hasFinePerm = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasBackPerm = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
        startForeground(1,buildNotification())
        scope.launch {
            userPreferencesRepository.toggleServiceRun(true)
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val hasNotifPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                while(!hasBackPerm || !hasFinePerm || !hasNotifPerm) {
                    delay (1000)
                }
            } else {
                while(!hasBackPerm || !hasFinePerm) {
                    delay (1000)
                }
            }
            val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()
            fusedClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        fusedClient.removeLocationUpdates(callback)
        scope.launch {
            userPreferencesRepository.toggleServiceRun(false)
        }
        scope.cancel()
    }
}