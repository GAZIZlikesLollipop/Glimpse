package org.app.glimpse

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.yandex.mapkit.MapKitFactory
import io.ktor.websocket.DefaultWebSocketSession
import org.app.glimpse.data.network.ApiService
import org.app.glimpse.data.repository.ApiRepository
import org.app.glimpse.data.repository.UserDataRepository
import org.app.glimpse.data.repository.UserPreferencesRepository

class MyApplication: Application() {
    val apiRepository = ApiRepository(ApiService.httpClient)
    lateinit var userPreferencesRepository: UserPreferencesRepository
    var webSocketCnn: DefaultWebSocketSession? = null
    lateinit var userDataRepository: UserDataRepository
    override fun onCreate() {
        super.onCreate()
        userPreferencesRepository = UserPreferencesRepository(applicationContext)
        userDataRepository = UserDataRepository(applicationContext)
        MapKitFactory.setApiKey(BuildConfig.yandexApiKey)
        MapKitFactory.initialize(this)
        val channel = NotificationChannel(
            "location_tracking",
            "Location tracking",
            NotificationManager.IMPORTANCE_HIGH
        )
        val notificationManager = this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}