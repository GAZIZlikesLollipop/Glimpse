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

    private val _userData = MutableStateFlow<ApiState>(ApiState.Loading)
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

//    val userData: User =
//        User(
//            id = 0,
//            name = "Grinya",
//            password = "12345678",
//            bio = "Im very funny",
//            avatar = "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fmlpnk72yciwc.i.optimole.com%2FcqhiHLc.IIZS~2ef73%2Fw%3Aauto%2Fh%3Aauto%2Fq%3A75%2Fhttps%3A%2F%2Fbleedingcool.com%2Fwp-content%2Fuploads%2F2022%2F11%2FAVATAR_THE_WAY_OF_WATER_1SHT_DIGITAL_LOAK_sRGB_V1.jpg&f=1&nofb=1&ipt=ff723019f41bbd61e208e1e29de3472068d269714c642ff051893f954d0cf29e",
//            latitude = 41.2167289259734,
//            longitude = 69.33520401007092,
//            lastOnline = LocalDateTime.now().toKotlinLocalDateTime(),
//            friends = listOf(
//                FriendUser(
//                    id = 3,
//                    name = "Furiya",
//                    avatar = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSmGjt5BrPeTNQMuCNHnIAPjzPzi-SJRKqqxA&s",
//                    bio = "Im cuties and darkest dragon on the world",
//                    latitude = 41.216728,
//                    longitude = 69.335105,
//                    lastOnline = LocalDateTime.now().toKotlinLocalDateTime(),
//                    friends = emptyList(),
//                    createdAt = LocalDateTime.now().minusWeeks(1).toKotlinLocalDateTime(),
//                    updatedAt = LocalDateTime.now().minusDays(2).toKotlinLocalDateTime()
//                ),
//                FriendUser(
//                    id = 1,
//                    name = "D. Tramp",
//                    avatar = "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Ftse1.mm.bing.net%2Fth%2Fid%2FOIP.-O8kGYxjDkwOldLaV9HL1AHaFW%3Fr%3D0%26pid%3DApi&f=1&ipt=a0dc84ded74f9869574f695c92874028aa9553d5d787c2d4865ba8fc72bbb04b&ipo=images",
//                    bio = "Im very big boy for more than friendship)",
//                    latitude = 41.216195,
//                    longitude = 69.335341,
//                    lastOnline = LocalDateTime.now().minusHours(6).toKotlinLocalDateTime(),
//                    friends = emptyList(),
//                    createdAt = LocalDateTime.now().minusYears(1).toKotlinLocalDateTime(),
//                    updatedAt = LocalDateTime.now().minusMonths(1).toKotlinLocalDateTime()
//                ),
//                FriendUser(
//                    id = 2,
//                    name = "Vanya2077",
//                    bio = "Im very Funny boy",
//                    avatar = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTP0KtXQ5ov5a9PoDEWxGpv1AL83o8As6y5cw&s",
//                    latitude = 41.232697,
//                    longitude = 69.335182,
//                    lastOnline = LocalDateTime.now().toKotlinLocalDateTime(),
//                    friends = emptyList(),
//                    createdAt = LocalDateTime.now().minusDays(3).toKotlinLocalDateTime(),
//                    updatedAt = LocalDateTime.now().minusDays(1).toKotlinLocalDateTime()
//                )
//            ),
//            sentMessages = listOf(
//                Message(
//                    id = 0,
//                    content = "hello, how are you?",
//                    senderId = 0,
//                    receivedId = 3,
//                    isChecked = true,
//                    createdAt = LocalDateTime.now().minusMinutes(5).toKotlinLocalDateTime(),
//                    updatedAt = LocalDateTime.now().toKotlinLocalDateTime()
//                ),
//                Message(
//                    id = 2,
//                    content = "im good!",
//                    senderId = 0,
//                    receivedId = 3,
//                    isChecked = true,
//                    createdAt = LocalDateTime.now().minusMinutes(3).minusSeconds(40).toKotlinLocalDateTime(),
//                    updatedAt = LocalDateTime.now().toKotlinLocalDateTime()
//                ),
//                Message(
//                    id = 1,
//                    content = "bye!",
//                    isChecked = true,
//                    senderId = 0,
//                    receivedId = 0,
//                    createdAt = LocalDateTime.now().minusMinutes(1).minusSeconds(30).toKotlinLocalDateTime(),
//                    updatedAt = LocalDateTime.now().toKotlinLocalDateTime()
//                ),
//                Message(
//                    id = 3,
//                    content = "нет, я занят давай завтра",
//                    senderId = 0,
//                    isChecked = true,
//                    receivedId = 2,
//                    createdAt = LocalDateTime.now().minusMinutes(30).toKotlinLocalDateTime(),
//                    updatedAt = LocalDateTime.now().toKotlinLocalDateTime()
//                ),
//                Message(
//                    id = 4,
//                    content = "yooo D, how are you bro?",
//                    senderId = 0,
//                    isChecked = true,
//                    receivedId = 1,
//                    createdAt = LocalDateTime.now().minusHours(3).toKotlinLocalDateTime(),
//                    updatedAt = LocalDateTime.now().toKotlinLocalDateTime()
//                ),
//                Message(
//                    id = 5,
//                    content = "ok bro i understand",
//                    senderId = 0,
//                    receivedId = 1,
//                    createdAt = LocalDateTime.now().minusHours(1).toKotlinLocalDateTime(),
//                    updatedAt = LocalDateTime.now().toKotlinLocalDateTime()
//                )
//            ),
//            receivedMessages = listOf(
//                Message(
//                    id = 0,
//                    content = "im fine, and you?",
//                    senderId = 3,
//                    receivedId = 0,
//                    createdAt = LocalDateTime.now().minusMinutes(4).toKotlinLocalDateTime(),
//                    updatedAt = LocalDateTime.now().toKotlinLocalDateTime()
//                ),
//                Message(
//                    id = 1,
//                    content = "ok, bye,bye",
//                    senderId = 3,
//                    receivedId = 0,
//                    createdAt = LocalDateTime.now().minusMinutes(2).minusSeconds(50).toKotlinLocalDateTime(),
//                    updatedAt = LocalDateTime.now().toKotlinLocalDateTime()
//                ),
//                Message(
//                    id = 2,
//                    content = "Првет. блат!? п ашли в, Брал Страс ират,",
//                    senderId = 2,
//                    receivedId = 0,
//                    createdAt = LocalDateTime.now().minusHours(1).toKotlinLocalDateTime(),
//                    updatedAt = LocalDateTime.now().toKotlinLocalDateTime()
//                ),
//                Message(
//                    id = 3,
//                    content = "Да блиин. ну ЛадНо тогла звтраа! пиграим, в игоу",
//                    senderId = 2,
//                    receivedId = 0,
//                    createdAt = LocalDateTime.now().minusMinutes(29).minusSeconds(50).toKotlinLocalDateTime(),
//                    updatedAt = LocalDateTime.now().toKotlinLocalDateTime()
//                ),
//                Message(
//                    id = 4,
//                    content = "brooo yeah hi long time no see all is well sorry but can't talk right now",
//                    senderId = 1,
//                    receivedId = 0,
//                    createdAt = LocalDateTime.now().minusHours(2).toKotlinLocalDateTime(),
//                    updatedAt = LocalDateTime.now().toKotlinLocalDateTime()
//                )
//            ),
//            createdAt = LocalDateTime.now().minusMonths(1).toKotlinLocalDateTime(),
//            updatedAt = LocalDateTime.now().minusWeeks(2).toKotlinLocalDateTime()
//        )
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