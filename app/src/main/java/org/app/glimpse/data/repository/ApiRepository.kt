package org.app.glimpse.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import org.app.glimpse.data.network.AuthRequest
import org.app.glimpse.data.network.GeocoderResponse
import org.app.glimpse.data.network.SignUpUser
import org.app.glimpse.data.network.User
import java.util.Locale

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
}

class ApiRepository(val httpClient: HttpClient): ApiRepo {
    override suspend fun getLocation(
        longitude: Double,
        latitude: Double,
        zoomLvl: Int,
        language: String?
    ): GeocoderResponse {
        return httpClient.get("https://nominatim.openstreetmap.org/reverse?format=json&lat=$latitude&lon=$longitude&zoom=$zoomLvl&addressdetails=1&accept-language=${language ?: Locale.getDefault().language.lowercase(Locale.ROOT)}").body<GeocoderResponse>()
    }

    override suspend fun getUserData(token: String): User {
        return httpClient.get("https://192.168.13.49:8080/api/users"){
            header("Authorization", "Bearer $token")
        }.body<User>()
    }

    override suspend fun signUp(data: SignUpUser) {
        httpClient.submitFormWithBinaryData(
            url = "https://192.168.13.49:8080/signUp",
            formData = formData {
                append("name",data.userName)
                append("password",data.password)
                append("bio",data.bio)
                append("avatar", data.avatar.readBytes(), Headers.build {
                    append(HttpHeaders.ContentType, "application/octet-stream")
                    append(HttpHeaders.ContentDisposition, "filename=\"${data.avatar.name}\"")
                })
                append("latitude",data.latitude)
                append("longitude",data.longitude)
            },
        )
    }

    override suspend fun signIn(
        login: String,
        password: String
    ): String {
        return httpClient.post("https://192.168.13.49:8080/signIn") { AuthRequest(login,password) }.body<String>()
    }
}