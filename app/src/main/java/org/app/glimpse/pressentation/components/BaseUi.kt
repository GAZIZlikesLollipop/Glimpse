package org.app.glimpse.pressentation.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import org.app.glimpse.Navigation

@Composable
fun BaseScreen(){
    val navController = rememberNavController()
    Scaffold(
        topBar = {TopAppBar()},
        bottomBar = {BottomAppBar()},
        modifier = Modifier.fillMaxSize()
    ){ paddingValues ->
        Navigation(
            navController = navController,
            padding = paddingValues
        )
    }
}

@Composable
fun TopAppBar(){}

@Composable
fun BottomAppBar(){}