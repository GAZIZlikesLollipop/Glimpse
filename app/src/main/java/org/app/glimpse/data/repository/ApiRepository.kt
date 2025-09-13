package org.app.glimpse.data.repository

import android.graphics.Bitmap
import io.ktor.client.HttpClient
import io.ktor.client.call.body
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
import io.ktor.websocket.send
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import org.app.glimpse.data.network.AuthRequest
import org.app.glimpse.data.network.FriendUser
import org.app.glimpse.data.network.GeocoderResponse
import org.app.glimpse.data.network.SignUpUser
import org.app.glimpse.data.network.UpdateUser
import org.app.glimpse.data.network.User
import java.io.ByteArrayOutputStream
import java.util.Locale
import java.util.Scanner

interface ApiRepo {
    suspend fun getLocation(
        longitude: Double,
        latitude: Double,
        zoomLvl: Int,
        language: String? = null
    ): GeocoderResponse
    suspend fun getUserData(token: String): User
    suspend fun signIn(login: String, password: String): String
    suspend fun signUp(data: SignUpUser)
    suspend fun startWebSocket(token: String, onReceived: (User) -> Unit, isSend: Boolean)
    suspend fun updateUserData(token: String, data: UpdateUser): User
    suspend fun getFriendFriends(friendFriendId: Long): List<FriendUser>
    suspend fun deleteAccount(token: String)
}

class ApiRepository(val httpClient: HttpClient): ApiRepo {
    val host = "10.0.2.2"
//    val host = "192.168.1.12"

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

    override suspend fun getUserData(token: String): User {
        return httpClient.get("https://$host:8080/api/users"){
            header("Authorization", "Bearer $token")
        }.body<User>()
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

    override suspend fun startWebSocket(
        token: String,
        onReceived: (User) -> Unit,
        isSend: Boolean
    ) {
        httpClient.webSocket(
            port = 8080,
            path = "/api/ws",
            host =  host,
            request = { header("Authorization",token) }
        ) {
            while(true){
                if(isSend){
                    send(Scanner(System.`in`).next())
                    for(frame in incoming) {
                        when(frame) {
                            is Frame.Text -> {
                                onReceived(Json.decodeFromString(frame.readText()))
                            }
                            is Frame.Close -> {
                                return@webSocket
                            }
                            else -> {}
                        }
                    }
                } else {
                    delay(100)
                }
            }
        }

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
}