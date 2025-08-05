package org.app.glimpse.pressentation.screen

import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Edit
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import kotlinx.datetime.toJavaLocalDateTime
import org.app.glimpse.R
import org.app.glimpse.data.network.ApiState
import org.app.glimpse.data.network.ApiViewModel
import org.app.glimpse.data.network.User
import org.app.glimpse.pressentation.components.SettingsCard
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
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
        val settings = stringArrayResource(R.array.settings)
        var isEdit by rememberSaveable { mutableStateOf(false) }
        val userData = (apiState as ApiState.Success).data as User
        val geocodeState by apiViewModel.geocoderState.collectAsState()
        val cnt = stringArrayResource(R.array.profile_cnt)
        val context = LocalContext.current
        val geocoder = Geocoder(context, Locale.getDefault())
        val friendsLocations = remember {
            mutableStateListOf(
                *Array(userData.friends.size) { "" }.toList().toTypedArray())
        }

        LaunchedEffect(Unit) {
            if (userId == userData.id) {
                apiViewModel.getLocation(userData.longitude, userData.latitude)
                userData.friends.forEachIndexed { i, addr ->
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
                val friend = userData.friends.find { it.id == userId }
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

        Column(
            modifier = Modifier.fillMaxSize(),
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
            LazyColumn(modifier = Modifier.weight(1f)) {
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
                                contentDescription = null,
                                contentScale = ContentScale.FillBounds,
                                modifier = Modifier.size((windowInfo.containerSize.width / 5).dp)
                                    .clip(RoundedCornerShape(20.dp)),
                            )
                            if (userData.createdAt.date != userData.updatedAt.date) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
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
                                        text = userData.createdAt.toJavaLocalDateTime().format(
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
                                userData.createdAt.toJavaLocalDateTime().format(
                                    DateTimeFormatter.ofPattern(
                                        "yyyy dd MMMM",
                                        Locale.getDefault()
                                    )
                                )
                            }",
                            fontWeight = FontWeight.W600,
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(0.75f)
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
                    Text(
                        cnt[2],
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(0.7f),
                        modifier = Modifier.padding(16.dp),
                        fontWeight = FontWeight.W500
                    )
                }
                itemsIndexed(settings) { i, it ->
                    SettingsCard(it)
                    if (i != settings.size - 1) {
                        HorizontalDivider()
                    }
                }
                item {
                    Text(
                        "${cnt[0]}: ${userData.friends.size}",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(0.7f),
                        modifier = Modifier.padding(16.dp),
                        fontWeight = FontWeight.W500
                    )
                }
                itemsIndexed(userData.friends) { i, f ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable {},
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(Modifier.weight(1f).padding(12.dp)) {
                            AsyncImage(
                                model = f.avatar,
                                contentDescription = null,
                                contentScale = ContentScale.FillBounds,
                                modifier = Modifier
                                    .size((windowInfo.containerSize.width / 16).dp)
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
                            color = MaterialTheme.colorScheme.onBackground.copy(0.75f),
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
        Column(
            modifier = Modifier.fillMaxSize(),
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
                Button(
                    onClick = {},
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        MaterialTheme.colorScheme.surfaceContainerHigh,
                        MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier.size((windowInfo.containerSize.width / 26).dp)
                ) {}
            }
            Box(
                modifier = Modifier.size((windowInfo.containerSize.width / 5).dp)
                    .clip(RoundedCornerShape(20.dp))
            )
            Box(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).offset(y = 8.dp).height(20.dp).width((windowInfo.containerSize.width/5).dp)
            )

            Box(Modifier
            )
//        Text(
//            text = userData.name,
//            fontWeight = FontWeight.W600,
//            fontSize = 42.sp
//        )
//        Text(
//            text = userData.bio,
//            fontWeight = FontWeight.W700,
//            fontSize = 30.sp
//        )
//        Text(
//            text = "${cnt[1]} ${
//                userData.createdAt.toJavaLocalDateTime().format(
//                    DateTimeFormatter.ofPattern(
//                        "yyyy dd MMMM",
//                        Locale.getDefault()
//                    )
//                )
//            }",
//            fontWeight = FontWeight.W600,
//            fontSize = 24.sp,
//            color = MaterialTheme.colorScheme.onBackground.copy(0.75f)
//        )
//        if (geocodeState is ApiState.Success) {
//            Text(
//                (geocodeState as ApiState.Success).data.toString(),
//                color = MaterialTheme.colorScheme.onBackground.copy(0.5f),
//                fontWeight = FontWeight.W600,
//                style = MaterialTheme.typography.titleLarge,
//                modifier = Modifier.padding(horizontal = 16.dp)
//            )
//        }
        }
    }
}