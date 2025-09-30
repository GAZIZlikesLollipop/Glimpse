package org.app.glimpse.pressentation.screen

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.valentinilk.shimmer.shimmer
import org.app.glimpse.R
import org.app.glimpse.Route
import org.app.glimpse.data.network.ApiState
import org.app.glimpse.data.network.ApiViewModel
import org.app.glimpse.data.network.FriendUser
import org.app.glimpse.data.network.UpdateUser
import org.app.glimpse.data.network.User
import org.app.glimpse.pressentation.components.createUnsafeOkHttpClient
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
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
        var loginField by rememberSaveable { mutableStateOf("") }
        var passwordField by rememberSaveable { mutableStateOf("") }
        var isShow by rememberSaveable { mutableStateOf(false) }
        var isUser by rememberSaveable { mutableStateOf(false) }
        val settings = stringArrayResource(R.array.settings)
        var isEdit by rememberSaveable { mutableStateOf(false) }
        val data = (apiState as ApiState.Success).data as User
        var isFriends by rememberSaveable { mutableStateOf(false) }
        var isDeleteAccount by rememberSaveable { mutableStateOf(false) }
        val userLang by apiViewModel.userLang.collectAsState()
        var isChange by rememberSaveable { mutableStateOf(false) }
        val userData: FriendUser? = when {
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
                data.friends.find { ff -> ff.friends!!.find { it.id == userId } != null }
            }
        }
        var avatarField by remember { mutableStateOf<Bitmap?>(null) }

        val geocodeState by apiViewModel.geocoderState.collectAsState()
        val cnt = stringArrayResource(R.array.profile_cnt)
        val context = LocalContext.current
        val geocoder = Geocoder(context, Locale.getDefault())
        val focusManager = LocalFocusManager.current
        if(userData != null) {
        val friendsLocations = remember {
            mutableStateListOf(*Array(userData.friends?.size ?: 0) { "" }.toList().toTypedArray())
        }
        var extField by rememberSaveable { mutableStateOf("") }
        val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) {
            val uri = it
            if(uri != null){
                context.contentResolver.openInputStream(uri).use { o ->
                    val mimeTypeMap = MimeTypeMap.getSingleton()
                    val mimeType = context.contentResolver.getType(uri)
                    extField = mimeTypeMap.getExtensionFromMimeType(mimeType) ?: "png"
                    avatarField = BitmapFactory.decodeStream(o)
                }
            }
        }

        var aboutField by rememberSaveable { mutableStateOf("") }

        BackHandler {
            if(isChange) {
                isChange = false
            } else {
                navController.popBackStack()
            }
            if(!isEdit) {
                navController.popBackStack()
            } else {
                isEdit = false
            }
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
                    verticalArrangement = Arrangement.spacedBy(if(isEdit) 0.dp else 20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = paddingValues.calculateTopPadding())
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = {
                                if (!isEdit) navController.popBackStack() else isEdit = false
                            },
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
                        } else {
                            if (data.friends.find { it.id == userId } != null) {
                                Button(
                                    onClick = {
                                        navController.navigate(Route.Main.route)
                                        apiViewModel.deleteFriend(userId)
                                    },
                                    shape = RoundedCornerShape(20.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        MaterialTheme.colorScheme.surfaceContainerHigh,
                                        MaterialTheme.colorScheme.error
                                    ),
                                    modifier = Modifier.size((windowInfo.containerSize.width / 26).dp)
                                ) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(R.drawable.baseline_person_remove_24),
                                        contentDescription = "Delete friend"
                                    )
                                }
                            }
                        }
                    }
                    if (!isEdit) {
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
                                                .components {
                                                    add(
                                                        OkHttpNetworkFetcherFactory(
                                                            createUnsafeOkHttpClient()
                                                        )
                                                    )
                                                }
                                                .build(),
                                            contentDescription = null,
                                            contentScale = ContentScale.FillBounds,
                                            modifier = Modifier.size((windowInfo.containerSize.width / 5).dp)
                                                .clip(RoundedCornerShape(20.dp)),
                                        )
                                        if (userData.createdAt != userData.updatedAt) {
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
                                                    tint = MaterialTheme.colorScheme.onBackground.copy(
                                                        0.8f
                                                    ),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(Modifier.width(8.dp))
                                                Text(
                                                    text = LocalDateTime
                                                        .ofInstant(Instant.ofEpochMilli(userData.createdAt), ZoneId.systemDefault())
                                                        .format(
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
                                        fontSize = 42.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = userData.bio,
                                        fontWeight = FontWeight.W700,
                                        fontSize = 30.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "${cnt[1]} ${
                                            LocalDateTime.ofInstant(Instant.ofEpochMilli(userData.createdAt),ZoneId.systemDefault()).format(
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
                                if (userData.friends != null && userData.friends.isNotEmpty()) {
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
                                                "${cnt[0]}: ${userData.friends.size}",
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
                                        AnimatedVisibility(
                                            visible = isFriends,
                                            enter = slideInHorizontally(tween(300, 50), { -it }),
                                            exit = slideOutHorizontally(tween(300, 50), { -it }),
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
                                                                imageLoader = ImageLoader.Builder(
                                                                    context
                                                                )
                                                                    .components {
                                                                        add(
                                                                            OkHttpNetworkFetcherFactory(
                                                                                createUnsafeOkHttpClient()
                                                                            )
                                                                        )
                                                                    }
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
                                    }
                                } else {
                                    Text(
                                        "${userData.name} ${cnt[6]}",
                                        style = MaterialTheme.typography.headlineLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.W600
                                    )
                                }
                            }
                            item {
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
                                            when (it) {
                                                settings[1] -> {
                                                    navController.navigate(Route.Login.route)
                                                    apiViewModel.setRoute(Route.Login.route)
                                                }

                                                settings[2] -> isDeleteAccount = true
                                                else -> if (userLang == "en") apiViewModel.setUserLang("ru") else apiViewModel.setUserLang(
                                                    "en"
                                                )
                                            }
                                        }
                                ) {
                                    val color =
                                        if (it == settings[0]) MaterialTheme.colorScheme.onBackground.copy(
                                            0.75f
                                        ) else MaterialTheme.colorScheme.error.copy(0.75f)
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Icon(
                                            imageVector =
                                                when (it) {
                                                    settings[1] -> Icons.AutoMirrored.Rounded.ExitToApp
                                                    settings[2] -> Icons.Rounded.Delete
                                                    else -> ImageVector.vectorResource(R.drawable.language)
                                                },
                                            contentDescription = null,
                                            tint = color
                                        )
                                        Text(
                                            if (it == settings[0]) "${it}: $userLang" else it,
                                            style = MaterialTheme.typography.titleLarge,
                                            color = color
                                        )
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                            contentDescription = null,
                                            tint = color
                                        )
                                    }
                                    if (it != settings[2]) HorizontalDivider(color = color)
                                }
                            }
                            item {
                                Spacer(Modifier.height(20.dp))
                            }
                        }
                        if (isDeleteAccount) {
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

                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            Spacer(Modifier.height(0.dp))
                            Text(
                                text = cnt[12],
                                style = MaterialTheme.typography.displayMedium
                            )
                            Spacer(Modifier.height(0.dp))
                            OutlinedTextField(
                                value = loginField,
                                onValueChange = { loginField = it },
                                keyboardActions = KeyboardActions(
                                    onDone = { focusManager.clearFocus() }
                                ),
                                singleLine = true,
                                shape = RoundedCornerShape(20.dp),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onBackground
                                    )
                                },
                                placeholder = {
                                    Text(
                                        cnt[8],
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            )
                            OutlinedTextField(
                                value = passwordField,
                                onValueChange = { passwordField = it },
                                visualTransformation = if (isShow) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    keyboardType = KeyboardType.Password,
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = { focusManager.clearFocus() }
                                ),
                                singleLine = true,
                                shape = RoundedCornerShape(20.dp),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onBackground
                                    )
                                },
                                placeholder = {
                                    Text(
                                        cnt[9],
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                },
                                trailingIcon = {
                                    IconButton(
                                        onClick = { isShow = !isShow }
                                    ) {
                                        Icon(
                                            imageVector = if (!isShow) ImageVector.vectorResource(R.drawable.visibility) else ImageVector.vectorResource(
                                                R.drawable.visibility_off
                                            ),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                }
                            )
                            OutlinedTextField(
                                value = aboutField,
                                onValueChange = { aboutField = it },
                                keyboardActions = KeyboardActions(
                                    onDone = { focusManager.clearFocus() }
                                ),
                                singleLine = true,
                                shape = RoundedCornerShape(20.dp),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onBackground
                                    )
                                },
                                placeholder = {
                                    Text(
                                        cnt[10],
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            )
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                modifier =
                                    Modifier
                                        .clickable {
                                            if (avatarField != null) {
                                                isChange = true
                                            } else {
                                                photoPicker.launch(
                                                    PickVisualMediaRequest(
                                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                                    )
                                                )
                                            }
                                        }
                                        .padding(horizontal = 20.dp),
                                border = BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.onBackground
                                ),
                                colors = CardDefaults.cardColors(Color.Transparent)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(R.drawable.photo),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onBackground
                                    )
                                    Box(Modifier.width(16.dp))
                                    Text(
                                        cnt[11],
                                        color = MaterialTheme.colorScheme.onBackground,
                                        fontWeight = FontWeight.W400,
                                        fontSize = 18.sp
                                    )
                                    if (avatarField != null) {
                                        Box(Modifier.weight(1f))
                                        Icon(
                                            imageVector = Icons.Rounded.CheckCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onBackground
                                        )
                                    } else {
                                        Box(Modifier.weight(1f))
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    apiViewModel.updateUser(
                                        UpdateUser(
                                            loginField.ifBlank { null },
                                            passwordField.ifBlank { null },
                                            aboutField.ifBlank { null },
                                            avatarField,
                                            avatarExt = extField.ifBlank { null },
                                        )
                                    )
                                    loginField = ""
                                    passwordField = ""
                                    aboutField = ""
                                    avatarField = null
                                    extField = ""
                                },
                                enabled = (loginField.isNotBlank() || passwordField.isNotBlank() || aboutField.isNotBlank() || avatarField != null) && apiState !is ApiState.Loading,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
                            ) {
                                Text(
                                    cnt[7],
                                    style = MaterialTheme.typography.headlineMedium
                                )
                            }
                        }
                    }
                }
            }
            if (isChange) {
                Box(
                    modifier = Modifier
                        .pointerInput(Unit) {
                            detectTapGestures { isChange = false }
                        }
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(0.8f)),
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        AsyncImage(
                            model = avatarField,
                            contentDescription = null,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Column(
                                modifier = Modifier.clip(CircleShape).clickable {
                                    isChange = false
                                    avatarField = null
                                },
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(8.dp).size(36.dp)
                                )
                                Text(
                                    cnt[8],
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(8.dp),
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Column(
                                modifier = Modifier.clip(CircleShape).clickable {
                                    photoPicker.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                },
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Edit,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(8.dp).size(36.dp)
                                )
                                Text(
                                    cnt[9],
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(8.dp),
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        }
                    }
                }
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