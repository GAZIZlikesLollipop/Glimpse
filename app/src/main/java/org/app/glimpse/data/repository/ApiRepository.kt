package org.app.glimpse.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import org.app.glimpse.data.network.GeocoderResponse
import org.app.glimpse.data.network.User
import java.util.Locale

interface ApiRepo {
    suspend fun getLocation(
        longitude: Double,
        latitude: Double,
        zoomLvl: Int,
        language: String? = null
    ): GeocoderResponse
    suspend fun getUserData(): User
    suspend fun signIn()
    suspend fun signUp()
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

    override suspend fun getUserData(): User {
        return httpClient.get("https://192.168.13.42:8080/api/users"){
            header("Authorization", "Bearer eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoyLCJ1c2VyTmFtZSI6IlVzZXIiLCJjcmF0ZWRfYXQiOiIyMDI1LTA4LTA4VDE1OjQ4OjQ1LjAzODk2OCswNTowMCIsImlzcyI6InlvdXItZ2luLWNydWQtYXBwIiwic3ViIjoiMiIsImF1ZCI6WyJ1c2VycyJdLCJleHAiOjE3NTQ3MzY2MTMsIm5iZiI6MTc1NDY1MDIxMywiaWF0IjoxNzU0NjUwMjEzfQ.JyjyTs9vXJ21_e2bcHaq4eMG7jTqIuG8Sh9ZCF4iJJVNu22J674fcn0NsGETp0Dv2p0aC3UIUSc1GgckCAxG6g")
        }.body<User>()
    }

    override suspend fun signUp() {}
    override suspend fun signIn() {}
}