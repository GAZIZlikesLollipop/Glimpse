package org.app.glimpse.pressentation.screen

import android.graphics.PointF
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.yandex.mapkit.Animation
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.TextStyle
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import org.app.glimpse.R

@Composable
fun MainScreen(
    paddingValues: PaddingValues
){
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    val point = Point(41.216728,69.335105)
    val windowInfo = LocalWindowInfo.current
    val isDarkTheme = isSystemInDarkTheme()
    var isChats by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(isDarkTheme) {
        mapView.apply { mapWindow.map.apply { isNightModeEnabled = if(isDarkTheme) true else false } }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ){
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                mapView.apply {
                    mapWindow.map.mapObjects.addPlacemark().apply {
                        geometry = point
                        setText(
                            "User",
                            TextStyle().apply {
                                this.placement = TextStyle.Placement.BOTTOM
                                this.offset = -10.0f
                                this.size = 10f
                            }
                        )
                        useCompositeIcon().apply {
                            setIcon(
                                ImageProvider.fromResource(context, R.drawable.navigation),
                                IconStyle().apply {
                                    anchor = PointF(0.5f, 0.5f)
                                    scale = 0.3f
                                    flat = true
                                }
                            )
                        }
                    }
                    mapWindow.map.move(
                    CameraPosition(
                    point,
                    18.0f,
                    0f,
                     0f
                         )
                    )
                }
            },
            update = {}
        )
        Column(
            modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp).padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    mapView.apply {
                        mapWindow.map.move(
                            CameraPosition(
                                point,
                                18.0f,
                                0f,
                                0f,
                            ),
                            Animation(Animation.Type.SMOOTH,1.0f),
                            {}
                        )
                    }
                },
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(0.65f),
                    contentColor = MaterialTheme.colorScheme.onBackground
                ),
                shape = CircleShape,
                modifier = Modifier.size((windowInfo.containerSize.width/16).dp)
            ) {
                Icon(
                   imageVector = ImageVector.vectorResource(R.drawable.navigation),
                    contentDescription = "",
                    modifier = Modifier.size((windowInfo.containerSize.width/32).dp)
                )
            }

            Button(
                onClick = { isChats = true },
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .width((windowInfo.containerSize.width/16).dp)
                    .height((windowInfo.containerSize.height/30).dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(0.8f),
                    contentColor = MaterialTheme.colorScheme.onBackground
                ),
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.group),
                    contentDescription = "",
                    modifier = Modifier.size((windowInfo.containerSize.width/30).dp)
                )
            }
        }
        Button(
            onClick = {},
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .padding(paddingValues)
                .size((windowInfo.containerSize.width/22).dp),
            colors = ButtonDefaults.buttonColors(
                MaterialTheme.colorScheme.background.copy(0.75f),
                MaterialTheme.colorScheme.primary
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
           Icon(
               imageVector = Icons.Rounded.Settings,
               contentDescription = "Settings",
           )
        }
        if(isChats){
            ModalBottomSheet(
                onDismissRequest = {isChats = false}
            ) {

            }
        }
    }

    DisposableEffect(mapView) {
        mapView.onStart()
        onDispose { mapView.onStop() }
    }
}