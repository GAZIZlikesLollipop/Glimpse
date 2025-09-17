package org.app.glimpse.pressentation.components

import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.runtime.ui_view.ViewProvider


@Composable
fun UserPlacemark(
    avatar: String,
    name: String,
    userView: ComposeView,
    placemark: PlacemarkMapObject,
    root: ViewGroup,
    hiddenHost: ViewGroup
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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        SubcomposeAsyncImage(
            model = avatar,
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
                userView.post {
                    userView.measure(
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                    )
                    userView.layout(0, 0, userView.measuredWidth, userView.measuredHeight)

                    val vp = ViewProvider(userView, true)
                    placemark.setView(vp) {
                        root.post {
                            try { root.removeView(hiddenHost) } catch (_: Exception) {}
                        }
                    }
                }
            }
        )
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}