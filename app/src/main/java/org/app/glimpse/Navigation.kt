package org.app.glimpse

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import org.app.glimpse.data.ApiViewModel
import org.app.glimpse.pressentation.screen.ChatScreen
import org.app.glimpse.pressentation.screen.MainScreen
import org.app.glimpse.pressentation.screen.ProfileScreen

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
            MainScreen(padding, navController)
        }
        composable(
            route = Route.Chat.route,
            arguments = listOf(navArgument("friendId"){type = NavType.LongType})
        ){
            val id = it.arguments?.getLong("friendId") ?: 0
            ChatScreen(id,padding,navController)
        }
        composable(
            route = Route.Profile.route,
            arguments = listOf(navArgument("id"){type = NavType.LongType})
        ) {
            ProfileScreen()
        }
    }
}

sealed class Route(val route: String){
    object Main: Route("main")
    object Chat: Route("chat/{friendId}"){ fun createRoute(friendId: Long) = "chat/$friendId" }
    object Profile: Route("profile/{id}"){ fun createRoute(id:Long) = "chat/$id" }
    object Settings: Route("settings")
}