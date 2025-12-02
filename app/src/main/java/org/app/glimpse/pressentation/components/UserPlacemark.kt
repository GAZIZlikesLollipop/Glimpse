package org.app.glimpse.pressentation.components

import android.util.Log
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.SubcomposeAsyncImage
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.allowHardware
import com.valentinilk.shimmer.shimmer
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.runtime.ui_view.ViewProvider

@Composable
fun UserPlacemarkBox(
    name: String,
    view: ComposeView,
    placemark: PlacemarkMapObject,
    root: ViewGroup
){
    val rainbowColorsBrush = remember {
        Brush.sweepGradient(
            listOf(
                Color(0xFF9575CD),
                Color(0xFFBA68C8),
                Color(0xFFE57373),
                Color(0xFFFFB74D),
                Color(0xFFFFF176),
                Color(0xFFAED581),
                Color(0xFF4DD0E1),
                Color(0xFF9575CD)
            )
        )
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Box(
            modifier = Modifier
                .size(65.dp)
                .shimmer()
                .border(
                    2.dp,
                    rainbowColorsBrush,
                    RoundedCornerShape(50.dp)
                )
                .clip(RoundedCornerShape(50.dp))
                .background(Color.Gray),
        )
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
    DisposableEffect(Unit) {
        if(placemark.isValid){
            view.post {
                val vp = ViewProvider(view, true)
                placemark.setView(vp) { root.post { root.removeView(view) } }
            }
        }
        onDispose {  }
    }
}

@Composable
fun UserPlacemark(
    avatar: String,
    name: String,
    view: ComposeView,
    placemark: PlacemarkMapObject,
    root: ViewGroup
){
    val rainbowColorsBrush = remember {
        Brush.sweepGradient(
            listOf(
                Color(0xFF9575CD),
                Color(0xFFBA68C8),
                Color(0xFFE57373),
                Color(0xFFFFB74D),
                Color(0xFFFFF176),
                Color(0xFFAED581),
                Color(0xFF4DD0E1),
                Color(0xFF9575CD)
            )
        )
    }
    val context = LocalContext.current
    val timestamp = System.currentTimeMillis()
    val imageURL = "${avatar}?t=$timestamp"
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        SubcomposeAsyncImage(
            model = imageURL,
            imageLoader = ImageLoader.Builder(context)
                .components { add(OkHttpNetworkFetcherFactory(createUnsafeOkHttpClient())) }
                .allowHardware(false)
                .build(),
            contentDescription = name,
            modifier = Modifier
                .size(65.dp)
                .border(
                    2.dp,
                    rainbowColorsBrush,
                    RoundedCornerShape(50.dp)
                )
                .clip(RoundedCornerShape(50.dp)),
            contentScale = ContentScale.Crop,
            onSuccess = {
                if(placemark.isValid){
                    view.post {
                        val vp = ViewProvider(view, true)
                        placemark.setView(vp) { root.post { root.removeView(view) } }
                    }
                }
            },
            onError = {
                Log.e("USER_PLACEMARK",it.result.throwable.localizedMessage ?: "user placemark error")
            },
        )
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}