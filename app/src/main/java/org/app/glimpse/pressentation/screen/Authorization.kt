@file:OptIn(ExperimentalPermissionsApi::class)

package org.app.glimpse.pressentation.screen

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import org.app.glimpse.R
import org.app.glimpse.Route
import org.app.glimpse.data.LocationTrackingService
import org.app.glimpse.data.network.ApiState
import org.app.glimpse.data.network.ApiViewModel

@Composable
fun RegisterScreen(
    navController: NavController,
    apiViewModel: ApiViewModel
){
    val focusManager = LocalFocusManager.current
    val cnt = stringArrayResource(R.array.signUp_cnt)
    var loginField by rememberSaveable { mutableStateOf("") }
    var passwordField by rememberSaveable { mutableStateOf("") }
    var aboutField by rememberSaveable { mutableStateOf("") }
    val apiState by apiViewModel.userData.collectAsState()
    var isShow by rememberSaveable { mutableStateOf(false) }
    var avatarField by remember { mutableStateOf<Bitmap?>(null) }
    var extField by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current
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

    var isChange by rememberSaveable { mutableStateOf(false) }

    BackHandler {
        if(isChange) {
            isChange = false
        } else {
            navController.popBackStack()
        }
    }

    val permissionRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {}

    val hasFinePermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION).status.isGranted

    val hasBackPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION).status.isGranted

    val hasNotifPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION).status.isGranted

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                cnt[0],
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(36.dp))
            OutlinedTextField(
                enabled = apiState is ApiState.Error || apiState is ApiState.Initial,
                isError = apiState is ApiState.Error,
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
                        tint =
                            when (apiState) {
                                is ApiState.Loading -> MaterialTheme.colorScheme.onBackground.copy(0.45f)
                                is ApiState.Error -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onBackground
                            }
                    )
                },
                placeholder = {
                    Text(
                        cnt[3],
                        color =
                            when (apiState) {
                                is ApiState.Loading -> MaterialTheme.colorScheme.onBackground.copy(0.45f)
                                is ApiState.Error -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onBackground
                            }
                    )
                }
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                enabled = apiState is ApiState.Error || apiState is ApiState.Initial,
                isError = apiState is ApiState.Error,
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
                        tint =
                            when (apiState) {
                                is ApiState.Loading -> MaterialTheme.colorScheme.onBackground.copy(0.45f)
                                is ApiState.Error -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onBackground
                            }
                    )
                },
                placeholder = {
                    Text(
                        cnt[4],
                        color =
                            when (apiState) {
                                is ApiState.Loading -> MaterialTheme.colorScheme.onBackground.copy(0.45f)
                                is ApiState.Error -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onBackground
                            }
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
                            tint =
                                when (apiState) {
                                    is ApiState.Loading -> MaterialTheme.colorScheme.onBackground.copy(0.45f)
                                    is ApiState.Error -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.onBackground
                                }
                        )
                    }
                }
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                enabled = apiState is ApiState.Error || apiState is ApiState.Initial,
                isError = apiState is ApiState.Error,
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
                        tint =
                            when (apiState) {
                                is ApiState.Loading -> MaterialTheme.colorScheme.onBackground.copy(0.45f)
                                is ApiState.Error -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onBackground
                            }
                    )
                },
                placeholder = {
                    Text(
                        cnt[5],
                        color =
                            when (apiState) {
                                is ApiState.Loading -> MaterialTheme.colorScheme.onBackground.copy(0.45f)
                                is ApiState.Error -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onBackground
                            }
                    )
                }
            )
            Spacer(Modifier.height(16.dp))
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = if(apiState is ApiState.Error || apiState is ApiState.Initial) {
                    Modifier.clickable {
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
                        .padding(horizontal = 20.dp)
                } else {
                    Modifier.padding(horizontal = 20.dp)
                },
                border = BorderStroke(
                    1.dp,
                    when (apiState) {
                        is ApiState.Loading -> MaterialTheme.colorScheme.onBackground.copy(0.15f)
                        is ApiState.Error -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onBackground
                    }
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
                        tint =
                            when (apiState) {
                                is ApiState.Loading -> MaterialTheme.colorScheme.onBackground.copy(0.45f)
                                is ApiState.Error -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onBackground
                            }
                    )
                    Box(Modifier.width(16.dp))
                    Text(
                        cnt[6],
                        color =
                            when (apiState) {
                                is ApiState.Loading -> MaterialTheme.colorScheme.onBackground.copy(0.5f)
                                is ApiState.Error -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onBackground
                            }
,
                        fontWeight = FontWeight.W400,
                        fontSize = 18.sp
                    )
                    if(avatarField != null){
                        Box(Modifier.weight(1f))
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint =
                                when (apiState) {
                                    is ApiState.Loading -> MaterialTheme.colorScheme.onBackground.copy(0.45f)
                                    is ApiState.Error -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.onBackground
                                }
                        )
                    } else {
                        Box(Modifier.weight(1f))
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            AnimatedVisibility(
                visible = apiState is ApiState.Error,
            ) {
                Text(
                    cnt[7],
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            if (apiState is ApiState.Error) {
                Spacer(Modifier.height(24.dp))
            }
            Button(
                onClick = {
                    focusManager.clearFocus()
                    if(!hasFinePermission || !hasBackPermission || !hasNotifPermission){
                        if(!hasFinePermission){
                            permissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                        if(!hasBackPermission) {
                            permissionRequest.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        }
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            if (!hasNotifPermission) {
                                permissionRequest.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    } else {
                        LocationServices.getFusedLocationProviderClient(context).lastLocation.addOnSuccessListener { location ->
                            apiViewModel.signUp(
                                loginField,
                                passwordField,
                                aboutField,
                                avatarField,
                                extField,
                                location.latitude,
                                location.longitude
                            )
                        }
                        if(apiState is ApiState.Success) {
                            val inta = Intent(context, LocationTrackingService::class.java).apply {
                                action = LocationTrackingService.Actions.START_TRACKING.name
                            }
                            ContextCompat.startForegroundService(context, inta)
                        }
                    }
                },
                enabled = loginField.isNotBlank() && passwordField.isNotBlank() && aboutField.isNotBlank() && apiState !is ApiState.Loading && avatarField != null,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            ) {
                Text(
                    cnt[1],
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            Spacer(Modifier.height(20.dp))
            TextButton(
                onClick = {
                    navController.navigate(Route.Login.route)
                }
            ) {
                Text(
                    cnt[2],
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
        if(isChange) {
            Box(
                modifier = Modifier
                    .pointerInput(Unit){
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
                                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
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
}

@Composable
fun LoginScreen(
    navController: NavController,
    apiViewModel: ApiViewModel
){
    val focusManager = LocalFocusManager.current
    val cnt = stringArrayResource(R.array.login_cnt)
    var loginField by rememberSaveable { mutableStateOf("") }
    var passwordField by rememberSaveable { mutableStateOf("") }
    val apiState by apiViewModel.userData.collectAsState()
    var isShow by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val permissionRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {}

    val hasFinePermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION).status.isGranted

    val hasBackPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION).status.isGranted

    val hasNotifPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION).status.isGranted

    LaunchedEffect(Unit) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasNotifPermission) {
                permissionRequest.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if(!hasFinePermission){
            permissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if(!hasBackPermission) {
            permissionRequest.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
    }

    LaunchedEffect(apiState) {
        if(apiState is ApiState.Success){
            navController.navigate(Route.Main.route)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        Text(
            cnt[0],
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(50.dp))
        OutlinedTextField(
            enabled = apiState is ApiState.Error || apiState is ApiState.Initial,
            isError = apiState is ApiState.Error,
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
                    tint =
                        when (apiState) {
                            is ApiState.Loading -> MaterialTheme.colorScheme.onBackground.copy(0.5f)
                            is ApiState.Error -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onBackground
                        }
                )
            },
            placeholder = {
                Text(
                    cnt[3],
                    color =
                        when (apiState) {
                            is ApiState.Loading -> MaterialTheme.colorScheme.onBackground.copy(0.5f)
                            is ApiState.Error -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onBackground
                        }

                )
            }
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            enabled = apiState is ApiState.Error || apiState is ApiState.Initial,
            isError = apiState is ApiState.Error,
            value = passwordField,
            onValueChange = { passwordField = it },
            visualTransformation = if(isShow) VisualTransformation.None else PasswordVisualTransformation(),
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
                    tint =
                        when (apiState) {
                            is ApiState.Loading -> MaterialTheme.colorScheme.onBackground.copy(0.5f)
                            is ApiState.Error -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onBackground
                        }
                )
            },
            placeholder = {
                Text(
                    cnt[4],
                    color =
                        when (apiState) {
                            is ApiState.Loading -> MaterialTheme.colorScheme.onBackground.copy(0.5f)
                            is ApiState.Error -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onBackground
                        }
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = {isShow = !isShow}
                ) {
                    Icon(
                        imageVector = if (!isShow) ImageVector.vectorResource(R.drawable.visibility) else ImageVector.vectorResource(
                            R.drawable.visibility_off
                        ),
                        contentDescription = null,
                    )
                }
            }
        )
        Spacer(Modifier.height(24.dp))
        AnimatedVisibility(
            visible = apiState is ApiState.Error,
        ) {
            Text(
                cnt[5],
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        if(apiState is ApiState.Error) {
            Spacer(Modifier.height(24.dp))
        }
        Button(
            onClick = {
                focusManager.clearFocus()
                if(!hasFinePermission || !hasBackPermission || !hasNotifPermission){
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (!hasNotifPermission) {
                            permissionRequest.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                    if(!hasFinePermission){
                        permissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                    if(!hasBackPermission) {
                        permissionRequest.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    }
                } else {
                    apiViewModel.signIn(loginField,passwordField)
                    if(apiState is ApiState.Success) {
                        val inta = Intent(context, LocationTrackingService::class.java).apply {
                            action = LocationTrackingService.Actions.START_TRACKING.name
                        }
                        ContextCompat.startForegroundService(context,inta)
                    }
                }
                Log.d("HELLO","$hasNotifPermission, $hasFinePermission, $hasBackPermission")
            },
            enabled = loginField.isNotBlank() && passwordField.isNotBlank() && apiState !is ApiState.Loading,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
        ) {
            Text(
                cnt[1],
                style = MaterialTheme.typography.headlineMedium
            )
        }
        Spacer(Modifier.height(20.dp))
        TextButton(
            onClick = {navController.navigate(Route.Register.route)}
        ) {
            Text(
                cnt[2],
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}