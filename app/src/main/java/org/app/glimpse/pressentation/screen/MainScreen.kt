@file:OptIn(ExperimentalMaterial3Api::class)

package org.app.glimpse.pressentation.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.navigation.NavController
import com.yandex.mapkit.Animation
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.TextStyle
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import org.app.glimpse.R
import org.app.glimpse.Route
import org.app.glimpse.data.ApiViewModel
import org.app.glimpse.pressentation.components.ChatCard

@Composable
fun MainScreen(
    paddingValues: PaddingValues,
    navController: NavController,
    apiViewModel: ApiViewModel
){
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    val point = Point(41.216728,69.335105)
    val windowInfo = LocalWindowInfo.current
    val isDarkTheme = isSystemInDarkTheme()
    var isChats by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val cnt = stringArrayResource(R.array.main_cnt)
    var isAdd by rememberSaveable { mutableStateOf(false) }

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
                            apiViewModel.userData.name,
                            TextStyle().apply {
                                this.placement = TextStyle.Placement.BOTTOM
                                this.offset = -10.0f
                                this.size = 12f
                                this.color = 1
                            }
                        )
                        setIcon(
                            ImageProvider.fromResource(context, R.drawable.my_location),
                            IconStyle().apply {
//                                anchor = PointF(0.5f, 0.5f)
                                scale = 1f
                                zIndex = 10f
                            }
                        )
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
                    contentDescription = "navigate to your location",
                    modifier = Modifier.size((windowInfo.containerSize.width/36).dp)
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
                    contentDescription = "Friends",
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
                AnimatedContent(
                    targetState = isAdd,
                    transitionSpec = {
                        if(!isAdd) {
                            slideInHorizontally(tween(450,50),{it}) togetherWith slideOutHorizontally(tween(550,50),{-it})
                        } else {
                            slideInHorizontally(tween(450,50),{-it}) togetherWith slideOutHorizontally(tween(550,50),{it})
                        }
                    }
                ) { state ->
                    if (!state) {
                        Box(Modifier.fillMaxSize()) {
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
                                    items(apiViewModel.userData.friends) {
                                        ChatCard(
                                            friend = it,
                                            onLocation = {
                                                isChats = false
                                                mapView.apply {
                                                    mapWindow.map.move(
                                                        CameraPosition(
                                                            Point(
                                                                it.latitude,
                                                                it.longitude
                                                            ),
                                                            18.0f,
                                                            0f,
                                                            0f,
                                                        ),
                                                        Animation(Animation.Type.SMOOTH, 1.0f),
                                                        {}
                                                    )
                                                }
                                            },
                                            onChat = {
                                                isChats = false
                                                navController.navigate(Route.Chat.createRoute(it.id))
                                            },
                                            onProfile = {
                                                isChats = false
                                                navController.navigate(Route.Profile.createRoute(it.id))
                                            }
                                        )
                                    }
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(paddingValues)
                                    .padding(horizontal = 12.dp)
                            ) {
                                Button(
                                    onClick = { isAdd = true },
                                    border = BorderStroke(
                                        2.dp,
                                        MaterialTheme.colorScheme.onBackground
                                    ),
                                    colors = ButtonDefaults.buttonColors(
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.onBackground
                                    ),
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.size((windowInfo.containerSize.width / 22).dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Add,
                                        contentDescription = "Add friend",
                                        modifier = Modifier.size((windowInfo.containerSize.width / 42).dp)
                                    )
                                }
                            }
                        }
                    } else {

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