package org.app.glimpse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.mapview.MapView
import org.app.glimpse.pressentation.components.BaseScreen
import org.app.glimpse.pressentation.theme.GlimpseTheme

lateinit var mapView: MapView
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapView = MapView(this)
        enableEdgeToEdge()
        setContent {
            GlimpseTheme {
                BaseScreen()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapView.onStart()
    }

    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }
}