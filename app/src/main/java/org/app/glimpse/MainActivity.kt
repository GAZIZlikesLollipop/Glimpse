package org.app.glimpse

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.compose.rememberNavController
import com.yandex.mapkit.MapKitFactory
import org.app.glimpse.data.network.ApiService
import org.app.glimpse.data.network.ApiViewModel
import org.app.glimpse.data.network.ApiViewModelFactory
import org.app.glimpse.data.repository.ApiRepository
import org.app.glimpse.data.repository.JWTRepository
import org.app.glimpse.pressentation.theme.GlimpseTheme

val Context.jwtPreferences by preferencesDataStore(
    "jwtPreferences"
)
class MainActivity : ComponentActivity() {
    lateinit var apiViewModel: ApiViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val apiRepository = ApiRepository(ApiService.httpClient)
            val jwtRepository = JWTRepository(this)
            apiViewModel = viewModels<ApiViewModel>{
                ApiViewModelFactory(apiRepository,jwtRepository)
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