package org.app.glimpse.pressentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.yandex.mapkit.mapview.MapView
import org.app.glimpse.mapView

@Composable
fun MainScreen(){
    Box(
        modifier = Modifier.fillMaxSize()
    ){
        AndroidView(
            factory = { mapView }
        )
    }
}