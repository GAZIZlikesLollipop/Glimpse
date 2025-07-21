package org.app.glimpse

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.app.glimpse.data.ApiViewModel
import org.app.glimpse.pressentation.screen.MainScreen

@Composable
fun Navigation(
    navController: NavHostController,
    padding: PaddingValues,
    apiViewModel: ApiViewModel
){
    NavHost(
        navController = navController,
        startDestination = Route.Main.route
    ){
        composable(Route.Main.route){
            MainScreen(padding)
        }
    }
}

sealed class Route(val route: String){
    object Main: Route("main")
    object Chat: Route("chat/{id}"){ fun createRoute(id:Long) = "chat/$id" }
    object Profile: Route("profile")
    object Settings: Route("settings")
}