@file:OptIn(ExperimentalMaterial3Api::class)

package org.app.glimpse.pressentation.screen

import android.graphics.PointF
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringArrayResource
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
import org.app.glimpse.data.Friend
import org.app.glimpse.data.FriendUser
import org.app.glimpse.pressentation.components.ChatCard
import java.time.OffsetDateTime

val testFriends = listOf(
    Friend(
        id = 0,
        userId = 1,
        messages = emptyList(),
        data = FriendUser(
            userName = "Furiya",
            avatar = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSmGjt5BrPeTNQMuCNHnIAPjzPzi-SJRKqqxA&s",
            latitude = 41.216728,
            longitude = 69.335105,
            lastOnline = OffsetDateTime.now(),
            friends = emptyList(),
            createdAt = OffsetDateTime.now().minusWeeks(1),
            updatedAt = OffsetDateTime.now().minusDays(2)
        )
    ),
    Friend(
        id = 1,
        userId = 0,
        messages = emptyList(),
        data = FriendUser(
            userName = "D. Tramp",
            avatar = "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Ftse1.mm.bing.net%2Fth%2Fid%2FOIP.-O8kGYxjDkwOldLaV9HL1AHaFW%3Fr%3D0%26pid%3DApi&f=1&ipt=a0dc84ded74f9869574f695c92874028aa9553d5d787c2d4865ba8fc72bbb04b&ipo=images",
            latitude = 41.216195,
            longitude = 69.335341,
            lastOnline = OffsetDateTime.now().minusHours(6),
            friends = emptyList(),
            createdAt = OffsetDateTime.now().minusYears(1),
            updatedAt = OffsetDateTime.now().minusMonths(1)
        )
    ),
    Friend(
        id = 2,
        userId = 2,
        messages = emptyList(),
        data = FriendUser(
            userName = "Vanya2077",
            avatar = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTP0KtXQ5ov5a9PoDEWxGpv1AL83o8As6y5cw&s",
            latitude = 41.232697,
            longitude = 69.335182,
            lastOnline = OffsetDateTime.now().minusHours(1),
            friends = emptyList(),
            createdAt = OffsetDateTime.now().minusDays(3),
            updatedAt = OffsetDateTime.now().minusDays(1)
        )
    )
)

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
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val cnt = stringArrayResource(R.array.main_cnt)

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
                        mapWindow.map.move( CameraPosition(
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
                modifier = Modifier.size((windowInfo.containerSize.width/19).dp)
            ) {
                Icon(
                   imageVector = ImageVector.vectorResource(R.drawable.navigation),
                    contentDescription = "",
                    modifier = Modifier.size((windowInfo.containerSize.width/35).dp)
                )
            }

            Button(
                onClick = { isChats = true },
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .width((windowInfo.containerSize.width/20).dp)
                    .height((windowInfo.containerSize.height/34).dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(0.8f),
                    contentColor = MaterialTheme.colorScheme.onBackground
                ),
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.group),
                    contentDescription = "",
                    modifier = Modifier.size((windowInfo.containerSize.width/34).dp)
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
                .size((windowInfo.containerSize.width/27).dp),
            colors = ButtonDefaults.buttonColors(
                MaterialTheme.colorScheme.background.copy(0.75f),
                MaterialTheme.colorScheme.onBackground
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
                onDismissRequest = { isChats = false },
                sheetState = sheetState,
                modifier = Modifier.fillMaxSize().padding(top = paddingValues.calculateTopPadding())
            ) {
                Box(Modifier.fillMaxSize()){
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                        ) {
                            Text(
                                text = cnt[0],
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.displayMedium
                            )
                        }
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(12.dp)
                        ) {
                            items(testFriends) {
                                ChatCard(
                                    data = it,
                                    onLocation = {
                                        isChats = false
                                        mapView.apply {
                                            mapWindow.map.move( CameraPosition(
                                                Point(it.data.latitude,it.data.longitude),
                                                18.0f,
                                                0f,
                                                0f,
                                            ),
                                                Animation(Animation.Type.SMOOTH,1.0f),
                                                {}
                                            )
                                        }
                                    },
                                    onChat = {}
                                )
                            }
                        }
                    }
                    Box (modifier = Modifier.align(Alignment.BottomEnd).padding(paddingValues).padding(horizontal = 12.dp)){
                        Button(
                            onClick = {},
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.onBackground),
                            colors = ButtonDefaults.buttonColors(
                                Color.Transparent,
                                MaterialTheme.colorScheme.onBackground
                            ),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.size((windowInfo.containerSize.width / 22).dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "",
                                modifier = Modifier.size((windowInfo.containerSize.width / 42).dp)
                            )
                        }
                    }
                }
            }
        }
    }

    DisposableEffect(mapView) {
        mapView.onStart()
        onDispose { mapView.onStop() }
    }
}