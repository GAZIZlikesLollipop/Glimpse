package org.app.glimpse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.yandex.mapkit.MapKitFactory
import org.app.glimpse.data.network.ApiViewModel
import org.app.glimpse.data.network.ApiViewModelFactory
import org.app.glimpse.pressentation.theme.GlimpseTheme

class MainActivity : ComponentActivity() {
    lateinit var apiViewModel: ApiViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            apiViewModel = viewModels<ApiViewModel>{
                ApiViewModelFactory(
                    (application as MyApplication).apiRepository,
                    (application as MyApplication).userPreferencesRepository,
                    (application as MyApplication).userDataRepository
                )
            }.value
            val navController = rememberNavController()
            GlimpseTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ){ paddingValues ->
                    Navigation(
                        navController = navController,
                        padding = paddingValues
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