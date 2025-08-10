package org.app.glimpse

import android.annotation.SuppressLint
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import org.app.glimpse.data.network.ApiViewModel
import org.app.glimpse.pressentation.screen.ChatScreen
import org.app.glimpse.pressentation.screen.LoginScreen
import org.app.glimpse.pressentation.screen.MainScreen
import org.app.glimpse.pressentation.screen.ProfileScreen
import org.app.glimpse.pressentation.screen.RegisterScreen
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@SuppressLint("NewApi")
@Composable
fun Navigation(
    navController: NavHostController,
    padding: PaddingValues
){
    val apiViewModel = viewModel<ApiViewModel>()
    val token by apiViewModel.token.collectAsState()
    NavHost(
        navController = navController,
        startDestination = if(token.isNotBlank()) Route.Main.route else Route.Login.route
    ){
        composable(Route.Main.route){
            MainScreen(padding, navController,apiViewModel)
        }
        composable(
            route = Route.Chat.route,
            arguments = listOf(navArgument("friendId"){type = NavType.LongType}),
            enterTransition = {
                slideInVertically(tween(450,50)){it}
            },
            exitTransition = {
                slideOutVertically(tween(450,50)){it}
            }
        ){
            val friendId = it.arguments?.getLong("friendId") ?: 0
            ChatScreen(friendId,padding,navController,apiViewModel)
        }
        composable(
            route = Route.Profile.route,
            arguments = listOf(navArgument("userId"){type = NavType.LongType}),
            enterTransition = {
                slideInVertically(tween(450,50))
            },
            exitTransition = {
                slideOutVertically(tween(450,50))
            }
        ) {
            val userId = it.arguments?.getLong("userId") ?: -1
            ProfileScreen(userId,navController,padding,apiViewModel)
        }
        composable(Route.Login.route){
            LoginScreen()
        }
        composable(Route.Register.route){
            RegisterScreen()
        }
    }
}

sealed class Route(val route: String){
    object Main: Route("main")
    object Chat: Route("chat/{friendId}"){ fun createRoute(friendId: Long) = "chat/$friendId" }
    object Profile: Route("profile/{userId}"){ fun createRoute(userId: Long) = "profile/$userId" }
    object Login: Route("login")
    object Register: Route("register")
}