package org.app.glimpse.data.network

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.app.glimpse.Route
import org.app.glimpse.data.repository.ApiRepository
import org.app.glimpse.data.repository.UserRepository
import java.util.Locale

sealed interface ApiState {
    object Initial: ApiState
    data class Success(val data: Any): ApiState
    object Loading: ApiState
    object Error: ApiState
}

class ApiViewModel(
    val apiRepository: ApiRepository,
    val userRepository: UserRepository
): ViewModel() {

    private val _geocoderState = MutableStateFlow<ApiState>(ApiState.Loading)
    val geocoderState = _geocoderState.asStateFlow()

    fun getLocation(
        longitude: Double,
        latitude: Double
    ) {
        viewModelScope.launch {
            _geocoderState.value = try {
                ApiState.Success(apiRepository.getLocation(longitude, latitude,17, "${Locale.getDefault().language.lowercase(Locale.ROOT)}_${Locale.getDefault().country.uppercase(Locale.ROOT)}").name)
            }catch (_: Exception){
                ApiState.Error
            }
        }
    }

    private val _userData = MutableStateFlow<ApiState>(ApiState.Initial)
    val userData = _userData.asStateFlow()

    val token = userRepository.token
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            ""
        )

    val startRoute = userRepository.startRoute
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            Route.Login.route
        ).value

    fun signIn(
        userName: String,
        password: String
    ){
        _userData.value = ApiState.Loading
        viewModelScope.launch {
            try {
                val toka = apiRepository.signIn(userName,password)
                userRepository.setToken(toka.substring(1,toka.length-1))
            } catch (e: Exception) {
                Log.e("TOKEN", e.localizedMessage)
            }
            while(token.value.isBlank()){
                delay(100)
            }
            try {
                _userData.value = ApiState.Success(apiRepository.getUserData(token.value))
                userRepository.setStartRoute(Route.Main.route)
            } catch(e: Exception) {
                _userData.value = ApiState.Error
                Log.e("Network", "${e.localizedMessage} ${token.value}")
            }
        }
    }

    fun getOwnData(){
        _userData.value = ApiState.Loading
        viewModelScope.launch {
            _userData.value = try {
                ApiState.Success(apiRepository.getUserData(token.value))
            } catch (e: Exception){
                Log.e("Network",e.localizedMessage ?: "")
                ApiState.Error
            }
        }
    }

}

class ApiViewModelFactory(
    val apiRepository: ApiRepository,
    val jwtRepository: UserRepository
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(ApiViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ApiViewModel(apiRepository,jwtRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}