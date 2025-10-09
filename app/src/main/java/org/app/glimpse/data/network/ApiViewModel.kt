package org.app.glimpse.data.network

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.ktor.websocket.DefaultWebSocketSession
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.app.glimpse.FriendData
import org.app.glimpse.Route
import org.app.glimpse.UserData
import org.app.glimpse.data.repository.ApiRepository
import org.app.glimpse.data.repository.UserDataRepository
import org.app.glimpse.data.repository.UserPreferencesRepository
import java.util.Locale
import kotlin.time.ExperimentalTime

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
    var webSocketCnn: DefaultWebSocketSession? = null

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

    val isServiceRun = userPreferencesRepository.isServiceRun
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            false
        )

    val startRoute = userPreferencesRepository.startRoute
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            Route.Login.route
        )

    val userLang = userPreferencesRepository.userLang
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            "en"
        )

    fun setUserLang(lang: String){
        viewModelScope.launch {
            userPreferencesRepository.setUserLang(lang)
        }
    }

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
                userDataRepository.setUserDataNet(data)
                userPreferencesRepository.setStartRoute(Route.Main.route)
            } catch(e: Exception) {
                _userData.value = ApiState.Error
                Log.e("USERDATA", e.localizedMessage ?: "")
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    fun getFriendFriends(id: Long){
        viewModelScope.launch {
            try {
                val user = you.value
                val friendFriends = mutableListOf<FriendData>()
                apiRepository.getFriendFriends(id).forEach { data ->
                    friendFriends.add(
                        FriendData.newBuilder()
                            .setId(data.id)
                            .setName(data.name)
                            .setBio(data.bio)
                            .setAvatar(data.avatar)
                            .setLatitude(data.latitude)
                            .setLongitude(data.longitude)
                            .setCreatedAt(data.createdAt)
                            .setUpdatedAt(data.updatedAt)
                            .build()
                    )
                }
                val friends = you.value.friendsList
                    .find { it.id == id } !!.toBuilder()
                    .addAllFriends(friendFriends)
                    .build()
                user.toBuilder().addFriends(friends).build()
                userDataRepository.setUserData(user)
            } catch(e: Exception) {
                Log.d("HEllo",e.localizedMessage ?: "")
            }
        }
    }
    fun signUp(
        userName: String,
        password: String,
        about: String? = null,
        avatar: Bitmap? = null,
        avatarExt: String,
        latitude: Double,
        longitude: Double
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
                        avatarExt = avatarExt,
                        latitude,
                        longitude
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
                userDataRepository.setUserDataNet(data)
                userPreferencesRepository.setStartRoute(Route.Main.route)
            } catch(e: Exception) {
                _userData.value = ApiState.Error
                Log.e("USERDATA", e.localizedMessage ?: "")
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun setUserData() {
        val data = you.value
        val friends = mutableListOf<FriendUser>()
        for(friend in data.friendsList){
            val friendsFriend = mutableListOf<FriendUser>()
            for(friendFriends in friend.friendsList){
                friendsFriend.add(
                    FriendUser(
                        id = friendFriends.id,
                        name = friendFriends.name,
                        avatar = friendFriends.avatar,
                        bio = friendFriends.bio,
                        latitude = friendFriends.latitude,
                        longitude = friendFriends.longitude,
                        friends = null,
                        createdAt = friendFriends.createdAt,
                        updatedAt = friendFriends.updatedAt
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
                    lastOnline = friend.lastOnline,
                    friends = friendsFriend,
                    createdAt = friend.createdAt,
                    updatedAt = friend.updatedAt
                )
            )
        }
        val sentMessages = mutableListOf<Message>()
        for(msg in data.sentMessagesList){
            sentMessages.add(
                Message(
                    id = msg.id,
                    content = msg.content,
                    senderId = msg.senderId,
                    receiverId = msg.receivedId,
                    createdAt = msg.createdAt,
                    updatedAt = msg.updatedAt
                )
            )
        }
        val receivedMessages = mutableListOf<Message>()
        for(msg in data.receivedMessagesList){
            receivedMessages.add(
                Message(
                    id = msg.id,
                    content = msg.content,
                    senderId = msg.senderId,
                    receiverId = msg.receivedId,
                    createdAt = msg.createdAt,
                    updatedAt = msg.updatedAt
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
                createdAt = data.createdAt,
                updatedAt = data.updatedAt
            )
        )

    }

    @OptIn(ExperimentalTime::class)
    fun getOwnData(){
        _userData.value = ApiState.Loading
        viewModelScope.launch {
             try {
                 userDataRepository.setUserDataNet(apiRepository.getUserData(token.value))
                 setUserData()
             } catch(_: Exception) {
                 setUserData()
             }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _userData.value = ApiState.Initial
            userPreferencesRepository.setStartRoute(Route.Login.route)
            apiRepository.deleteAccount(token.value)
        }
    }

    fun setRoute(route: String){
        viewModelScope.launch {
            _userData.value = ApiState.Initial
            userPreferencesRepository.setStartRoute(route)
        }
    }

    fun updateUser(
        data: UpdateUser
    ){
        viewModelScope.launch {
            try {
                val response = apiRepository.updateUserData(token.value,data)
                _userData.value = ApiState.Success(response)
                userDataRepository.setUserDataNet(response)
            } catch(e: Exception) {
                Log.e("Update",e.localizedMessage ?: "")
            }
        }
    }
    val userNames = mutableStateListOf<Users>()
    fun getUserNames() {
       viewModelScope.launch {
           userNames.clear()
           userNames.addAll(apiRepository.getUserNames())
       }
    }

    fun addFriend(id: Long) {
        viewModelScope.launch {
            try {
                apiRepository.addFriend(id,token.value)
                _userData.value = ApiState.Success(apiRepository.getUserData(token.value))
            } catch(e: Exception) {
                Log.e("FRIEND", e.localizedMessage ?: "")
            }
        }
    }

    fun deleteFriend(id: Long) {
        viewModelScope.launch {
            try {
                apiRepository.deleteFriend(id,token.value)
                _userData.value = ApiState.Success(apiRepository.getUserData(token.value))
            } catch(e: Exception) {
                Log.e("FRIEND", e.localizedMessage ?: "")
            }
        }
    }

    fun startWebSocket(){
        viewModelScope.launch {
            webSocketCnn = apiRepository.startWebSocket(
                token.value
            ) {
                viewModelScope.launch {
                    userDataRepository.setUserDataNet(it)
                    _userData.value = ApiState.Success(it)
                }
            }
        }
    }

    fun sendMessage(
        msg: Message,
        receiverId: Long
    ): Deferred<Message?> {
//        if(webSocketCnn == null) {
//            startWebSocket()
//        }
        return viewModelScope.async {
//            while(webSocketCnn == null){
//                delay(100)
//            }
            try {
                val result = apiRepository.sendMessage(
                    msg = msg,
                    token = token.value,
                    receiverId = receiverId
                )
                val data = apiRepository.getUserData(token.value)
                _userData.value = ApiState.Success(data)
                userDataRepository.setUserDataNet(data)
//                webSocketCnn!!.send(Frame.Text(""))
                result
            } catch (e: Exception) {
                Log.e("msg", e.localizedMessage ?: "")
                null
            }
        }
    }
    fun deleteMessage(msgId: Long){
        viewModelScope.launch {
            try {
                apiRepository.deleteMessage(msgId,token.value)
                val data = apiRepository.getUserData(token.value)
                _userData.value = ApiState.Success(data)
                userDataRepository.setUserDataNet(data)
            } catch (e: Exception) {
                Log.e("DELETE_MSG", e.localizedMessage ?: "")
            }
        }
    }

    fun updateMessage(
        id: Long,
        content: String
    ){
        viewModelScope.launch {
            try {
               apiRepository.updateMessage(id,Message(content = content),token.value)
                val data = apiRepository.getUserData(token.value)
                _userData.value = ApiState.Success(data)
                userDataRepository.setUserDataNet(data)
            } catch (e: Exception){
                Log.e("UPDATEMSG", e.localizedMessage ?: "")
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