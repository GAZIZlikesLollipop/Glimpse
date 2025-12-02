@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)

package org.app.glimpse.pressentation.screen

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.doOnAttach
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.valentinilk.shimmer.shimmer
import com.yandex.mapkit.Animation
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView
import kotlinx.coroutines.cancel
import org.app.glimpse.MyApplication
import org.app.glimpse.R
import org.app.glimpse.Route
import org.app.glimpse.data.LocationTrackingService
import org.app.glimpse.data.LocationTrackingService.Actions
import org.app.glimpse.data.network.ApiState
import org.app.glimpse.data.network.ApiViewModel
import org.app.glimpse.data.network.User
import org.app.glimpse.pressentation.components.ChatCard
import org.app.glimpse.pressentation.components.FriendAdd
import org.app.glimpse.pressentation.components.UserPlacemark
import org.app.glimpse.pressentation.components.UserPlacemarkBox

@Composable
fun MainScreen(
    paddingValues: PaddingValues,
    navController: NavController,
    apiViewModel: ApiViewModel
){
    val windowInfo = LocalWindowInfo.current
    val isDarkTheme = isSystemInDarkTheme()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val cnt = stringArrayResource(R.array.main_cnt)
    var isAdd by rememberSaveable { mutableStateOf(false) }
    val apiState by apiViewModel.userData.collectAsState()
    val you by apiViewModel.you.collectAsState()
    val context = LocalContext.current
    val mapView = remember { MapView(context) }

    LaunchedEffect(isDarkTheme) {
        mapView.apply { mapWindow.map.apply { isNightModeEnabled = isDarkTheme } }
    }

    LaunchedEffect(Unit) {
        apiViewModel.getOwnData {
            navController.navigate(Route.Login.route)
            apiViewModel.setRoute(Route.Login.route)
        }
        if(apiViewModel.webSocketCnn == null){
            apiViewModel.startWebSocket()
        }
    }

    val permissionRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {}

    val hasFinePermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION).status.isGranted

    val hasBackPermission = rememberPermissionState(Manifest.permission.ACCESS_BACKGROUND_LOCATION).status.isGranted

    val mapState by apiViewModel.mapState.collectAsState()

    LaunchedEffect(Unit) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasNotifPermission = ActivityCompat.checkSelfPermission(context,Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

            if(!hasFinePermission || !hasBackPermission || !hasNotifPermission){
                if(!hasFinePermission){
                    permissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
                if(!hasBackPermission) {
                    permissionRequest.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                }
                if (!hasNotifPermission) {
                    permissionRequest.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            } else {
                (context.applicationContext as MyApplication).webSocketCnn?.cancel()
                (context.applicationContext as MyApplication).webSocketCnn = apiViewModel.webSocketCnn
                val inta = Intent(context, LocationTrackingService::class.java).apply {
                    action = Actions.START_TRACKING.name
                }
                ContextCompat.startForegroundService(context, inta)
            }
        } else {
            if(!hasFinePermission || !hasBackPermission){
                if(!hasFinePermission){
                    permissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
                if(!hasBackPermission) {
                    permissionRequest.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                }
            } else {
                if(apiState is ApiState.Success) {
                    (context.applicationContext as MyApplication).webSocketCnn?.cancel()
                    (context.applicationContext as MyApplication).webSocketCnn = apiViewModel.webSocketCnn
                    val inta = Intent(context, LocationTrackingService::class.java).apply {
                        action = Actions.START_TRACKING.name
                    }
                    ContextCompat.startForegroundService(context, inta)
                }
            }
        }
        if(apiViewModel.isFirst){
            mapView.apply {
                mapWindow.map.move(
                    CameraPosition(
                        Point(you.latitude,you.longitude),
                        18f,
                        0f,
                        0f
                    ),
                    Animation(Animation.Type.SMOOTH,2f)
                )
                {}
            }
            apiViewModel.isFirst = false
        } else {
            mapView.apply { mapWindow.map.move(mapState) }
        }
    }

    LaunchedEffect(apiState,you) {
        if(apiState is ApiState.Success){
            mapView.apply {
                val activity = context as Activity
                val root = activity.findViewById<ViewGroup>(android.R.id.content)

                val boxView = ComposeView(context).apply {
                    translationX = -10_000f // offscreen, но прикреплён к окну
                    setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
                }
                val userView = ComposeView(context).apply {
                    translationX = -10_000f // offscreen, но прикреплён к окну
                    setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
                }
                val boxParent = boxView.parent
                if(boxParent is ViewGroup){
                    boxParent.removeView(boxView)
                }
                val userParent = userView.parent
                if(userParent is ViewGroup){
                    userParent.removeView(userView)
                }

                root.addView(boxView)
                root.addView(userView)

                if(!apiViewModel.isFirst) {
                    mapWindow.map.mapObjects.clear()
                }

                val placemark = mapWindow.map.mapObjects.addPlacemark().apply { geometry =
                    Point(you.latitude, you.longitude)
                }

                boxView.doOnAttach {
                    boxView.setContent {
                        UserPlacemarkBox(you.name,boxView,placemark,root)
                    }
                }

                userView.doOnAttach {
                    userView.setContent {
                        UserPlacemark(you.avatar,you.name,userView, placemark,root)
                    }
                }

                for(friend in you.friendsList){
                    val friendBoxView = ComposeView(context).apply {
                        translationX = -10_000f // offscreen, но прикреплён к окну
                        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
                    }
                    val friendView = ComposeView(context).apply {
                        translationX = -10_000f // offscreen, но прикреплён к окну
                        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
                    }
                    (friendBoxView.parent as? ViewGroup)?.removeView(friendBoxView)
                    (friendView.parent as? ViewGroup)?.removeView(friendView)

                    root.addView(friendBoxView)
                    root.addView(friendView)
                    val friendPlacemark = mapWindow.map.mapObjects.addPlacemark().apply { geometry = Point(friend.latitude,friend.longitude)}
                    friendBoxView.doOnAttach {
                        friendBoxView.setContent { UserPlacemarkBox(friend.name,friendBoxView,friendPlacemark,root) }
                    }
                    friendView.doOnAttach {
                        friendView.setContent { UserPlacemark(friend.avatar,friend.name,friendView,friendPlacemark,root) }
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ){
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { mapView },
            update = {}
        )
        Column(
            modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp).padding(vertical = 12.dp).padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    mapView.apply {
                        mapWindow.map.move(
                            CameraPosition(
                                Point(you.latitude, you.longitude),
                                18.0f,
                                0f,
                                0f,
                            ),
                            Animation(Animation.Type.SMOOTH, 2f),
                            {}
                        )
                    }
                },
                contentPadding = PaddingValues(0.dp),
                enabled = apiState is ApiState.Success,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(0.65f),
                    contentColor = MaterialTheme.colorScheme.onBackground
                ),
                shape = CircleShape,
                modifier = Modifier.size((windowInfo.containerSize.width/18).dp)
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.navigation),
                    contentDescription = "navigate to your location",
                    modifier = Modifier.size((windowInfo.containerSize.width/36).dp)
                )
            }

            Button(
                onClick = { apiViewModel.isChats = true },
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .width((windowInfo.containerSize.width/18).dp)
                    .height((windowInfo.containerSize.height/32).dp),
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
            onClick = {
                if(apiState is ApiState.Success){
                    val userData = (apiState as ApiState.Success).data as User
                    navController.navigate(Route.Profile.createRoute(userData.id))
                } else {
                    navController.navigate(Route.Profile.createRoute(0))
                }
            },
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
        if(apiViewModel.isChats) {
            ModalBottomSheet(
                onDismissRequest = { apiViewModel.isChats = false },
                sheetState = sheetState,
                modifier = Modifier.fillMaxSize().padding(top = paddingValues.calculateTopPadding())
            ) {
                AnimatedContent(
                    targetState = isAdd,
                    transitionSpec = {
                        if (!isAdd) {
                            slideInHorizontally(tween(450, 50),
                                { -it }) togetherWith slideOutHorizontally(tween(550, 50),
                                { it })
                        } else {
                            slideInHorizontally(tween(450, 50),
                                { it }) togetherWith slideOutHorizontally(tween(550, 50),
                                { -it })
                        }
                    }
                ) { state ->
                    if (!state) {
                        Box(Modifier.fillMaxSize()) {
                            Column {
                                Text(
                                    text = cnt[0],
                                    color = MaterialTheme.colorScheme.onBackground,
                                    style = MaterialTheme.typography.displayMedium,
                                    modifier = Modifier.padding(16.dp)
                                )
                                if (apiState is ApiState.Success) {
                                    if (you.friendsList.isNotEmpty()) {
                                        LazyColumn(
                                            verticalArrangement = Arrangement.spacedBy(12.dp),
                                            contentPadding = PaddingValues(12.dp)
                                        ) {
                                            items(you.friendsList) {
                                                ChatCard(
                                                    friend = it,
                                                    onLocation = {
                                                        apiViewModel.isChats = false
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
                                                                Animation(
                                                                    Animation.Type.SMOOTH,
                                                                    1.0f
                                                                ),
                                                                {}
                                                            )
                                                        }
                                                    },
                                                    onChat = {
                                                        apiViewModel.getChatMessages(it.id)
                                                        apiViewModel.isChats = false
                                                        navController.navigate(
                                                            Route.Chat.createRoute(
                                                                it.id
                                                            )
                                                        )
                                                    },
                                                    onProfile = {
                                                        apiViewModel.isChats = false
                                                        navController.navigate(
                                                            Route.Profile.createRoute(
                                                                it.id
                                                            )
                                                        )
                                                    }
                                                )
                                            }
                                        }
                                    } else {
                                        Column(
                                            modifier = Modifier.fillMaxSize(),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = ImageVector.vectorResource(R.drawable.group_off),
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onBackground.copy(0.6f),
                                                modifier = Modifier.size(100.dp)
                                            )
                                            Spacer(Modifier.height(8.dp))
                                            Text(
                                                cnt[1],
                                                style = MaterialTheme.typography.headlineLarge,
                                                color = MaterialTheme.colorScheme.onBackground.copy(0.6f),
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(1.dp)
                                            )
                                        }
                                    }
                                } else {
                                    Column {
                                        repeat(5) {
                                            Row(
                                                modifier = Modifier.width(windowInfo.containerSize.width.dp).padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .shimmer()
                                                        .clip(RoundedCornerShape(25.dp))
                                                        .size((windowInfo.containerSize.width / 18).dp)
                                                        .background(MaterialTheme.colorScheme.onBackground.copy(0.5f))
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .shimmer()
                                                        .weight(1f)
                                                        .height(30.dp)
                                                        .background(MaterialTheme.colorScheme.onBackground.copy(0.5f))
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .shimmer()
                                                        .clip(RoundedCornerShape(24.dp))
                                                        .size(50.dp)
                                                        .background(MaterialTheme.colorScheme.onBackground.copy(0.5f))
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .shimmer()
                                                        .clip(RoundedCornerShape(24.dp))
                                                        .size(50.dp)
                                                        .background(MaterialTheme.colorScheme.onBackground.copy(0.5f))
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(bottom = paddingValues.calculateBottomPadding())
                                    .padding(horizontal = 12.dp)
                            ) {
                                Button(
                                    onClick = { isAdd = true },
                                    border = BorderStroke(
                                        2.dp,
                                        if(apiState is ApiState.Success)MaterialTheme.colorScheme.onBackground else Color.Transparent
                                    ),
                                    colors = ButtonDefaults.buttonColors(
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.onBackground
                                    ),
                                    enabled = apiState is ApiState.Success,
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.size((windowInfo.containerSize.width / 22).dp)
                                ) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(R.drawable.add_friend),
                                        contentDescription = "Add friend",
                                        modifier = Modifier.size((windowInfo.containerSize.width / 42).dp)
                                    )
                                }
                            }
                        }
                    } else {
                        BackHandler {
                            isAdd = false
                        }
                        FriendAdd(
                            apiViewModel,
                            { isAdd = false },
                            navController
                        )
                    }
                }
            }
        }
        if(apiState !is ApiState.Success){
            Box(
                modifier = Modifier.fillMaxSize().offset(y = 32.dp),
                contentAlignment = Alignment.TopCenter
            ){
                Button(
                    onClick = {
                        val inta = Intent(context, LocationTrackingService::class.java).apply { action = Actions.STOP_TRACKING.name }
                        ContextCompat.startForegroundService(context, inta)
                        navController.navigate(Route.Login.route)
                        apiViewModel.setRoute(Route.Login.route)
                        apiViewModel.isFirst = true
                        apiViewModel.clearData()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(
                        text = "Log out",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
    }
    DisposableEffect(Unit) {
        mapView.onStart()
        onDispose {
            apiViewModel.editMapState(mapView.mapWindow.map.cameraPosition)
            mapView.onStop()
        }
    }
}