package org.app.glimpse.data.network

import android.graphics.Bitmap
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
import org.app.glimpse.UserData
import org.app.glimpse.data.repository.ApiRepository
import org.app.glimpse.data.repository.UserDataRepository
import org.app.glimpse.data.repository.UserPreferencesRepository
import java.util.Locale
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

sealed interface ApiState {
    object Initial: ApiState
    data class Success(val data: Any): ApiState
    object Loading: ApiState
    object Error: ApiState
}

class ApiViewModel(
    val apiRepository: ApiRepository,
    val userPreferencesRepository: UserPreferencesRepository,
    val userDataRepository: UserDataRepository
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
            } catch (_: Exception){
                ApiState.Error
            }
        }
    }

    private val _userData: MutableStateFlow<ApiState> = MutableStateFlow(ApiState.Initial)
    val userData = _userData.asStateFlow()

    val you = userDataRepository.userData
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            UserData.getDefaultInstance()
        )

    val token = userPreferencesRepository.token
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            ""
        )

    val startRoute = userPreferencesRepository.startRoute
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            Route.Login.route
        )


    fun signIn(
        userName: String,
        password: String
    ){
        _userData.value = ApiState.Loading
        viewModelScope.launch {
            try {
                val toka = apiRepository.signIn(userName,password)
                userPreferencesRepository.setToken(toka.substring(1,toka.length-1))
            } catch (e: Exception) {
                _userData.value = ApiState.Error
                Log.e("TOKEN", e.localizedMessage ?: "")
            }
            while(token.value.isBlank()){
                delay(100)
            }
            try {
                val data = apiRepository.getUserData(token.value)
                _userData.value = ApiState.Success(data)
                userDataRepository.setUserData(data)
                userPreferencesRepository.setStartRoute(Route.Main.route)
            } catch(e: Exception) {
                _userData.value = ApiState.Error
                Log.e("USERDATA", e.localizedMessage ?: "")
            }
        }
    }

    fun signUp(
        userName: String,
        password: String,
        about: String? = null,
        avatar: Bitmap? = null,
        avatarExt: String
    ){
        _userData.value = ApiState.Loading
        viewModelScope.launch {
            try {
                apiRepository.signUp(
                    SignUpUser(
                        userName = userName,
                        password = password,
                        bio = about ?: "",
                        avatar = avatar,
                        avatarExt = avatarExt
                    )
                )
            } catch (e: Exception) {
                _userData.value = ApiState.Error
                Log.e("SIGNUP", e.localizedMessage ?: "")
            }
            try {
                val toka = apiRepository.signIn(userName,password)
                userPreferencesRepository.setToken(toka.substring(1,toka.length-1))
            } catch (e: Exception) {
                _userData.value = ApiState.Error
                Log.e("TOKEN", e.localizedMessage ?: "")
            }
            while(token.value.isBlank()){
                delay(100)
            }
            try {
                val data = apiRepository.getUserData(token.value)
                _userData.value = ApiState.Success(data)
                userDataRepository.setUserData(data)
                userPreferencesRepository.setStartRoute(Route.Main.route)
            } catch(e: Exception) {
                _userData.value = ApiState.Error
                Log.e("USERDATA", e.localizedMessage ?: "")
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    fun getOwnData(){
        _userData.value = ApiState.Loading
        viewModelScope.launch {
             try {
                 val data = you.value
                 val friends = mutableListOf<FriendUser>()
                 for(friend in data.friendsList){
                     val friendsFriend = mutableListOf<FriendData>()
                     for(friendFriends in friend.friendsList){
                         friendsFriend.add(
                             FriendData(
                                 id = friendFriends.id,
                                 name = friendFriends.name,
                                 avatar = friendFriends.avatar,
                                 bio = friendFriends.bio,
                                 latitude = friendFriends.latitude,
                                 longitude = friendFriends.longitude,
                                 friends = null,
                                 createdAt = Instant.fromEpochMilliseconds(friendFriends.createdAt),
                                 updatedAt = Instant.fromEpochMilliseconds(friendFriends.updatedAt)
                             )
                         )
                     }
                     friends.add(
                         FriendUser(
                             id = friend.id,
                             name = friend.name,
                             avatar = friend.avatar,
                             bio = friend.bio,
                             latitude = friend.latitude,
                             longitude = friend.longitude,
                             lastOnline = Instant.fromEpochMilliseconds(friend.lastOnline),
                             friends = friendsFriend,
                             createdAt = Instant.fromEpochMilliseconds(friend.createdAt),
                             updatedAt = Instant.fromEpochMilliseconds(friend.updatedAt)
                         )
                     )
                 }
                 val sentMessages = mutableListOf<Message>()
                 for(msg in data.sentMessagesList){
                     sentMessages.add(
                         Message(
                             id = msg.id,
                             content = msg.content,
                             isChecked = msg.isChecked,
                             senderId = msg.senderId,
                             receivedId = msg.receivedId,
                             createdAt = Instant.fromEpochMilliseconds(msg.createdAt),
                             updatedAt = Instant.fromEpochMilliseconds(msg.updatedAt)
                         )
                     )
                 }
                 val receivedMessages = mutableListOf<Message>()
                 for(msg in data.receivedMessagesList){
                     receivedMessages.add(
                         Message(
                             id = msg.id,
                             content = msg.content,
                             isChecked = msg.isChecked,
                             senderId = msg.senderId,
                             receivedId = msg.receivedId,
                             createdAt = Instant.fromEpochMilliseconds(msg.createdAt),
                             updatedAt = Instant.fromEpochMilliseconds(msg.updatedAt)
                         )
                     )
                 }
                _userData.value = ApiState.Success(
                    User(
                        id = data.id,
                        name = data.name,
                        password = data.password,
                        bio = data.bio,
                        avatar = data.avatar,
                        latitude = data.latitude,
                        longitude = data.longitude,
                        friends = friends,
                        sentMessages = sentMessages,
                        receivedMessages = receivedMessages,
                        createdAt = Instant.fromEpochMilliseconds(data.createdAt),
                        updatedAt = Instant.fromEpochMilliseconds(data.updatedAt)
                    )
                )
            } catch (e: Exception){
                 _userData.value = ApiState.Error
                Log.e("Network",e.localizedMessage ?: "")
            }
        }
    }

}

class ApiViewModelFactory(
    val apiRepository: ApiRepository,
    val userPreferencesRepository: UserPreferencesRepository,
    val userDataRepository: UserDataRepository
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(ApiViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ApiViewModel(apiRepository,userPreferencesRepository,userDataRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}