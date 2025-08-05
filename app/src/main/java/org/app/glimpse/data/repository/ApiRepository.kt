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
        return httpClient.get("https://192.168.13.15:8080/api/users"){
            header("Authorization", "Bearer eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoxLCJ1c2VyTmFtZSI6IkJvYiIsImNyYXRlZF9hdCI6IjIwMjUtMDgtMDVUMTg6MTE6NTIuMDU2NjUrMDU6MDAiLCJpc3MiOiJ5b3VyLWdpbi1jcnVkLWFwcCIsInN1YiI6IjEiLCJhdWQiOlsidXNlcnMiXSwiZXhwIjoxNzU0NDg5NTEyLCJuYmYiOjE3NTQ0MDMxMTIsImlhdCI6MTc1NDQwMzExMn0.xKqHtJMw9CFgkq0Lmy_VUbP6l8sy-fbTXWgkT2WzHEgekqdXVsLRdL2cHUjXE2W0FhRRHPxVH5KkDxM_YmMdYQ")
        }.body<User>()
    }

    override suspend fun signUp() {}
    override suspend fun signIn() {}
}