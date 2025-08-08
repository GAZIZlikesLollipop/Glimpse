package org.app.glimpse.data.network

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.app.glimpse.data.repository.ApiRepository
import java.util.Locale

sealed interface ApiState {
    object Initial: ApiState
    data class Success(val data: Any): ApiState
    object Loading: ApiState
    object Error: ApiState
}

class ApiViewModel(val apiRepository: ApiRepository): ViewModel() {

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

    fun getOwnData(){
        _userData.value = ApiState.Loading
        viewModelScope.launch {
            _userData.value = try {
                ApiState.Success(apiRepository.getUserData())
            } catch (e: Exception){
                Log.e("Network",e.localizedMessage ?: "")
                ApiState.Error
            }
        }
    }

}

class ApiViewModelFactory(val apiRepository: ApiRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(ApiViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ApiViewModel(apiRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}