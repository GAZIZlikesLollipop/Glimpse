package org.app.glimpse

import android.app.Application
import com.yandex.mapkit.MapKitFactory

class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setApiKey(BuildConfig.yandexApiKey)
        MapKitFactory.initialize(this)
    }
}