package org.app.glimpse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.yandex.mapkit.MapKitFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.app.glimpse.data.network.ApiViewModel
import org.app.glimpse.data.network.ApiViewModelFactory
import org.app.glimpse.data.network.UpdateUser
import org.app.glimpse.pressentation.theme.GlimpseTheme

class MainActivity : ComponentActivity() {
    lateinit var apiViewModel: ApiViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val apiRepository = (application as MyApplication).apiRepository
            apiViewModel = viewModels<ApiViewModel>{
                ApiViewModelFactory(
                    apiRepository,
                    (application as MyApplication).userPreferencesRepository,
                    (application as MyApplication).userDataRepository
                )
            }.value
            val scope = rememberCoroutineScope()
            val viewModel = viewModel<ApiViewModel>()
            val token by viewModel.token.collectAsState()
            val navController = rememberNavController()
            LaunchedEffect(Unit) {
                scope.launch {
                    while(true){
                        if(token.isNotBlank()) {
                            apiRepository.updateUserData(token, UpdateUser(lastOnline = 0))
                        }
                        delay(1000)
                    }
                }
            }
            GlimpseTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ){ paddingValues ->
                    Navigation(
                        navController = navController,
                        padding = paddingValues,
                        apiViewModel = viewModel
                    )
                }
            }

        }
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
    }

    override fun onStop() {
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }
}
