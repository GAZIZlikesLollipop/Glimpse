package org.app.glimpse.pressentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.app.glimpse.R
import org.app.glimpse.Route
import org.app.glimpse.data.network.ApiState
import org.app.glimpse.data.network.ApiViewModel

@Composable
fun RegisterScreen(){

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
                )
            },
            placeholder = {
                Text(
                    cnt[3],
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
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
                )
            },
            placeholder = {
                Text(
                    cnt[4],
                    color = MaterialTheme.colorScheme.onBackground
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
                apiViewModel.signIn(loginField,passwordField)
                loginField = ""
                passwordField = ""
            },
            enabled = loginField.isNotBlank() && passwordField.isNotBlank(),
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