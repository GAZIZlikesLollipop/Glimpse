package org.app.glimpse.data.network

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import io.ktor.client.plugins.ClientRequestException
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.app.glimpse.Route
import org.app.glimpse.UserData
import org.app.glimpse.data.repository.ApiRepository
import org.app.glimpse.data.repository.UserDataRepository
import org.app.glimpse.data.repository.UserPreferencesRepository
import java.util.Locale

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
    var isFirst by mutableStateOf(true)
    var isChats by mutableStateOf(false)

    fun getLocation(
        longitude: Double,
        latitude: Double
    ) {
        viewModelScope.launch {
            _geocoderState.value = try {
                withContext(Dispatchers.IO) {
                    ApiState.Success(apiRepository.getLocation(longitude, latitude,17, "${Locale.getDefault().language.lowercase(Locale.ROOT)}_${Locale.getDefault().country.uppercase(Locale.ROOT)}").name)
                }
            } catch (_: Exception){
                ApiState.Error
            }
        }
    }

    private val _userData: MutableStateFlow<ApiState> = MutableStateFlow(ApiState.Initial)
    val userData = _userData.asStateFlow()

    private val _chatMessages: MutableStateFlow<ApiState> = MutableStateFlow(ApiState.Initial)
    val chatMessages = _chatMessages.asStateFlow()

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
            Route.Default.route
        )

    val userLang = userPreferencesRepository.userLang
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            "en"
        )

    fun setUserLang(lang: String){
        viewModelScope.launch {
            withContext(Dispatchers.IO) { // Добавлено
                userPreferencesRepository.setUserLang(lang)
            }
        }
    }

    fun getChatMessages(receiverId: Long){
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO){
                    _chatMessages.value = ApiState.Success(apiRepository.getChatMessages(token.value,receiverId))
                }
            } catch(e: Exception){
                Log.e("CHAT_MESSAGES",e.localizedMessage ?: "Ошибка получения ответа от сервера")
                _chatMessages.value = ApiState.Error
            }
        }
    }

    fun signIn(
        userName: String,
        password: String
    ){
        _userData.value = ApiState.Loading
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO){
                    val toka = apiRepository.signIn(userName,password)
                    userPreferencesRepository.setToken(toka.substring(1,toka.length-1))
                }
            } catch (e: Exception) {
                _userData.value = ApiState.Error
                Log.e("TOKEN", e.localizedMessage ?: "")
            }
            withContext(Dispatchers.IO){
                while(token.value.isBlank()){
                    delay(250)
                }
            }
            try {
                withContext(Dispatchers.IO) {
                    val data = apiRepository.getUserData(token.value)!!
                    _userData.value = ApiState.Success(data)
                    userDataRepository.setUserDataNet(data)
                    userPreferencesRepository.setStartRoute(Route.Main.route)
                }
            } catch(e: Exception) {
                _userData.value = ApiState.Error
                Log.e("SIGNIN", e.localizedMessage ?: "")
            }
        }
    }

    fun getFriendFriends(mutFriendId: Int){
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { // Добавлено
                    val user = ((userData.value as ApiState.Success).data as User)
                    var friendId = 0
                    for(f in user.friends) {
                        if(f.friends?.get(mutFriendId) != null){
                            friendId = user.friends.indexOf(f)
                        }
                    }
                    val friendsNet = apiRepository.getFriendFriends(user.friends[friendId].friends?.get(mutFriendId)!!.id)
                    val friendNet = user.friends[friendId].friends?.get(mutFriendId)?.copy(friends = friendsNet)
                    val friendMap = user.friends[friendId].friends?.toMutableList().apply { this?.set(mutFriendId, friendNet!!) }
                    val updatedFriends = user.friends.toMutableList().apply { this[friendId] = this[friendId].copy(friends = friendMap) }
                    _userData.value = ApiState.Success(user.copy(friends = updatedFriends))
                }
            } catch(e: Exception) {
                Log.e("friends",e.localizedMessage ?: "")
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
                withContext(Dispatchers.IO){
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
                }
            } catch (e: Exception) {
                _userData.value = ApiState.Error
                Log.e("SIGNUP", e.localizedMessage ?: "")
            }
            try {
                withContext(Dispatchers.IO){
                    val toka = apiRepository.signIn(userName,password)
                    userPreferencesRepository.setToken(toka.substring(1,toka.length-1))
                }
            } catch (e: Exception) {
                _userData.value = ApiState.Error
                Log.e("TOKEN", e.localizedMessage ?: "")
            }
            withContext(Dispatchers.IO) {
                while (token.value.isBlank()) {
                    delay(100)
                }
            }
            try {
                withContext(Dispatchers.IO){
                    val data = apiRepository.getUserData(token.value)!!
                    _userData.value = ApiState.Success(data)
                    userDataRepository.setUserDataNet(data)
                    userPreferencesRepository.setStartRoute(Route.Main.route)
                }
            } catch(e: Exception) {
                _userData.value = ApiState.Error
                Log.e("SIGNUP", e.localizedMessage ?: "")
            }
        }
    }

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
                createdAt = data.createdAt,
                updatedAt = data.updatedAt
            )
        )
    }

    fun deleteAccount() {
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                apiRepository.deleteAccount(token.value)
                webSocketCnn?.send(Frame.Text(""))
                _userData.value = ApiState.Initial
                userDataRepository.setUserData(UserData.getDefaultInstance())
            }
        }
    }

    fun setRoute(route: String){
        viewModelScope.launch {
            _userData.value = ApiState.Initial
            withContext(Dispatchers.IO) { // Добавлено
                userPreferencesRepository.setStartRoute(route)
            }
        }
    }

    fun updateUser(
        data: UpdateUser
    ){
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO){
                    val response = apiRepository.updateUserData(token.value,data)
                    _userData.value = ApiState.Success(response)
                    userDataRepository.setUserDataNet(response)
                    webSocketCnn?.send(Frame.Text(""))
                }
            } catch(e: Exception) {
                Log.e("Update",e.localizedMessage ?: "")
            }
        }
    }

    val userNames = mutableStateListOf<Users>()
    fun getUserNames() {
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                userNames.clear()
                userNames.addAll(apiRepository.getUserNames())
            }
        }
    }

    fun addFriend(id: Long) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO){
                    apiRepository.addFriend(id,token.value)
                    _userData.value = ApiState.Success(apiRepository.getUserData(token.value)!!)
                    webSocketCnn?.send(Frame.Text(""))
                }
            } catch(e: Exception) {
                Log.e("FRIEND", e.localizedMessage ?: "")
            }
        }
    }

    fun deleteFriend(id: Long) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO){
                    apiRepository.deleteFriend(id,token.value)
                    val resp = apiRepository.getUserData(token.value)!!
                    _userData.value = ApiState.Success(resp)
                    userDataRepository.setUserDataNet(resp)
                    webSocketCnn?.send(Frame.Text(""))
                    setRoute(Route.Login.route)
                }
            } catch(e: Exception) {
                Log.e("FRIEND", e.localizedMessage ?: "")
            }
        }
    }

    fun clearData(){
        viewModelScope.launch {
            _userData.value = ApiState.Initial
            withContext(Dispatchers.IO) { // Добавлено
                userDataRepository.setUserData(UserData.getDefaultInstance())
            }
        }
    }

    fun getOwnData(callback: () -> Unit){
        _userData.value = ApiState.Loading
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO){
                    val res = apiRepository.getUserData(token.value)
                    if(res != null) {
                        userDataRepository.setUserDataNet(res)
                        _userData.value = ApiState.Success(res)
                    }
                }
            } catch(e: Exception) {
                Log.e("OWNDATA",e.localizedMessage ?: "")
                setUserData()
            }
            catch(_: ClientRequestException) {
                setUserData()
                callback()
                clearData()
            }
        }
    }

    fun startWebSocket(){
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    apiRepository.startWebSocket(
                        token.value,
                        {
                            viewModelScope.launch {
                                userDataRepository.setUserDataNet(it.user)
                                _userData.value = ApiState.Success(it.user)
                                _chatMessages.value = ApiState.Loading
                                _chatMessages.value = ApiState.Success(it.messages)
                            }
                        },
                        { webSocketCnn = it }
                    )
                }
            } catch (e: Exception){
                Log.e("WEBSOCKET",e.localizedMessage ?: "")
            }
        }
    }

    fun sendMessage(
        msg: Message,
        receiverId: Long
    ): Deferred<Message?> {
        return viewModelScope.async {
            withContext(Dispatchers.IO) {
                while(webSocketCnn == null){
                    delay(250)
                }
            }
            withContext(Dispatchers.IO) {
                try {
                    val result = apiRepository.sendMessage(
                        msg = msg,
                        token = token.value,
                        receiverId = receiverId
                    )
                    webSocketCnn?.send(Frame.Text("$receiverId"))
                    result
                } catch (e: Exception) {
                    Log.e("msg", e.localizedMessage ?: "")
                    null
                }
            }
        }
    }

    fun deleteMessage(
        msgId: Long,
        friendId: Long
    ){
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO){
                    apiRepository.deleteMessage(msgId,token.value)
                    webSocketCnn?.send(Frame.Text("$friendId"))
                }
            } catch (e: Exception) {
                Log.e("DELETE_MSG", e.localizedMessage ?: "")
            }
        }
    }

    fun updateMessage(
        id: Long,
        content: String,
        friendId: Long
    ){
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO){
                    apiRepository.updateMessage(Message(id,content),token.value)
                    webSocketCnn?.send(Frame.Text("$friendId"))
                }
            } catch (e: Exception){
                Log.e("UPDATEMSG", e.localizedMessage ?: "")
            }
        }
    }

    private val _mapState = MutableStateFlow(
        CameraPosition(Point(
            if(you.value.latitude == 0.0) 41.311286 else you.value.latitude,
            if(you.value.longitude == 0.0) 69.279755 else you.value.longitude
        ),15f,0f,0f)
    )
    val mapState = _mapState.asStateFlow()

    fun editMapState(state: CameraPosition){
        _mapState.value = state
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