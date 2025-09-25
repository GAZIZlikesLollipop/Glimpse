package org.app.glimpse.pressentation.components

import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.app.glimpse.R
import org.app.glimpse.data.network.ApiState
import org.app.glimpse.data.network.ApiViewModel
import org.app.glimpse.data.network.User
import org.app.glimpse.data.network.Users
import java.util.Locale
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun FriendAdd(
    apiViewModel: ApiViewModel,
    onBack: () -> Unit
){
    var searchText by rememberSaveable { mutableStateOf("") }
    val searched = remember { mutableStateListOf<Users>() }
    val cnt = stringArrayResource(R.array.main_cnt)
    val windowInfo = LocalWindowInfo.current
    val apiState by apiViewModel.userData.collectAsState()
    val userData = (apiState as ApiState.Success).data as User

    LaunchedEffect(Unit) {
        apiViewModel.getUserNames()
    }
    LaunchedEffect(searchText) {
        searched.clear()
        apiViewModel.userNames.forEach {
            if(
                searchText.isNotBlank() &&
                it.name.lowercase().trim().contains(searchText.lowercase().trim())
                && userData.id != it.id
                && userData.friends.find { fu -> fu.id == it.id } == null
            ) {
                searched.add(it)
            }
        }
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Row(
            verticalAlignment = Alignment.CenterVertically
        ){
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = { onBack() },
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
            Spacer(Modifier.width(8.dp))
            TextField(
                value = searchText,
                onValueChange = { searchText = it },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "search",
                        modifier = Modifier.size(24.dp)
                    )
                },
                placeholder = {
                    Text(
                        text = cnt[2]
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent,
                    errorBorderColor = Color.Transparent
                ),
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .offset(x = (-4).dp)
                    .border(
                    2.dp,
                    MaterialTheme.colorScheme.onBackground,
                    RoundedCornerShape(36.dp)
                )
            )
        }
        if(searched.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(searched) {
                    if(userData.friends.find { fu -> fu.id == it.id } == null) {
                        FriendCard(
                            it,
                            it != searched.last(),
                            apiViewModel
                        )
                    }
                }
            }
        } else {
            Text(
                text = cnt[3],
                fontSize = 36.sp,
                fontWeight = FontWeight.W600,
                modifier = Modifier.fillMaxSize().offset(y = (windowInfo.containerSize.height/12).dp),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(0.5f)
            )
        }
    }
}

@Composable
fun FriendCard(
    f: Users,
    isDiv: Boolean,
    apiViewModel: ApiViewModel
){
    val context = LocalContext.current
    val geocoder = Geocoder(context, Locale.getDefault())
    var friendLocation by rememberSaveable { mutableStateOf("") }
    val windowInfo = LocalWindowInfo.current
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(f.latitude,f.longitude,1, object: Geocoder.GeocodeListener {
                override fun onGeocode(list: List<Address?>) {
                    val it = list[0]
                    friendLocation = it?.thoroughfare ?: it?.subLocality ?: it?.locality ?: it?.subAdminArea ?: it?.adminArea ?: it?.countryName ?: "Mars"
                }

                override fun onError(errorMessage: String?) {
                    super.onError(errorMessage)
                    Log.e("GEOCODER", errorMessage ?: "")
                }
            })
        }
    }
    Column (
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .width(windowInfo.containerSize.width.dp).height(100.dp)
                .clickable { apiViewModel.addFriend(f.id) },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy((windowInfo.containerSize.width/80).dp)
        ){
            AsyncImage(
                model = f.avatar,
                contentDescription = f.name,
                modifier = Modifier.clip(RoundedCornerShape(24.dp)),
                contentScale = ContentScale.Fit
            )
            Text(
                text = f.name,
                fontWeight = FontWeight.W600,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = friendLocation,
                color = MaterialTheme.colorScheme.onBackground.copy(0.5f),
                fontWeight = FontWeight.W500
            )
        }
        if(isDiv) HorizontalDivider()
    }
}