package org.app.glimpse.data.repository

import android.graphics.Bitmap
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.utils.io.InternalAPI
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import org.app.glimpse.data.network.AuthRequest
import org.app.glimpse.data.network.FriendUser
import org.app.glimpse.data.network.GeocoderResponse
import org.app.glimpse.data.network.Message
import org.app.glimpse.data.network.SignUpUser
import org.app.glimpse.data.network.UpdateUser
import org.app.glimpse.data.network.User
import org.app.glimpse.data.network.Users
import org.app.glimpse.data.network.json
import java.io.ByteArrayOutputStream
import java.util.Locale

interface ApiRepo {
    suspend fun getLocation(
        longitude: Double,
        latitude: Double,
        zoomLvl: Int,
        language: String? = null
    ): GeocoderResponse
    suspend fun getUserData(token: String): User?
    suspend fun signIn(login: String, password: String): String
    suspend fun signUp(data: SignUpUser)
    suspend fun startWebSocket(
        token: String,
        onReceived: (User) -> Unit,
        onWebSocket: (DefaultClientWebSocketSession) -> Unit
    )
    suspend fun getUserNames(): List<Users>
    suspend fun getFriendFriends(friendFriendId: Long): List<FriendUser>
    suspend fun updateUserData(token: String, data: UpdateUser): User
    suspend fun deleteAccount(token: String)
    suspend fun addFriend(
        id: Long,
        token: String
    )
    suspend fun deleteFriend(
        id: Long,
        token: String
    )
    suspend fun sendMessage(
        msg: Message,
        token: String,
        receiverId: Long
    ): Message
    suspend fun deleteMessage(
        id: Long,
        token: String
    )
    suspend fun updateMessage(
        msg: Message,
        token: String
    )
}

class ApiRepository(val httpClient: HttpClient): ApiRepo {
//    val host = "10.0.2.2"
    val host = "172.30.102.229"

    override suspend fun deleteAccount(token: String) {
        httpClient.delete("https://$host:8080/api/users") { header("Authorization", "Bearer $token") }
    }
    override suspend fun getLocation(
        longitude: Double,
        latitude: Double,
        zoomLvl: Int,
        language: String?
    ): GeocoderResponse {
        return httpClient.get("https://nominatim.openstreetmap.org/reverse?format=json&lat=$latitude&lon=$longitude&zoom=$zoomLvl&addressdetails=1&accept-language=${language ?: Locale.getDefault().language.lowercase(Locale.ROOT)}").body<GeocoderResponse>()
    }

    override suspend fun getUserData(token: String): User? {
        return try {
            httpClient.get("https://$host:8080/api/users"){
                header("Authorization", "Bearer $token")
            }.body<User>()
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun getFriendFriends(friendFriendId: Long): List<FriendUser> {
        return httpClient.get("https://$host:8080/friends/$friendFriendId").body<List<FriendUser>>()
    }

    override suspend fun signUp(data: SignUpUser) {
        val stream = ByteArrayOutputStream()
        data.avatar?.compress(Bitmap.CompressFormat.PNG,100,stream)
        val avatar = stream.toByteArray()
        httpClient.submitFormWithBinaryData(
            url = "https://$host:8080/signUp",
            formData = formData {
                append("name",data.userName)
                append("password",data.password)
                append("bio",data.bio)
                if(data.avatar != null) {
                    append("avatar", avatar, Headers.build {
                        append(HttpHeaders.ContentType, "application/octet-stream")
                        append(HttpHeaders.ContentDisposition, "filename=\"${data.userName}.${data.avatarExt}\"")
                    })
                }
                append("latitude",data.latitude)
                append("longitude",data.longitude)
            }
        )
    }

    override suspend fun signIn(
        login: String,
        password: String
    ): String {
        return httpClient.post("https://$host:8080/signIn") {
            contentType(ContentType.Application.Json)
            setBody(AuthRequest(login,password))
        }.body<String>()
    }

    @OptIn(InternalAPI::class)
    override suspend fun updateUserData(token: String, data: UpdateUser): User {
        val stream = ByteArrayOutputStream()
        data.avatar?.compress(Bitmap.CompressFormat.PNG,100,stream)
        val avatar = stream.toByteArray()
        return httpClient.patch("https://$host:8080/api/users") {
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(
                MultiPartFormDataContent(
                    formData {
                        if(data.lastOnline != null) {
                            append("online",data.lastOnline)
                        }
                        if(data.name != null) {
                            append("name", data.name)
                        }
                        if(data.password != null) {
                            append("password", data.password)
                        }
                        if(data.bio != null) {
                            append("bio", data.bio)
                        }
                        if(data.avatar != null) {
                            append("avatar", avatar, Headers.build {
                                append(HttpHeaders.ContentType, "application/octet-stream")
                                append(HttpHeaders.ContentDisposition, "filename=\"${data.name ?: "User"}.${data.avatarExt ?: "png"}\"")
                            })
                        }
                        if(data.latitude != null) {
                            append("latitude", data.latitude)
                        }
                        if(data.longitude != null) {
                            append("longitude", data.longitude)
                        }
                        if(data.friends != null) {
                            for(f in data.friends) {
                                append("friends",f)
                            }
                        }
                        if(data.receivedMessages != null) {
                            for(f in data.receivedMessages) {
                                append("receivedMessages",f)
                            }
                        }
                        if(data.sentMessages != null) {
                            for(f in data.sentMessages) {
                                append("sentMessages",f)
                            }
                        }
                    }
                )
            )
        }.body<User>()
    }

    override suspend fun getUserNames(): List<Users> {
        return httpClient.get("https://$host:8080/users").body()
    }

    override suspend fun addFriend(
        id: Long,
        token: String
    ) {
        httpClient.get("https://$host:8080/api/friends/$id"){
            header("Authorization", "Bearer $token")
        }
    }

    override suspend fun deleteFriend(id: Long, token: String) {
        httpClient.delete("https://$host:8080/api/friends/$id") {
            header("Authorization", "Bearer $token")
        }
    }

    override suspend fun deleteMessage(
        id: Long,
        token: String
    ) {
        httpClient.delete("https://$host:8080/api/messages/$id"){ header("Authorization", "Bearer $token") }
    }

    override suspend fun sendMessage(
        msg: Message,
        token: String,
        receiverId: Long
    ): Message {
        return httpClient.post("https://$host:8080/api/messages/$receiverId") {
            setBody(msg)
            header("Authorization", "Bearer $token")
            header("Content-Type","application/json")
        }.body()
    }

    override suspend fun updateMessage(
        msg: Message,
        token: String
    ) {
        httpClient.patch("https://$host:8080/api/messages/${msg.id}"){
            setBody(msg)
            header("Content-Type","application/json")
            header("Authorization", "Bearer $token")
        }
    }

    override suspend fun startWebSocket(
        token: String,
        onReceived: (User) -> Unit,
        onWebSocket: (DefaultClientWebSocketSession) -> Unit
    ){
        httpClient.webSocket(
            urlString = "wss://$host:8080/api/ws",
            request = { header("Authorization","Bearer $token") }
        ) {
            onWebSocket(this)
            while(true){
                for(frame in incoming) {
                    when(frame) {
                        is Frame.Text -> {
                            if(frame.readText()[0] == '{'){
                                onReceived(json.decodeFromString(frame.readText()))
                            }
                        }
                        is Frame.Close -> {
                            return@webSocket
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}