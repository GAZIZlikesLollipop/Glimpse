package org.app.glimpse.pressentation.screen

import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.valentinilk.shimmer.shimmer
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import org.app.glimpse.R
import org.app.glimpse.Route
import org.app.glimpse.data.network.ApiState
import org.app.glimpse.data.network.ApiViewModel
import org.app.glimpse.data.network.FriendUser
import org.app.glimpse.data.network.User
import org.app.glimpse.pressentation.components.createUnsafeOkHttpClient
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.ExperimentalTime

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@ExperimentalTime
@Composable
fun ProfileScreen(
    userId: Long,
    navController: NavController,
    paddingValues: PaddingValues,
    apiViewModel: ApiViewModel
){
    val apiState by apiViewModel.userData.collectAsState()
    val windowInfo = LocalWindowInfo.current
    if(apiState is ApiState.Success) {
        var isUser by rememberSaveable { mutableStateOf(false) }
        val settings = stringArrayResource(R.array.settings)
        var isEdit by rememberSaveable { mutableStateOf(false) }
        val data = (apiState as ApiState.Success).data as User
        var isFriends by rememberSaveable { mutableStateOf(false) }
        var isDeleteAccount by rememberSaveable { mutableStateOf(false) }
        val userLang by apiViewModel.userLang.collectAsState()
        val userData: FriendUser = when {
            userId == data.id -> {
                isUser = true
                FriendUser(
                    id = data.id,
                    name = data.name,
                    avatar = data.avatar,
                    bio = data.bio,
                    latitude = data.latitude,
                    longitude = data.longitude,
                    friends = data.friends,
                    createdAt = data.createdAt,
                    updatedAt = data.updatedAt
                )
            }

            else -> {
                data.friends.find { it.id == userId } ?:
                data.friends.find { ff -> ff.friends!!.find { it.id == userId } != null }!!
            }
        }

        val geocodeState by apiViewModel.geocoderState.collectAsState()
        val cnt = stringArrayResource(R.array.profile_cnt)
        val context = LocalContext.current
        val geocoder = Geocoder(context, Locale.getDefault())
        val friendsLocations = remember {
            mutableStateListOf(*Array(userData.friends?.size ?: 0) { "" }.toList().toTypedArray())
        }

        LaunchedEffect(Unit) {
            if (userId == userData.id) {
                apiViewModel.getLocation(userData.longitude, userData.latitude)
                userData.friends?.forEachIndexed { i, addr ->
                    geocoder.getFromLocation(
                        addr.latitude, addr.longitude, 1,
                        object : Geocoder.GeocodeListener {
                            override fun onGeocode(list: List<Address?>) {
                                val it = list[0]
                                friendsLocations[i] = it?.thoroughfare ?: it?.subLocality ?: it?.locality ?: it?.subAdminArea ?: it?.adminArea ?: it?.countryName ?: "Mars"
                            }

                            override fun onError(errorMessage: String?) {
                                super.onError(errorMessage)
                                Log.e("Geocoder", errorMessage ?: "")
                            }
                        })
                }
            } else {
                val friend = userData.friends?.find { it.id == userId }
                apiViewModel.getLocation(friend?.longitude ?: 0.0, friend?.latitude ?: 0.0)
                friend?.friends?.forEachIndexed { i, it ->
                    geocoder.getFromLocation(
                        it.longitude, it.latitude, 1,
                        object : Geocoder.GeocodeListener {
                            override fun onGeocode(list: List<Address?>) {
                                list.forEach {
                                    friendsLocations[i] = it?.thoroughfare ?: it?.subLocality ?: it?.locality ?: it?.subAdminArea ?: it?.adminArea ?: it?.countryName ?: "Mars"
                                }
                            }

                            override fun onError(errorMessage: String?) {
                                super.onError(errorMessage)
                                Log.e("Geocoder", errorMessage ?: "")
                            }
                        })
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = paddingValues.calculateTopPadding())
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { navController.popBackStack() },
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            MaterialTheme.colorScheme.surfaceContainerHigh,
                            MaterialTheme.colorScheme.onBackground
                        ),
                        modifier = Modifier.size((windowInfo.containerSize.width / 26).dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                    if (isUser) {
                        Button(
                            onClick = { isEdit = true },
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                MaterialTheme.colorScheme.surfaceContainerHigh,
                                MaterialTheme.colorScheme.onBackground
                            ),
                            modifier = Modifier.size((windowInfo.containerSize.width / 26).dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = "Back"
                            )
                        }
                    }
                }
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                AsyncImage(
                                    model = userData.avatar,
                                    imageLoader = ImageLoader.Builder(context)
                                        .components { add(OkHttpNetworkFetcherFactory( createUnsafeOkHttpClient())) }
                                        .build(),
                                    contentDescription = null,
                                    contentScale = ContentScale.FillBounds,
                                    modifier = Modifier.size((windowInfo.containerSize.width / 5).dp)
                                        .clip(RoundedCornerShape(20.dp)),
                                )
                                if (userData.createdAt != userData.updatedAt && userData.createdAt.toEpochMilliseconds() != userData.updatedAt.toEpochMilliseconds()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.End,
                                        modifier = Modifier.fillMaxWidth()
                                            .padding(horizontal = 16.dp)
                                            .offset(y = 8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Edit,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onBackground.copy(0.8f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = userData.createdAt.toLocalDateTime(TimeZone.currentSystemDefault())
                                                .toJavaLocalDateTime().format(
                                                    DateTimeFormatter.ofPattern(
                                                        "yyyy.MM.dd",
                                                        Locale.getDefault()
                                                    )
                                                ),
                                            fontWeight = FontWeight.W500,
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.onBackground.copy(0.8f)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = userData.name,
                                fontWeight = FontWeight.W600,
                                fontSize = 42.sp
                            )
                            Text(
                                text = userData.bio,
                                fontWeight = FontWeight.W700,
                                fontSize = 30.sp
                            )
                            Text(
                                text = "${cnt[1]} ${
                                    userData.createdAt.toLocalDateTime(TimeZone.currentSystemDefault())
                                        .toJavaLocalDateTime().format(
                                        DateTimeFormatter.ofPattern(
                                            "yyyy dd MMMM",
                                            Locale.getDefault()
                                        )
                                    )
                                }",
                                fontWeight = FontWeight.W600,
                                fontSize = 24.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(0.75f),
                                textAlign = TextAlign.Center
                            )
                            if (geocodeState is ApiState.Success) {
                                Text(
                                    (geocodeState as ApiState.Success).data.toString(),
                                    color = MaterialTheme.colorScheme.onBackground.copy(0.5f),
                                    fontWeight = FontWeight.W600,
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                            if (geocodeState is ApiState.Loading || geocodeState is ApiState.Error) {
                                CircularProgressIndicator()
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                    }
                    item {
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isFriends = !isFriends },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "${cnt[0]}: ${userData.friends?.size}",
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = MaterialTheme.colorScheme.onBackground.copy(0.7f),
                                    modifier = Modifier.padding(16.dp),
                                    fontWeight = FontWeight.W500
                                )
                                Icon(
                                    imageVector = if (isFriends) Icons.Rounded.KeyboardArrowDown else Icons.Rounded.KeyboardArrowUp,
                                    contentDescription = null,
                                    modifier = Modifier.size(36.dp),
                                    tint = MaterialTheme.colorScheme.onBackground.copy(0.7f)
                                )
                            }
                            HorizontalDivider()
                            if (userData.friends != null) {
                                AnimatedVisibility(
                                    visible = isFriends,
                                    enter = slideInHorizontally(tween(300,50),{-it}),
                                    exit = slideOutHorizontally(tween(300,50),{-it}),
                                ) {
                                    Column {
                                        userData.friends.forEachIndexed { i, f ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        if (f.friends == null) {
                                                            apiViewModel.getFriendFriends(f.id)
                                                            navController.navigate(
                                                                Route.Profile.createRoute(
                                                                    f.id
                                                                )
                                                            )
                                                        } else {
                                                            navController.navigate(
                                                                Route.Profile.createRoute(
                                                                    f.id
                                                                )
                                                            )
                                                        }
                                                    },
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Box(Modifier.weight(0.7f).padding(12.dp)) {
                                                    AsyncImage(
                                                        model = f.avatar,
                                                        contentDescription = null,
                                                        imageLoader = ImageLoader.Builder(context)
                                                            .components { add(OkHttpNetworkFetcherFactory( createUnsafeOkHttpClient())) }
                                                            .build(),
                                                        contentScale = ContentScale.FillBounds,
                                                        modifier = Modifier
                                                            .size((windowInfo.containerSize.width / 17).dp)
                                                            .clip(RoundedCornerShape(16.dp))
                                                    )
                                                }
                                                Text(
                                                    text = f.name,
                                                    fontWeight = FontWeight.W500,
                                                    modifier = Modifier.width((windowInfo.containerSize.width / 18).dp),
                                                    overflow = TextOverflow.Ellipsis,
                                                    textAlign = TextAlign.Center,
                                                    style = MaterialTheme.typography.titleLarge
                                                )
                                                Text(
                                                    friendsLocations[i],
                                                    textAlign = TextAlign.Center,
                                                    color = MaterialTheme.colorScheme.onBackground.copy(
                                                        0.75f
                                                    ),
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                            if (i != userData.friends.size - 1) {
                                                HorizontalDivider(thickness = 2.dp)
                                            }
                                            Spacer(Modifier.height(paddingValues.calculateBottomPadding()))
                                        }
                                    }
                                }
                            } else {
                                (0..5).toList().forEach {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().clickable{},
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(Modifier.weight(1f).padding(12.dp)) {
                                            Box(
                                                modifier = Modifier
                                                    .shimmer()
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .size((windowInfo.containerSize.width / 16).dp)
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
                                                    .weight(1f)
                                                    .height(30.dp)
                                                    .background(MaterialTheme.colorScheme.onBackground.copy(0.5f))
                                            )
                                        }
                                    }
                                    if (it != 5) {
                                        HorizontalDivider(thickness = 2.dp)
                                    }
                                    Spacer(Modifier.height(paddingValues.calculateBottomPadding()))
                                }
                            }
                        }
                        Text(
                            cnt[2],
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onBackground.copy(0.7f),
                            modifier = Modifier.padding(16.dp),
                            fontWeight = FontWeight.W500
                        )
                    }
                    items(settings) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    when(it){
                                        settings[1] -> {
                                            navController.navigate(Route.Login.route)
                                            apiViewModel.setRoute(Route.Login.route)
                                        }
                                        settings[2] -> isDeleteAccount = true
                                        else -> if(userLang == "en") apiViewModel.setUserLang("ru") else apiViewModel.setUserLang("en")
                                    }
                                }
                        ){
                            val color = if(it == settings[0]) MaterialTheme.colorScheme.onBackground.copy(0.5f) else MaterialTheme.colorScheme.error.copy(0.5f)
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Icon(
                                    imageVector =
                                        when(it){
                                            settings[1] -> Icons.AutoMirrored.Rounded.ExitToApp
                                            settings[2] -> Icons.Rounded.Delete
                                            else -> ImageVector.vectorResource(R.drawable.language)
                                        },
                                    contentDescription = null,
                                    tint = color
                                )
                                Text(
                                    if(it == settings[0]) "${it}: $userLang" else it,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = color
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = color
                                )
                            }
                            if(it != settings[2]) HorizontalDivider(color = color)
                        }
                    }
                    item {
                        Spacer(Modifier.height(20.dp))
                    }
                }
            }
            if(isDeleteAccount){
                AlertDialog(
                    onDismissRequest = {
                        isDeleteAccount = false
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                isDeleteAccount = false
                                navController.navigate(Route.Login.route)
                                apiViewModel.deleteAccount()
                            },
                            colors = ButtonDefaults.buttonColors(
                                MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(
                                cnt[4]
                            )
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = { isDeleteAccount = false }
                        ) {
                            Text(
                                cnt[5]
                            )
                        }
                    },
                    title = {
                        Text(cnt[3])
                    },
                    modifier = Modifier.padding(16.dp)
                )
            }
            AnimatedVisibility(isEdit) {
                
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(top = paddingValues.calculateTopPadding()).padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { navController.popBackStack() },
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        MaterialTheme.colorScheme.surfaceContainerHigh,
                        MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier.size((windowInfo.containerSize.width / 26).dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                Box(
                    modifier = Modifier
                        .shimmer()
                        .clip(RoundedCornerShape(20.dp))
                        .size((windowInfo.containerSize.width / 26).dp)
                        .background(MaterialTheme.colorScheme.onBackground.copy(0.35f))
                )
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .shimmer()
                        .clip(RoundedCornerShape(20.dp))
                        .size((windowInfo.containerSize.width / 5).dp)
                        .background(MaterialTheme.colorScheme.onBackground.copy(0.5f))
                )

                Box(
                    modifier = Modifier
                        .shimmer()
                        .fillMaxWidth()
                        .padding(horizontal = 95.dp)
                        .offset(y = 8.dp, x = (windowInfo.containerSize.width / 25).dp)
                        .height(24.dp)
                        .width((windowInfo.containerSize.width / 5).dp)
                        .background(MaterialTheme.colorScheme.onBackground.copy(0.5f))
                )
            }

            Box(
                modifier = Modifier
                    .shimmer()
                    .fillMaxWidth()
                    .padding(horizontal = 42.dp)
                    .offset(y = 8.dp)
                    .height(50.dp)
                    .width((windowInfo.containerSize.width/5).dp)
                    .background(MaterialTheme.colorScheme.onBackground.copy(0.5f))
            )

            Box(
                modifier = Modifier
                    .shimmer()
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .offset(y = 8.dp)
                    .height(36.dp)
                    .width((windowInfo.containerSize.width/5).dp)
                    .background(MaterialTheme.colorScheme.onBackground.copy(0.5f))
            )
            Box(
                modifier = Modifier
                    .shimmer()
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .offset(y = 8.dp)
                    .height(32.dp)
                    .width((windowInfo.containerSize.width/5).dp)
                    .background(MaterialTheme.colorScheme.onBackground.copy(0.5f))
            )
            Box(
                modifier = Modifier
                    .shimmer()
                    .fillMaxWidth()
                    .padding(horizontal = 50.dp)
                    .offset(y = 8.dp)
                    .height(32.dp)
                    .width((windowInfo.containerSize.width/5).dp)
                    .background(MaterialTheme.colorScheme.onBackground.copy(0.5f))
            )
        }
    }
}